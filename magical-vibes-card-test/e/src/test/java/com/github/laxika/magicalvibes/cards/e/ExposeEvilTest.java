package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.BriarbridgePatrol;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExposeEvil.class, BriarbridgePatrol.class, Forest.class})
class ExposeEvilTest extends BaseCardTest {

    @Test
    @DisplayName("Taps two target creatures and creates a Clue")
    void tapsTwoCreaturesAndInvestigates() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new BriarbridgePatrol());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new BriarbridgePatrol());
        harness.setHand(player1, List.of(new ExposeEvil()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, List.of(first.getId(), second.getId()));

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Can target only one creature")
    void tapsOneCreatureAndInvestigates() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new BriarbridgePatrol());
        harness.setHand(player1, List.of(new ExposeEvil()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, List.of(bear.getId()));

        assertThat(bear.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void investigatesWithNoTargets() {
        harness.setHand(player1, List.of(new ExposeEvil()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, List.of());

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player2, "Clue")).isEmpty();
        harness.assertInGraveyard(player1, "Expose Evil");
    }

    @Test
    void resolvesWithOneRemainingLegalTarget() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new BriarbridgePatrol());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new BriarbridgePatrol());
        harness.setHand(player1, List.of(new ExposeEvil()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, List.of(first.getId(), second.getId()));

        gd.playerBattlefields.get(player2.getId()).remove(first);
        gd.playerHands.get(player2.getId()).add(first.getCard());
        harness.passBothPriorities();

        assertThat(second.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void doesNotInvestigateWhenAllChosenTargetsAreIllegal() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BriarbridgePatrol());
        harness.setHand(player1, List.of(new ExposeEvil()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, List.of(creature.getId()));

        gd.playerBattlefields.get(player2.getId()).remove(creature);
        gd.playerHands.get(player2.getId()).add(creature.getCard());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        harness.assertInGraveyard(player1, "Expose Evil");
    }

    @Test
    void canTargetOwnAlreadyTappedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BriarbridgePatrol());
        creature.tap();
        harness.setHand(player1, List.of(new ExposeEvil()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, List.of(creature.getId()));

        assertThat(creature.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void createdClueCanBeSacrificedToDraw() {
        Forest drawnCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new ExposeEvil()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        harness.assertNotInGraveyard(player1, "Clue");
    }

    @Test
    void cannotChooseThreeCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new BriarbridgePatrol());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new BriarbridgePatrol());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new BriarbridgePatrol());
        harness.setHand(player1, List.of(new ExposeEvil()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotChooseTheSameCreatureTwice() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BriarbridgePatrol());
        harness.setHand(player1, List.of(new ExposeEvil()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new ExposeEvil()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
