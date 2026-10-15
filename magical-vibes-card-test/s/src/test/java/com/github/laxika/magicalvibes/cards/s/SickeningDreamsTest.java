package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AngelOfRetribution;
import com.github.laxika.magicalvibes.cards.a.AvenTrooper;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SickeningDreams.class, AvenTrooper.class, AngelOfRetribution.class})
class SickeningDreamsTest extends BaseCardTest {

    @Test
    @DisplayName("Discards X cards and deals X damage to each creature and player")
    void discardsXAndDealsDamageToCreaturesAndPlayers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new AvenTrooper());
        Permanent angelOfRetribution = harness.addToBattlefieldAndReturn(player2, new AngelOfRetribution());
        harness.setHand(player1, List.of(
                new SickeningDreams(), new AvenTrooper(), new AvenTrooper()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorceryWithDiscards(player1, 0, 2, (UUID) null, List.of(1, 2));
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
        harness.assertNotOnBattlefield(player1, "Aven Trooper");
        assertThat(angelOfRetribution.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder(
                        "Sickening Dreams", "Aven Trooper", "Aven Trooper", "Aven Trooper");
    }

    @Test
    @DisplayName("X=0 discards no cards and deals no damage")
    void zeroXDoesNothing() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new AvenTrooper());
        Permanent angelOfRetribution = harness.addToBattlefieldAndReturn(player2, new AngelOfRetribution());
        harness.setHand(player1, List.of(new SickeningDreams(), new AvenTrooper()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorceryWithDiscards(player1, 0, 0, (UUID) null, List.of());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Aven Trooper");
        assertThat(angelOfRetribution.getMarkedDamage()).isZero();
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Aven Trooper");
    }

    @Test
    @DisplayName("Casting is rejected when the hand cannot cover X discards")
    void cannotCastWithoutEnoughCardsToDiscard() {
        harness.setHand(player1, List.of(new SickeningDreams(), new AvenTrooper()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorceryWithDiscards(player1, 0, 2, (UUID) null, List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must discard 2");

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Discards are paid before resolution, using original hand indices and card count")
    void paysDiscardsBeforeResolutionFromEitherSideOfSpellInHand() {
        AvenTrooper discardedTrooper = new AvenTrooper();
        AngelOfRetribution discardedAngel = new AngelOfRetribution();
        AvenTrooper retainedTrooper = new AvenTrooper();
        harness.setHand(player1, List.of(
                discardedTrooper, new SickeningDreams(), discardedAngel, retainedTrooper));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorceryWithDiscards(player1, 1, 2, (UUID) null, List.of(0, 2));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retainedTrooper);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(discardedTrooper, discardedAngel);
        harness.assertNotInGraveyard(player1, "Sickening Dreams");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Sickening Dreams");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retainedTrooper);
    }

    @Test
    @DisplayName("X can exceed the mana paid and kills creatures controlled by both players")
    void fiveDiscardsDealFiveDamageForTwoMana() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new AngelOfRetribution());
        harness.addToBattlefield(player2, new AngelOfRetribution());
        harness.setHand(player1, List.of(new SickeningDreams(), new AvenTrooper(),
                new AvenTrooper(), new AvenTrooper(), new AvenTrooper(), new AvenTrooper()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorceryWithDiscards(player1, 0, 5, (UUID) null, List.of(1, 2, 3, 4, 5));
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
        harness.assertLife(player2, 15);
        harness.assertNotOnBattlefield(player1, "Angel of Retribution");
        harness.assertNotOnBattlefield(player2, "Angel of Retribution");
        harness.assertInGraveyard(player1, "Angel of Retribution");
        harness.assertInGraveyard(player2, "Angel of Retribution");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The spell cannot be discarded to pay its own additional cost")
    void cannotDiscardTheSpellItself() {
        harness.setHand(player1, List.of(new SickeningDreams()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorceryWithDiscards(player1, 0, 1, (UUID) null, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("pay for itself");

        harness.assertInHand(player1, "Sickening Dreams");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("One card cannot pay for multiple discards")
    void cannotDiscardTheSameCardTwice() {
        harness.setHand(player1, List.of(new SickeningDreams(), new AvenTrooper()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorceryWithDiscards(player1, 0, 2, (UUID) null, List.of(1, 1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Duplicate discard indices");

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Lethal damage to both players results in a draw")
    void simultaneousLethalDamageDrawsTheGame() {
        harness.setLife(player1, 2);
        harness.setLife(player2, 2);
        harness.setHand(player1, List.of(new SickeningDreams(), new AvenTrooper(), new AvenTrooper()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorceryWithDiscards(player1, 0, 2, (UUID) null, List.of(1, 2));
        harness.passBothPriorities();

        harness.assertLife(player1, 0);
        harness.assertLife(player2, 0);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.gameResult).isEqualTo(GameEventFact.GameResult.DRAW);
        assertThat(gd.winnerPlayerId).isNull();
    }
}
