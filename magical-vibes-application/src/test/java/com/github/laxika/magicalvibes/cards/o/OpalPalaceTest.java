package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.DeckFormat;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
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
    void tapsForColorlessMana() {
        harness.addToBattlefield(player1, new OpalPalace());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void producesManaInTheCommandersColorIdentity() {
        Card commander = commander();
        harness.addToBattlefield(player1, new OpalPalace());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).containsExactly("WHITE", "BLUE", "RED");
        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getCommanderCounterGrantingManaTotal()).isEqualTo(1);
        assertThat(commander.getColorIdentity()).containsExactly(
                CardColor.WHITE, CardColor.BLUE, CardColor.RED);
    }

    @Test
    void commanderEntersWithCountersEqualToItsCommandZoneCastCount() {
        Card commander = commander();
        Permanent opal = harness.addToBattlefieldAndReturn(player1, new OpalPalace());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());

        gs.castCommander(gd, player1, commander.getId(),
                () -> gs.playCard(gd, player1, 0, null, null, null));
        harness.passBothPriorities();

        Permanent entered = findPermanent(player1, commander.getName());
        assertThat(entered.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.commanderTaxByCardId.get(commander.getId())).isEqualTo(2);
        assertThat(opal.isTapped()).isTrue();
    }

    private Card commander() {
        Card commander = new Card();
        commander.setName("Test Commander");
        commander.setType(CardType.CREATURE);
        commander.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        commander.setManaCost("{1}");
        commander.setColors(List.of(CardColor.WHITE, CardColor.BLUE, CardColor.RED));
        commander.setColorIdentity(List.of(CardColor.WHITE, CardColor.BLUE, CardColor.RED));
        commander.setPower(2);
        commander.setToughness(2);
        commander.setOwnerId(player1.getId());
        commander.freeze();

        gd.format = DeckFormat.COMMANDER;
        gd.makeCommander(player1.getId(), commander);
        gd.playerCommandZones.put(player1.getId(), new ArrayList<>(List.of(commander)));
        gd.currentStep = com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN;
        gd.activePlayerId = player1.getId();
        gd.priorityPassedBy.clear();
        return commander;
    }
}
