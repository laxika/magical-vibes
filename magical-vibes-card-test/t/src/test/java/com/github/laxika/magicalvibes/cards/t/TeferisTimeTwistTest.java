package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GideonBlackblade;
import com.github.laxika.magicalvibes.cards.k.KayasGhostform;
import com.github.laxika.magicalvibes.cards.p.PouncingLynx;
import com.github.laxika.magicalvibes.cards.r.RoleReversal;
import com.github.laxika.magicalvibes.cards.u.UginsConjurant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TeferisTimeTwist.class, PouncingLynx.class, Island.class,
        GideonBlackblade.class, KayasGhostform.class, RoleReversal.class, UginsConjurant.class})
class TeferisTimeTwistTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a creature at the next end step with a +1/+1 counter")
    void returnsCreatureWithCounterAtNextEndStep() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new PouncingLynx());
        original.tap();
        original.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setHand(player1, List.of(new TeferisTimeTwist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID originalId = harness.getPermanentId(player1, "Pouncing Lynx");
        harness.castAndResolveInstant(player1, 0, originalId);

        harness.assertNotOnBattlefield(player1, "Pouncing Lynx");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Pouncing Lynx"));

        advanceToEndStep();

        Permanent returned = findPermanent(player1, "Pouncing Lynx");
        assertThat(returned.getId()).isNotEqualTo(originalId);
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(returned.isTapped()).isFalse();
        assertThat(returned.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Returns a noncreature permanent without a +1/+1 counter")
    void returnsNoncreatureWithoutCounter() {
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new TeferisTimeTwist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID originalId = harness.getPermanentId(player1, "Island");
        harness.castAndResolveInstant(player1, 0, originalId);
        harness.assertNotOnBattlefield(player1, "Island");

        advanceToEndStep();

        Permanent returned = findPermanent(player1, "Island");
        assertThat(returned.getId()).isNotEqualTo(originalId);
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Cannot target a permanent controlled by an opponent")
    void cannotTargetOpponentsPermanent() {
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(new TeferisTimeTwist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID opponentPermanentId = harness.getPermanentId(player2, "Island");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentPermanentId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returningGideonDuringYourTurnEntersWithCreatureCounter() {
        harness.forceActivePlayer(player1);
        Permanent original = harness.addToBattlefieldAndReturn(player1, new GideonBlackblade());
        harness.setHand(player1, List.of(new TeferisTimeTwist()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, original.getId());
        advanceToEndStep();

        Permanent returned = findPermanent(player1, "Gideon Blackblade");
        assertThat(gqs.isCreature(gd, returned)).isTrue();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(returned.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    void returningGideonDuringOpponentsTurnEntersWithoutCreatureCounter() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        Permanent original = harness.addToBattlefieldAndReturn(player1, new GideonBlackblade());
        harness.setHand(player1, List.of(new TeferisTimeTwist()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, original.getId());
        advanceToEndStep();

        Permanent returned = findPermanent(player1, "Gideon Blackblade");
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gqs.isCreature(gd, returned)).isFalse();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(returned.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    void castingDuringEndStepWaitsForTheFollowingTurnsEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        Permanent original = harness.addToBattlefieldAndReturn(player1, new PouncingLynx());
        harness.setHand(player1, List.of(new TeferisTimeTwist()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, original.getId());
        harness.assertNotOnBattlefield(player1, "Pouncing Lynx");
        harness.passUntilWithNoAttackers(player2, TurnStep.END_STEP);

        harness.assertNotOnBattlefield(player1, "Pouncing Lynx");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Pouncing Lynx");
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void returningAuraAttachesToTheOnlyLegalCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new PouncingLynx());
        harness.setHand(player1, List.of(new KayasGhostform(), new TeferisTimeTwist()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        UUID originalId = harness.getPermanentId(player1, "Kaya's Ghostform");

        harness.castAndResolveInstant(player1, 0, originalId);
        harness.assertNotOnBattlefield(player1, "Kaya's Ghostform");
        advanceToEndStep();

        Permanent returned = findPermanent(player1, "Kaya's Ghostform");
        assertThat(returned.getId()).isNotEqualTo(originalId);
        assertThat(returned.getAttachedTo()).isEqualTo(creature.getId());
        harness.assertNotInGraveyard(player1, "Kaya's Ghostform");
    }

    @Test
    void returningAuraStaysInExileWhenNothingCanBeEnchanted() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new PouncingLynx());
        harness.setHand(player1, List.of(new KayasGhostform(), new TeferisTimeTwist()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        UUID originalId = harness.getPermanentId(player1, "Kaya's Ghostform");
        var auraCard = findPermanent(player1, "Kaya's Ghostform").getCard();

        harness.castAndResolveInstant(player1, 0, originalId);
        harness.getPermanentRemovalService().removePermanentToExile(gd, creature);
        advanceToEndStep();

        harness.assertNotOnBattlefield(player1, "Kaya's Ghostform");
        harness.assertNotInGraveyard(player1, "Kaya's Ghostform");
        assertThat(gd.findExiledCard(auraCard.getId())).isNotNull();
    }

    @Test
    void stolenPermanentReturnsUnderItsOwnersControl() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new UginsConjurant());
        ownCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent opponentsCreature = harness.addToBattlefieldAndReturn(player2, new PouncingLynx());
        harness.setHand(player1, List.of(new RoleReversal(), new TeferisTimeTwist()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(ownCreature.getId(), opponentsCreature.getId()));
        harness.assertOnBattlefield(player1, "Pouncing Lynx");
        harness.castAndResolveInstant(player1, 0, opponentsCreature.getId());
        advanceToEndStep();

        harness.assertNotOnBattlefield(player1, "Pouncing Lynx");
        Permanent returned = findPermanent(player2, "Pouncing Lynx");
        assertThat(returned.getId()).isNotEqualTo(opponentsCreature.getId());
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void zeroToughnessCreatureEntersWithCounterBeforeStateBasedActions() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new UginsConjurant());
        original.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setHand(player1, List.of(new TeferisTimeTwist()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, original.getId());
        advanceToEndStep();

        Permanent returned = findPermanent(player1, "Ugin's Conjurant");
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Ugin's Conjurant");
    }

    private void advanceToEndStep() {
        harness.passUntilWithNoAttackers(null, TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
