package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MerfolkOfTheDepths;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({CrashingTide.class, GrizzlyBears.class, MerfolkOfTheDepths.class, Mountain.class})
class CrashingTideTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target creature and draws a card")
    void returnsCreatureAndDrawsCard() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card topCard = gd.playerDecks.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new CrashingTide()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getId)
                .contains(bears.getCard().getId());
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new CrashingTide()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can be cast during an opponent's turn while controlling a Merfolk")
    void canBeCastAtInstantSpeedWithMerfolk() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new MerfolkOfTheDepths());
        castDuringOpponentsTurn(bears);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot be cast during an opponent's turn without a Merfolk")
    void cannotBeCastAtInstantSpeedWithoutMerfolk() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new CrashingTide()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("An opponent's Merfolk does not grant flash")
    void opponentsMerfolkDoesNotGrantFlash() {
        Permanent merfolk = harness.addToBattlefieldAndReturn(player2, new MerfolkOfTheDepths());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new CrashingTide()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, merfolk.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Does not draw when its only target leaves before resolution")
    void doesNotDrawWhenTargetLeaves() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card topCard = gd.playerDecks.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new CrashingTide()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, 0, bears.getId());

        harness.getPermanentRemovalService().removePermanentToHand(gd, bears);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
        harness.assertInGraveyard(player1, "Crashing Tide");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Still resolves after the caster loses their Merfolk")
    void resolvesAfterLosingMerfolk() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new MerfolkOfTheDepths());
        Card topCard = gd.playerDecks.get(player1.getId()).getFirst();
        castDuringOpponentsTurn(bears);

        harness.getPermanentRemovalService().removePermanentToHand(gd, merfolk);
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).contains(topCard, merfolk.getCard());
        harness.assertInGraveyard(player1, "Crashing Tide");
    }

    @Test
    @DisplayName("Can return its caster's only Merfolk and still draw")
    void canReturnOwnOnlyMerfolk() {
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new MerfolkOfTheDepths());
        Card topCard = gd.playerDecks.get(player1.getId()).getFirst();
        castDuringOpponentsTurn(merfolk);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(merfolk.getCard(), topCard);
        harness.assertInGraveyard(player1, "Crashing Tide");
    }
    private void castDuringOpponentsTurn(Permanent target) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new CrashingTide()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0, target.getId());
    }
}
