package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.ElaborateFirecannon;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({AltanakTheThriceCalled.class, ElaborateFirecannon.class, Forest.class, Shock.class})
class AltanakTheThriceCalledTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when an opponent's spell targets Altanak")
    void drawsWhenOpponentSpellTargetsAltanak() {
        harness.addToBattlefield(player1, new AltanakTheThriceCalled());
        UUID altanakId = harness.getPermanentId(player1, "Altanak, the Thrice-Called");
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, altanakId);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Draws a card when an opponent's ability targets Altanak")
    void drawsWhenOpponentAbilityTargetsAltanak() {
        harness.addToBattlefield(player1, new AltanakTheThriceCalled());
        Permanent firecannon = harness.addToBattlefieldAndReturn(player2, new ElaborateFirecannon());
        firecannon.setSummoningSick(false);
        UUID altanakId = harness.getPermanentId(player1, "Altanak, the Thrice-Called");
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.activateAbility(player2, 0, null, altanakId);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Returns a targeted land tapped and discards Altanak from hand")
    void returnsTargetedLandTappedFromHand() {
        AltanakTheThriceCalled altanak = new AltanakTheThriceCalled();
        harness.setHand(player1, List.of(altanak));
        Card forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateHandAbilityWithGraveyardTargets(player1, 0, List.of(forest.getId()));
        harness.passBothPriorities();

        Permanent returnedForest = findPermanent(player1, "Forest");
        assertThat(returnedForest.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(altanak);
    }

    @Test
    @DisplayName("Rejects a nonland card as the hand ability's target")
    void rejectsNonlandGraveyardTarget() {
        AltanakTheThriceCalled altanak = new AltanakTheThriceCalled();
        harness.setHand(player1, List.of(altanak));
        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateHandAbilityWithGraveyardTargets(
                player1, 0, List.of(shock.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(altanak);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    void doesNotDrawWhenOwnSpellTargetsAltanak() {
        Permanent altanak = harness.addToBattlefieldAndReturn(player1, new AltanakTheThriceCalled());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, altanak.getId());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void rejectsLandInOpponentsGraveyardWithoutPayingCosts() {
        AltanakTheThriceCalled altanak = new AltanakTheThriceCalled();
        Card forest = new Forest();
        harness.setHand(player1, List.of(altanak));
        harness.setGraveyard(player2, List.of(forest));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateHandAbilityWithGraveyardTargets(
                player1, 0, List.of(forest.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(altanak);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(forest);
    }

    @Test
    void rejectsMultipleLandTargetsWithoutPayingCosts() {
        AltanakTheThriceCalled altanak = new AltanakTheThriceCalled();
        Card firstForest = new Forest();
        Card secondForest = new Forest();
        harness.setHand(player1, List.of(altanak));
        harness.setGraveyard(player1, List.of(firstForest, secondForest));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateHandAbilityWithGraveyardTargets(
                player1, 0, List.of(firstForest.getId(), secondForest.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(altanak);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    void paysDiscardImmediatelyAndDoesNotReturnLandThatLeavesGraveyard() {
        AltanakTheThriceCalled altanak = new AltanakTheThriceCalled();
        Card forest = new Forest();
        harness.setHand(player1, List.of(altanak));
        harness.setGraveyard(player1, List.of(forest));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateHandAbilityWithGraveyardTargets(player1, 0, List.of(forest.getId()));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(altanak, forest);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        gd.playerGraveyards.get(player1.getId()).remove(forest);
        harness.setHand(player1, List.of(forest));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(altanak);
    }

    @Test
    void drawsForEachSeparateOpponentsSpellBeforeThoseSpellsResolve() {
        Permanent altanak = harness.addToBattlefieldAndReturn(player1, new AltanakTheThriceCalled());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player2, 0, altanak.getId());
        harness.castInstant(player2, 0, altanak.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(altanak.getMarkedDamage()).isZero();

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        assertThat(altanak.getMarkedDamage()).isEqualTo(4);
        harness.assertOnBattlefield(player1, "Altanak, the Thrice-Called");
    }

    @Test
    void doesNotDrawWhenOwnAbilityTargetsAltanak() {
        Permanent altanak = harness.addToBattlefieldAndReturn(player1, new AltanakTheThriceCalled());
        harness.addToBattlefield(player1, new ElaborateFirecannon());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 1, null, altanak.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(altanak.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void cannotDiscardAltanakWithoutALandTarget() {
        AltanakTheThriceCalled altanak = new AltanakTheThriceCalled();
        harness.setHand(player1, List.of(altanak));
        harness.setGraveyard(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateHandAbilityWithGraveyardTargets(
                player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(altanak);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotPayTheHandAbilityWithOnlyGenericMana() {
        AltanakTheThriceCalled altanak = new AltanakTheThriceCalled();
        Card forest = new Forest();
        harness.setHand(player1, List.of(altanak));
        harness.setGraveyard(player1, List.of(forest));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateHandAbilityWithGraveyardTargets(
                player1, 0, List.of(forest.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(altanak);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }
}
