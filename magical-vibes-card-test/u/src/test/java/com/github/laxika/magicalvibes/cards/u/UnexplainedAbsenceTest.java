package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnexplainedAbsence.class, Forest.class, GrizzlyBears.class, SoulWarden.class})
class UnexplainedAbsenceTest extends BaseCardTest {

    @Test
    void exilesUpToOnePermanentPerControllerAndCloaksEachControllersTopCard() {
        Permanent ownPermanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentPermanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Forest ownTopCard = new Forest();
        Forest opponentTopCard = new Forest();
        harness.setLibrary(player1, List.of(ownTopCard));
        harness.setLibrary(player2, List.of(opponentTopCard));

        castUnexplainedAbsence(List.of(ownPermanent.getId(), opponentPermanent.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownPermanent);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentPermanent);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> assertThat(permanent.getCard()).isSameAs(ownTopCard));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anySatisfy(permanent -> assertThat(permanent.getCard()).isSameAs(opponentTopCard));
        assertThat(gd.playerBattlefields.get(player1.getId()).stream())
                .anyMatch(Permanent::isCloaked);
        assertThat(gd.playerBattlefields.get(player2.getId()).stream())
                .anyMatch(Permanent::isCloaked);
    }

    @Test
    void cannotChooseTwoPermanentsControlledByTheSamePlayer() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new UnexplainedAbsence()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one permanent per controller");
    }

    @Test
    void cannotTargetAland() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new UnexplainedAbsence()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonland permanent");
    }

    @Test
    void choosingNoTargetsDoesNotCloakAnyCards() {
        Forest ownTopCard = new Forest();
        Forest opponentTopCard = new Forest();
        harness.setLibrary(player1, List.of(ownTopCard));
        harness.setLibrary(player2, List.of(opponentTopCard));

        castUnexplainedAbsence(List.of());

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownTopCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentTopCard);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Unexplained Absence");
    }

    @Test
    void emptyLibraryDoesNotPreventExilingTheTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of());

        castUnexplainedAbsence(List.of(target.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.findExiledCard(target.getCard().getId())).isNotNull();
    }

    @Test
    void onlyTheChosenPermanentsControllerCloaksAndCanTurnACreatureFaceUp() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        GrizzlyBears topCard = new GrizzlyBears();
        Forest untouchedCard = new Forest();
        harness.setLibrary(player1, List.of(untouchedCard));
        harness.setLibrary(player2, List.of(topCard));

        castUnexplainedAbsence(List.of(target.getId()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouchedCard);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        Permanent cloaked = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(cloaked.getCard()).isSameAs(topCard);
        assertThat(cloaked.isCloaked()).isTrue();
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.turnFaceUp(player2, 0);

        assertThat(cloaked.isFaceDown()).isFalse();
        assertThat(cloaked.isCloaked()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void aCloakedLandCannotBeTurnedFaceUpByPayingItsManaCost() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Forest topCard = new Forest();
        harness.setLibrary(player2, List.of(topCard));

        castUnexplainedAbsence(List.of(target.getId()));

        assertThatThrownBy(() -> harness.turnFaceUp(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not a creature card");
        Permanent cloaked = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(cloaked.getCard()).isSameAs(topCard);
        assertThat(cloaked.isCloaked()).isTrue();
        assertThat(cloaked.isFaceDown()).isTrue();
    }

    @Test
    void allTargetsAreExiledBeforeAnyCardsAreCloaked() {
        Permanent ownTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent warden = harness.addToBattlefieldAndReturn(player2, new SoulWarden());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setLife(player2, 20);

        castUnexplainedAbsence(List.of(ownTarget.getId(), warden.getId()));

        assertThat(gd.findExiledCard(ownTarget.getCard().getId())).isNotNull();
        assertThat(gd.findExiledCard(warden.getCard().getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    private void castUnexplainedAbsence(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new UnexplainedAbsence()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player1, 0, targetIds);
    }
}
