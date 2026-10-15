package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FestivalCrasher;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AbandonThePost.class, Forest.class, FestivalCrasher.class})
class AbandonThePostTest extends BaseCardTest {

    private void addNormalMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Up to two target creatures can't block this turn")
    void makesTwoCreaturesUnableToBlock() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new FestivalCrasher());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new FestivalCrasher());
        harness.setHand(player1, List.of(new AbandonThePost()));
        addNormalMana();

        harness.castAndResolveSorcery(player1, 0, List.of(first.getId(), second.getId()));

        assertThat(first.isCantBlockThisTurn()).isTrue();
        assertThat(second.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("May target only one creature")
    void makesOneCreatureUnableToBlock() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new FestivalCrasher());
        harness.setHand(player1, List.of(new AbandonThePost()));
        addNormalMana();

        harness.castAndResolveSorcery(player1, 0, List.of(bear.getId()));

        assertThat(bear.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new AbandonThePost()));
        addNormalMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(forest.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flashback makes target creatures unable to block and exiles the spell")
    void flashbackMakesCreaturesUnableToBlockAndExilesSpell() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new FestivalCrasher());
        harness.setGraveyard(player1, List.of(new AbandonThePost()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 0, List.of(bear.getId()));
        harness.passBothPriorities();

        assertThat(bear.isCantBlockThisTurn()).isTrue();
        harness.assertNotInGraveyard(player1, "Abandon the Post");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Abandon the Post"));
    }

    @Test
    @DisplayName("The can't-block effect wears off at end of turn")
    void effectWearsOffAtEndOfTurn() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new FestivalCrasher());
        harness.setHand(player1, List.of(new AbandonThePost()));
        addNormalMana();

        harness.castAndResolveSorcery(player1, 0, List.of(bear.getId()));
        assertThat(bear.isCantBlockThisTurn()).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(bear.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("May resolve with zero targets")
    void mayChooseZeroTargets() {
        harness.setHand(player1, List.of(new AbandonThePost()));
        addNormalMana();

        harness.castAndResolveSorcery(player1, 0, List.of());

        harness.assertInGraveyard(player1, "Abandon the Post");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot choose more than two targets")
    void cannotChooseThreeTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new FestivalCrasher());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new FestivalCrasher());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new FestivalCrasher());
        harness.setHand(player1, List.of(new AbandonThePost()));
        addNormalMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot choose the same creature twice")
    void cannotChooseDuplicateTargets() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new FestivalCrasher());
        harness.setHand(player1, List.of(new AbandonThePost()));
        addNormalMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(bear.getId(), bear.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A remaining legal target is affected when the other target leaves")
    void affectsRemainingTarget() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new FestivalCrasher());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new FestivalCrasher());
        harness.setHand(player1, List.of(new AbandonThePost()));
        addNormalMana();

        harness.castSorcery(player1, 0, List.of(first.getId(), second.getId()));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, first));
        harness.passBothPriorities();

        assertThat(second.isCantBlockThisTurn()).isTrue();
        harness.assertInGraveyard(player1, "Abandon the Post");
    }

    @Test
    @DisplayName("Flashback with no targets still exiles the spell")
    void flashbackWithZeroTargetsExilesSpell() {
        harness.setGraveyard(player1, List.of(new AbandonThePost()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 0, List.of());
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Abandon the Post");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Abandon the Post"));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Flashback exiles the spell even when its only target leaves")
    void flashbackWithNoRemainingLegalTargetExilesSpell() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new FestivalCrasher());
        harness.setGraveyard(player1, List.of(new AbandonThePost()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 0, List.of(bear.getId()));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, bear));
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Abandon the Post");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Abandon the Post"));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Flashback requires its full cost rather than the normal spell cost")
    void flashbackCannotUseNormalManaCost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new FestivalCrasher());
        harness.setGraveyard(player1, List.of(new AbandonThePost()));
        addNormalMana();

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Abandon the Post");
        assertThat(creature.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Creatures that were not targeted can still block")
    void doesNotAffectUntargetedCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FestivalCrasher());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new FestivalCrasher());
        harness.setHand(player1, List.of(new AbandonThePost()));
        addNormalMana();

        harness.castAndResolveSorcery(player1, 0, List.of(target.getId()));

        assertThat(target.isCantBlockThisTurn()).isTrue();
        assertThat(other.isCantBlockThisTurn()).isFalse();
    }
}
