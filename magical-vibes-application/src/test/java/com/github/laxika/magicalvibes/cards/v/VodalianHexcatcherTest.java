package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MerfolkOfThePearlTrident;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VodalianHexcatcher.class, MerfolkOfThePearlTrident.class, GrizzlyBears.class, Shock.class})
class VodalianHexcatcherTest extends BaseCardTest {

    @Test
    @DisplayName("Other Merfolk you control get +1/+1")
    void boostsOtherMerfolkYouControl() {
        Permanent hexcatcher = harness.addToBattlefieldAndReturn(player1, new VodalianHexcatcher());
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new MerfolkOfThePearlTrident());
        Permanent nonMerfolk = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingMerfolk = harness.addToBattlefieldAndReturn(player2, new MerfolkOfThePearlTrident());

        assertThat(gqs.getEffectivePower(gd, hexcatcher)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, hexcatcher)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, merfolk)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, merfolk)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, nonMerfolk)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nonMerfolk)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingMerfolk)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opposingMerfolk)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrificing a Merfolk counters a noncreature spell when its controller cannot pay")
    void countersNoncreatureSpellWhenControllerCannotPay() {
        harness.addToBattlefield(player1, new VodalianHexcatcher());

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, shock.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Vodalian Hexcatcher");
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("A noncreature spell survives when its controller pays {1}")
    void noncreatureSpellSurvivesWhenControllerPays() {
        harness.addToBattlefield(player1, new VodalianHexcatcher());

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, shock.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Vodalian Hexcatcher");
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Cannot target a creature spell")
    void cannotTargetCreatureSpell() {
        harness.addToBattlefield(player1, new VodalianHexcatcher());

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player2, List.of(bears));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
