package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LingeringMirage;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HolyAvenger.class, GrizzlyBears.class, HolyStrength.class, LingeringMirage.class})
class HolyAvengerTest extends BaseCardTest {

    @Test
    @DisplayName("Holy Avenger grants double strike to its equipped creature")
    void grantsDoubleStrikeToEquippedCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent avenger = addAvengerReady(player1);
        avenger.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Combat damage trigger offers a legal Aura and attaches it to the equipped creature")
    void combatDamagePutsAuraOntoEquippedCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent avenger = addAvengerReady(player1);
        avenger.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        harness.setHand(player1, List.of(new HolyStrength()));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.TargetedHandCardChoice.class);
        harness.handleCardChosen(player1, 0);

        Permanent aura = findPermanent(player1, "Holy Strength");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Only Auras that can enchant the equipped creature are offered")
    void onlyLegalAurasAreOffered() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent avenger = addAvengerReady(player1);
        avenger.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        harness.setHand(player1, List.of(new LingeringMirage(), new HolyStrength()));

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.HandChoice choice =
                (PendingInteraction.HandChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIndices()).containsExactly(1);
    }

    @Test
    @DisplayName("Declining the trigger leaves the Aura in hand")
    void decliningTriggerLeavesAuraInHand() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent avenger = addAvengerReady(player1);
        avenger.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        harness.setHand(player1, List.of(new HolyStrength()));

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Holy Strength");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Holy Strength"));
    }

    private Permanent addAvengerReady(Player player) {
        Permanent permanent = new Permanent(new HolyAvenger());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
