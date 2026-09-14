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
import java.util.Map;

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

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
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
        harness.setHand(player1, List.of(new GrizzlyBears(), new Shock()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        gs.playFlashbackSpell(gd, player1, 0, null, null, List.of(), null, null,
                List.of(), null, null, List.of(), Map.of(), List.of(), List.of(), List.of(0, 1));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
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

        assertThatThrownBy(() -> gs.playFlashbackSpell(gd, player1, 0, null, null, List.of(), null, null,
                List.of(), null, null, List.of(), Map.of(), List.of(), List.of(), List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must discard 2 cards");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(rona);
    }
}
