package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BecomeAnonymous.class, BayekOfSiwa.class, GrizzlyBears.class, Forest.class, Island.class})
class BecomeAnonymousTest extends BaseCardTest {

    @Test
    void exilesTargetAndTopTwoThenCloaksAllThreeTapped() {
        Card targetCard = new GrizzlyBears();
        Permanent target = harness.addToBattlefieldAndReturn(player1, targetCard);
        Card topCard = new Forest();
        Card secondCard = new Island();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        castBecomeAnonymous(target);

        List<Permanent> cloaked = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isCloaked)
                .toList();
        assertThat(cloaked).hasSize(3);
        assertThat(cloaked).extracting(Permanent::getCard)
                .containsExactlyInAnyOrder(targetCard, topCard, secondCard);
        assertThat(cloaked).allMatch(Permanent::isTapped);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(targetCard.getId())).isNull();
        assertThat(gd.findExiledCard(topCard.getId())).isNull();
        assertThat(gd.findExiledCard(secondCard.getId())).isNull();
    }

    @Test
    void cloaksOnlyAvailableLibraryCards() {
        Card targetCard = new GrizzlyBears();
        Permanent target = harness.addToBattlefieldAndReturn(player1, targetCard);
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        castBecomeAnonymous(target);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isCloaked)
                .map(Permanent::getCard))
                .containsExactlyInAnyOrder(targetCard, topCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void canTargetOnlyANontokenCreatureYouOwn() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BecomeAnonymous()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nontoken creature you own");
    }

    @Test
    void cloaksTargetWhenLibraryIsEmpty() {
        Card targetCard = new GrizzlyBears();
        Permanent target = harness.addToBattlefieldAndReturn(player1, targetCard);
        harness.setLibrary(player1, List.of());
        castBecomeAnonymous(target);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Permanent cloaked = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(cloaked.getCard()).isSameAs(targetCard);
        assertThat(cloaked.isCloaked()).isTrue();
        assertThat(cloaked.isTapped()).isTrue();
    }

    @Test
    void returnsOwnedCreatureFromOpponentsControlToItsOwner() {
        Card targetCard = new GrizzlyBears();
        Permanent target = harness.addToBattlefieldAndReturn(player2, targetCard);
        gd.stolenCreatures.put(target.getId(), player1.getId());
        harness.setLibrary(player1, List.of(new Forest(), new Island()));
        castBecomeAnonymous(target);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .hasSize(3).allMatch(Permanent::isCloaked).allMatch(Permanent::isTapped);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).contains(targetCard);
    }

    @Test
    void cloakedCreatureCanTurnFaceUpForItsManaCostAndStaysTapped() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of());
        castBecomeAnonymous(target);
        Permanent cloaked = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.turnFaceUp(player1, 0);

        assertThat(cloaked.isFaceDown()).isFalse();
        assertThat(cloaked.isTapped()).isTrue();
    }

    @Test
    void cloakedLandCannotTurnFaceUpUsingCloak() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card land = new Island();
        harness.setLibrary(player1, List.of(land));
        castBecomeAnonymous(target);
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        Permanent cloakedLand = battlefield.stream()
                .filter(permanent -> permanent.getCard() == land).findFirst().orElseThrow();

        assertThatThrownBy(() -> harness.turnFaceUp(player1, battlefield.indexOf(cloakedLand)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(cloakedLand.isCloaked()).isTrue();
    }

    @Test
    void cloakedCreatureCanUseItsCheaperDisguiseCost() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BayekOfSiwa());
        harness.setLibrary(player1, List.of());
        castBecomeAnonymous(target);
        Permanent cloaked = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.turnFaceUp(player1, 0);

        assertThat(cloaked.isFaceDown()).isFalse();
        assertThat(cloaked.isTapped()).isTrue();
    }

    @Test
    void cloakedCreatureCanTurnFaceUpAfterLosingItsAbilities() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of());
        castBecomeAnonymous(target);
        Permanent cloaked = gd.playerBattlefields.get(player1.getId()).getFirst();
        cloaked.setLosesAllAbilitiesUntilEndOfTurn(true);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.turnFaceUp(player1, 0);

        assertThat(cloaked.isFaceDown()).isFalse();
        assertThat(cloaked.isTapped()).isTrue();
    }
    @Test
    void doesNotExileLibraryCardsWhenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card topCard = new Forest();
        Card secondCard = new Island();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.setHand(player1, List.of(new BecomeAnonymous()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, target.getId());
        harness.getPermanentRemovalService().removePermanentToExile(gd, target);

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, secondCard);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void cannotTargetANoncreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new BecomeAnonymous()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
    private void castBecomeAnonymous(Permanent target) {
        harness.setHand(player1, List.of(new BecomeAnonymous()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
