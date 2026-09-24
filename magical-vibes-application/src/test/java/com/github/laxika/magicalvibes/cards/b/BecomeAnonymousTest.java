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

@CardUsed({BecomeAnonymous.class, GrizzlyBears.class, Forest.class, Island.class})
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

    private void castBecomeAnonymous(Permanent target) {
        harness.setHand(player1, List.of(new BecomeAnonymous()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
