package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.DeckFormat;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(OpalPalace.class)
class OpalPalaceTest extends BaseCardTest {

    @Test
    void addsColorlessMana() {
        harness.addToBattlefield(player1, new OpalPalace());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void commanderEntersWithOneCounterOnFirstCast() {
        Card commander = commander();
        harness.addToBattlefield(player1, new OpalPalace());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, null);

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).containsExactlyInAnyOrder("RED", "BLUE");
        harness.handleListChoice(player1, ManaColor.RED.name());

        castCommander(commander);
        harness.passBothPriorities();

        Permanent entered = findPermanent(player1, commander.getName());
        assertThat(entered.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void commanderEntersWithTwoCountersAfterOnePreviousCommandZoneCast() {
        Card commander = commander();
        gd.commanderTaxByCardId.put(commander.getId(), 2);
        harness.addToBattlefield(player1, new OpalPalace());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        castCommander(commander);
        harness.passBothPriorities();

        Permanent entered = findPermanent(player1, commander.getName());
        assertThat(entered.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private Card commander() {
        Card card = new Card();
        card.setName("Test Commander");
        card.setType(CardType.CREATURE);
        card.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        card.setManaCost("{1}");
        card.setColorIdentity(List.of(CardColor.RED, CardColor.BLUE));
        card.setPower(2);
        card.setToughness(2);
        card.setOwnerId(player1.getId());
        card.freeze();
        gd.format = DeckFormat.COMMANDER;
        gd.makeCommander(player1.getId(), card);
        gd.playerCommandZones.put(player1.getId(), new ArrayList<>(List.of(card)));
        gd.currentStep = TurnStep.PRECOMBAT_MAIN;
        gd.activePlayerId = player1.getId();
        gd.priorityPassedBy.clear();
        return card;
    }

    private void castCommander(Card commander) {
        gs.castCommander(gd, player1, commander.getId(),
                () -> gs.playCard(gd, player1, 0, null, null, null));
    }
}
