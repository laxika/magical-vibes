package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndImprintEffect;
import com.github.laxika.magicalvibes.model.effect.ImprintedCardNameMatchesEnteringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class InvaderParasiteTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has ETB exile-and-imprint effect and opponent-land-enters trigger")
    void hasCorrectEffects() {
        InvaderParasite card = new InvaderParasite();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(ExileTargetPermanentAndImprintEffect.class);

        assertThat(card.getEffects(EffectSlot.ON_OPPONENT_LAND_ENTERS_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_OPPONENT_LAND_ENTERS_BATTLEFIELD).getFirst())
                .isInstanceOf(ImprintedCardNameMatchesEnteringPermanentConditionalEffect.class);
        ImprintedCardNameMatchesEnteringPermanentConditionalEffect trigger =
                (ImprintedCardNameMatchesEnteringPermanentConditionalEffect)
                        card.getEffects(EffectSlot.ON_OPPONENT_LAND_ENTERS_BATTLEFIELD).getFirst();
        assertThat(trigger.wrapped()).isInstanceOf(DealDamageToTargetPlayerEffect.class);
        assertThat(((DealDamageToTargetPlayerEffect) trigger.wrapped()).damage()).isEqualTo(2);
    }

    // ===== ETB: exile target land =====

    @Test
    @DisplayName("ETB exiles target land and imprints it")
    void etbExilesTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new InvaderParasite()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID forestId = harness.getPermanentId(player2, "Forest");
        harness.castCreature(player1, 0, 0, forestId);
        // Resolve creature spell (creature enters, ETB goes on stack)
        harness.passBothPriorities();
        // Resolve ETB trigger (exiles the forest)
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Invader Parasite");
        harness.assertNotOnBattlefield(player2, "Forest");

        // Verify the forest is exiled (in player2's exile zone)
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Forest"));
    }

    // ===== Triggered ability: opponent land with same name =====

    @Test
    @DisplayName("Deals 2 damage when opponent plays land with same name as exiled card")
    void triggersOnMatchingLand() {
        // Set up Invader Parasite with an imprinted Forest
        InvaderParasite parasite = new InvaderParasite();
        harness.addToBattlefield(player1, parasite);
        // Manually imprint a Forest
        Forest imprintedForest = new Forest();
        parasite.setImprintedCard(imprintedForest);

        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Opponent plays a Forest
        harness.setHand(player2, List.of(new Forest()));
        harness.castCreature(player2, 0);

        // Trigger should be on the stack
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not trigger when opponent plays land with different name")
    void doesNotTriggerOnDifferentLand() {
        InvaderParasite parasite = new InvaderParasite();
        harness.addToBattlefield(player1, parasite);
        Forest imprintedForest = new Forest();
        parasite.setImprintedCard(imprintedForest);

        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Opponent plays a Mountain (different name from imprinted Forest)
        harness.setHand(player2, List.of(new Mountain()));
        harness.castCreature(player2, 0);

        // No trigger should fire
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not trigger for controller's own matching lands")
    void doesNotTriggerForControllerLands() {
        InvaderParasite parasite = new InvaderParasite();
        harness.addToBattlefield(player1, parasite);
        Forest imprintedForest = new Forest();
        parasite.setImprintedCard(imprintedForest);

        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Controller plays a Forest (same name as imprint)
        harness.setHand(player1, List.of(new Forest()));
        harness.castCreature(player1, 0);

        // No trigger — only cares about opponents
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not trigger when no card is imprinted")
    void doesNotTriggerWithNoImprint() {
        // Invader Parasite with no imprinted card (e.g. ETB was countered)
        InvaderParasite parasite = new InvaderParasite();
        harness.addToBattlefield(player1, parasite);

        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Forest()));
        harness.castCreature(player2, 0);

        // No trigger — nothing imprinted
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Two Invader Parasites imprinting same land name trigger separately")
    void twoParasitesStack() {
        InvaderParasite parasite1 = new InvaderParasite();
        InvaderParasite parasite2 = new InvaderParasite();
        harness.addToBattlefield(player1, parasite1);
        harness.addToBattlefield(player1, parasite2);
        parasite1.setImprintedCard(new Forest());
        parasite2.setImprintedCard(new Forest());

        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Forest()));
        harness.castCreature(player2, 0);

        // Two triggers (one per Invader Parasite)
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Triggers each time opponent plays a matching land on separate turns")
    void triggersOnEachMatchingLand() {
        InvaderParasite parasite = new InvaderParasite();
        harness.addToBattlefield(player1, parasite);
        parasite.setImprintedCard(new Swamp());

        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // First Swamp on first turn
        harness.setHand(player2, List.of(new Swamp()));
        harness.castCreature(player2, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        // Reset lands-played-this-turn tracking for a new turn
        gd.landsPlayedThisTurn.put(player2.getId(), 0);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Second Swamp on new turn
        harness.setHand(player2, List.of(new Swamp()));
        harness.castCreature(player2, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }
}
