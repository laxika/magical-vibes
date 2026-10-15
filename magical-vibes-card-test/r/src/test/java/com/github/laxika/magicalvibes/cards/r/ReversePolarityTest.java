package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BlazingTorch;
import com.github.laxika.magicalvibes.cards.f.Fog;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.Juggernaut;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReversePolarity.class, RazortipWhip.class, Shock.class, BlazingTorch.class, GrizzlyBears.class, Juggernaut.class, Fog.class})
class ReversePolarityTest extends BaseCardTest {

    @Test
    @DisplayName("Gains twice the damage dealt to you by artifacts this turn")
    void gainsTwiceArtifactDamage() {
        harness.addToBattlefield(player2, new RazortipWhip());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new ReversePolarity(), "{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Ignores damage from nonartifact sources")
    void ignoresNonartifactDamage() {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player1.getId());
        harness.castFromHand(player1, new ReversePolarity(), "{W}{W}");
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Counts damage from an equipment-granted ability as artifact damage")
    void countsDamageFromEquipmentGrantedAbility() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent torch = harness.addToBattlefieldAndReturn(player1, new BlazingTorch());
        torch.setAttachedTo(creature.getId());
        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);

        harness.castFromHand(player1, new ReversePolarity(), "{W}{W}");
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Gains no life when no artifact has dealt damage")
    void gainsNoLifeWithoutDamage() {
        harness.castFromHand(player1, new ReversePolarity(), "{W}{W}");
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Each copy counts the same damage without consuming it")
    void repeatedCastsCountTheSameDamage() {
        harness.addToBattlefield(player2, new RazortipWhip());
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 19);

        harness.castFromHand(player1, new ReversePolarity(), "{W}{W}");
        harness.passBothPriorities();
        harness.assertLife(player1, 21);
        harness.castFromHand(player1, new ReversePolarity(), "{W}{W}");
        harness.passBothPriorities();
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Counts artifact damage dealt in response to the spell")
    void evaluatesDamageAtResolution() {
        harness.addToBattlefield(player2, new RazortipWhip());
        harness.castFromHand(player1, new ReversePolarity(), "{W}{W}");
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 19);
        harness.passBothPriorities();
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Ignores artifact damage dealt to the opponent")
    void ignoresDamageToAnotherPlayer() {
        harness.addToBattlefield(player1, new RazortipWhip());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
        harness.castFromHand(player1, new ReversePolarity(), "{W}{W}");
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Counts combat damage from artifact creatures")
    void countsArtifactCombatDamage() {
        addCreatureReady(player2, new Juggernaut());
        declareAttackers(player2, List.of(0));
        resolveCombat(player2);
        harness.assertLife(player1, 15);
        harness.castFromHand(player1, new ReversePolarity(), "{W}{W}");
        harness.passBothPriorities();
        harness.assertLife(player1, 25);
    }

    @Test
    @DisplayName("Does not count prevented artifact combat damage")
    void ignoresPreventedArtifactCombatDamage() {
        addCreatureReady(player2, new Juggernaut());
        harness.castFromHand(player1, new Fog(), "{G}");
        harness.passBothPriorities();
        declareAttackers(player2, List.of(0));
        resolveCombat(player2);
        harness.assertLife(player1, 20);
        harness.castFromHand(player1, new ReversePolarity(), "{W}{W}");
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Does not count artifact damage from the previous turn")
    void ignoresDamageFromPreviousTurn() {
        harness.addToBattlefield(player2, new RazortipWhip());
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 19);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new ReversePolarity(), "{W}{W}");
        harness.passBothPriorities();
        harness.assertLife(player1, 19);
    }
}
