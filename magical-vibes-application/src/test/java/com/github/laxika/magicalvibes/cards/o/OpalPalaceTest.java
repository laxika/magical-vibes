package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.DeckFormat;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OpalPalace.class, GrizzlyBears.class})
class OpalPalaceTest extends BaseCardTest {

    @Test
    void addsColorlessMana() {
        harness.addToBattlefield(player1, new OpalPalace());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void producesManaInCommandersColorIdentity() {
        prepareCommander();
        harness.addToBattlefield(player1, new OpalPalace());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).containsExactly("GREEN");

        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void commanderEntersWithCountersEqualToPriorCommandZoneCasts() {
        GrizzlyBears commander = prepareCommander();
        gd.commanderTaxByCardId.put(commander.getId(), 4);
        harness.addToBattlefield(player1, new OpalPalace());

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        harness.addMana(player1, ManaColor.COLORLESS, 5);
        gs.castCommander(gd, player1, commander.getId(),
                () -> gs.playCard(gd, player1, 0, null, null, null));
        harness.passBothPriorities();

        var permanent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(candidate -> candidate.getCard().getId().equals(commander.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(permanent.getCounters().get(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private GrizzlyBears prepareCommander() {
        GrizzlyBears commander = new GrizzlyBears();
        gd.format = DeckFormat.COMMANDER;
        gd.makeCommander(player1.getId(), commander);
        gd.playerCommandZones.put(player1.getId(), new ArrayList<>(List.of(commander)));
        gd.currentStep = TurnStep.PRECOMBAT_MAIN;
        gd.activePlayerId = player1.getId();
        gd.priorityPassedBy.clear();
        return commander;
    }
}
