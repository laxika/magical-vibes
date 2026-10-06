package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RonaSheoldredsFaithful.class, Divination.class, GrizzlyBears.class, Shock.class})
class RonaSheoldredsFaithfulTest extends BaseCardTest {

    @Test
    void eachInstantAndSorceryCastMakesEachOpponentLoseOneLife() {
        harness.addToBattlefield(player1, new RonaSheoldredsFaithful());
        harness.setHand(player1, List.of(new Shock(), new Divination()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    void creatureSpellsDoNotTriggerRona() {
        harness.addToBattlefield(player1, new RonaSheoldredsFaithful());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void mayCastFromGraveyardByDiscardingTwoCards() {
        RonaSheoldredsFaithful rona = new RonaSheoldredsFaithful();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(rona));
        var discardedCards = List.of(new GrizzlyBears(), new Shock());
        harness.setHand(player1, discardedCards);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFromGraveyardWithDiscards(player1, 0, 0, List.of(1));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrderElementsOf(discardedCards);
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(
                permanent -> permanent.getCard() == rona);
    }

    @Test
    void cannotCastFromGraveyardWithoutBothDiscardSelections() {
        RonaSheoldredsFaithful rona = new RonaSheoldredsFaithful();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(rona));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromGraveyardWithDiscards(player1, 0, 0, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must discard exactly 2 cards");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(rona);
    }

    @Test
    void opponentsInstantDoesNotTriggerRona() {
        harness.addToBattlefield(player1, new RonaSheoldredsFaithful());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void castingFromHandDoesNotRequireDiscarding() {
        harness.setHand(player1, List.of(new RonaSheoldredsFaithful()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Rona, Sheoldred's Faithful");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void graveyardCastingStillRequiresManaAndDoesNotDiscardOnRejection() {
        RonaSheoldredsFaithful rona = new RonaSheoldredsFaithful();
        var hand = List.of(new RonaSheoldredsFaithful(), new RonaSheoldredsFaithful());
        harness.setGraveyard(player1, List.of(rona));
        harness.setHand(player1, hand);

        assertThatThrownBy(() -> harness.castFromGraveyardWithDiscards(player1, 0, 0, List.of(1)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(hand);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(rona);
    }

    @Test
    void cannotDiscardTheSameCardTwice() {
        RonaSheoldredsFaithful rona = new RonaSheoldredsFaithful();
        var hand = List.of(new RonaSheoldredsFaithful(), new RonaSheoldredsFaithful());
        harness.setGraveyard(player1, List.of(rona));
        harness.setHand(player1, hand);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromGraveyardWithDiscards(player1, 0, 0, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Duplicate cards");

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(hand);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(rona);
    }

    @Test
    void graveyardPermissionDoesNotAllowCastingDuringCombat() {
        RonaSheoldredsFaithful rona = new RonaSheoldredsFaithful();
        var hand = List.of(new RonaSheoldredsFaithful(), new RonaSheoldredsFaithful());
        harness.setGraveyard(player1, List.of(rona));
        harness.setHand(player1, hand);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.castFromGraveyardWithDiscards(player1, 0, 0, List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery-speed");

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(hand);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(rona);
    }
}
