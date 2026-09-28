package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WizardMentor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RobeOfTheArchmagi.class, Forest.class, FugitiveWizard.class, GrizzlyBears.class, WizardMentor.class})
class RobeOfTheArchmagiTest extends BaseCardTest {

    @Test
    @DisplayName("The restricted equip ability attaches Robe of the Archmagi to a Wizard")
    void restrictedEquipAttachesToWizard() {
        Permanent robe = addRobeReady();
        Permanent wizard = addCreatureReady(player1, new FugitiveWizard());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(robe), 1,
                null, wizard.getId());
        harness.passBothPriorities();

        assertThat(robe.getAttachedTo()).isEqualTo(wizard.getId());
    }

    @Test
    @DisplayName("The restricted equip ability cannot target a nonmatching creature")
    void restrictedEquipRejectsNonmatchingCreature() {
        Permanent robe = addRobeReady();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(robe), 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Shaman, Warlock, or Wizard");
    }

    @Test
    @DisplayName("Equipped creature's combat damage draws that many cards")
    void combatDamageDrawsThatManyCards() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        Permanent creature = addCreatureReady(player1, new WizardMentor());
        Permanent robe = addRobeReady();
        robe.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
    }

    private Permanent addRobeReady() {
        Permanent robe = harness.addToBattlefieldAndReturn(player1, new RobeOfTheArchmagi());
        robe.setSummoningSick(false);
        return robe;
    }
}
