package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.ChandrasPyrohelix;
import com.github.laxika.magicalvibes.cards.l.LeylineProwler;
import com.github.laxika.magicalvibes.cards.r.RagingKronch;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.u.UginTheIneffable;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GideonsSacrifice.class, RagingKronch.class, ChandrasPyrohelix.class,
        UginTheIneffable.class, LeylineProwler.class, Shock.class})
class GideonsSacrificeTest extends BaseCardTest {

    @Test
    @DisplayName("Redirects damage to the chosen creature instead of the controller")
    void redirectsDamageToChosenCreature() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new RagingKronch());

        castAndChoose(chosen);
        castPyrohelixAtPlayer(player1);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(chosen.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Redirects damage to the chosen creature instead of another permanent")
    void redirectsDamageToChosenCreatureFromControlledPermanent() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new RagingKronch());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new RagingKronch());

        castAndChoose(chosen);
        castPyrohelixAtPermanent(other);

        assertThat(other.getMarkedDamage()).isZero();
        assertThat(chosen.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can redirect damage to a chosen planeswalker")
    void redirectsDamageToChosenPlaneswalker() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new UginTheIneffable());
        chosen.setCounterCount(CounterType.LOYALTY, 4);
        int startingLoyalty = chosen.getCounterCount(CounterType.LOYALTY);

        castAndChoose(chosen);
        castPyrohelixAtPlayer(player1);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(chosen.getCounterCount(CounterType.LOYALTY)).isEqualTo(startingLoyalty - 2);
    }

    @Test
    void doesNothingWithoutACreatureOrPlaneswalker() {
        harness.setHand(player1, List.of(new GideonsSacrifice()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0);

        castPyrohelixAtPlayer(player1);

        harness.assertLife(player1, 18);
    }

    @Test
    void doesNotRedirectDamageToOpponents() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new RagingKronch());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new RagingKronch());
        castAndChoose(chosen);

        castPyrohelixAtPlayer(player2);
        castPyrohelixAtPermanent(opponent);

        harness.assertLife(player2, 18);
        assertThat(opponent.getMarkedDamage()).isEqualTo(2);
        assertThat(chosen.getMarkedDamage()).isZero();
    }

    @Test
    void damageToChosenPermanentIsDealtOnlyOnce() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new RagingKronch());
        castAndChoose(chosen);

        castPyrohelixAtPermanent(chosen);

        assertThat(chosen.getMarkedDamage()).isEqualTo(2);
        harness.assertLife(player1, 20);
    }

    @Test
    void stopsRedirectingAfterChosenPermanentDies() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new RagingKronch());
        castAndChoose(chosen);

        castPyrohelixAtPlayer(player1);
        castPyrohelixAtPlayer(player1);
        harness.assertInGraveyard(player1, "Raging Kronch");
        castPyrohelixAtPlayer(player1);

        harness.assertLife(player1, 18);
    }

    @Test
    void doesNotUndoDamageDealtBeforeResolution() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new RagingKronch());
        castPyrohelixAtPlayer(player1);
        castAndChoose(chosen);

        castPyrohelixAtPlayer(player1);

        harness.assertLife(player1, 18);
        assertThat(chosen.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void redirectionExpiresAtEndOfTurn() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new RagingKronch());
        castAndChoose(chosen);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        castPyrohelixAtPlayer(player1);

        harness.assertLife(player1, 18);
        assertThat(chosen.getMarkedDamage()).isZero();
    }

    @Test
    void redirectsDamageAimedAtAnotherPlaneswalker() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new RagingKronch());
        Permanent protectedPlaneswalker = harness.addToBattlefieldAndReturn(player1, new UginTheIneffable());
        protectedPlaneswalker.setCounterCount(CounterType.LOYALTY, 4);
        castAndChoose(chosen);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, protectedPlaneswalker.getId());

        assertThat(protectedPlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(chosen.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void multipleSacrificesOfferADestinationChoiceWhenDamageWouldBeDealt() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new RagingKronch());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new RagingKronch());
        castAndChoose(first);
        castAndChoose(second);

        castPyrohelixAtPlayer(player1);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(first.getMarkedDamage()).isZero();
        assertThat(second.getMarkedDamage()).isZero();
        harness.assertLife(player1, 20);
    }

    @Test
    void redirectedCombatDamageRetainsLifelink() {
        Permanent chosen = addCreatureReady(player1, new RagingKronch());
        Permanent attacker = addCreatureReady(player2, new LeylineProwler());
        castAndChoose(chosen);
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 22);
    }

    @Test
    void redirectedCombatDamageRetainsDeathtouch() {
        Permanent chosen = addCreatureReady(player1, new RagingKronch());
        Permanent attacker = addCreatureReady(player2, new LeylineProwler());
        castAndChoose(chosen);
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertNotOnBattlefield(player1, "Raging Kronch");
        harness.assertInGraveyard(player1, "Raging Kronch");
    }

    private void castAndChoose(Permanent chosen) {
        harness.setHand(player1, List.of(new GideonsSacrifice()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(chosen.getId()));
    }

    private void castPyrohelixAtPlayer(com.github.laxika.magicalvibes.model.Player player) {
        harness.setHand(player2, List.of(new ChandrasPyrohelix()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, Map.of(player.getId(), 2));
        harness.passBothPriorities();
    }

    private void castPyrohelixAtPermanent(Permanent target) {
        harness.setHand(player2, List.of(new ChandrasPyrohelix()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, Map.of(target.getId(), 2));
        harness.passBothPriorities();
    }
}
