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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(OpalPalace.class)
class OpalPalaceTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability adds a colorless mana")
    void addsColorlessMana() {
        harness.addToBattlefield(player1, new OpalPalace());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("The second ability is limited to the commander's color identity")
    void producesManaInCommandersColorIdentity() {
        addCommanderToCommandZone();
        harness.addToBattlefield(player1, new OpalPalace());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).containsExactlyInAnyOrder("RED", "BLUE");

        assertThatThrownBy(() -> harness.handleListChoice(player1, ManaColor.GREEN.name()))
                .isInstanceOf(IllegalArgumentException.class);

        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getCommanderCastCounterGrantingManaTotal())
                .isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Mana spent to cast a commander gives counters equal to its current cast count")
    void commanderEntersWithCurrentCastCountCounters() {
        Card commander = addCommanderToCommandZone();
        harness.addToBattlefield(player1, new OpalPalace());
        harness.addToBattlefield(player1, new OpalPalace());

        activateForCommanderCast(0);
        gs.castCommander(gd, player1, commander.getId(),
                () -> gs.playCard(gd, player1, 0, null, null, null));
        harness.passBothPriorities();

        Permanent firstEntry = findPermanent(player1, commander.getName());
        assertThat(firstEntry.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        gd.playerBattlefields.get(player1.getId()).remove(firstEntry);
        gd.playerCommandZones.get(player1.getId()).add(commander);
        gd.priorityPassedBy.clear();

        activateForCommanderCast(1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        gs.castCommander(gd, player1, commander.getId(),
                () -> gs.playCard(gd, player1, 0, null, null, null));
        harness.passBothPriorities();

        Permanent secondEntry = findPermanent(player1, commander.getName());
        assertThat(secondEntry.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private void activateForCommanderCast(int permanentIndex) {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, permanentIndex, 1, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());
    }

    private Card addCommanderToCommandZone() {
        Card commander = new Card();
        commander.setName("Test Commander");
        commander.setType(CardType.CREATURE);
        commander.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        commander.setManaCost("{R}");
        commander.setColorIdentity(List.of(CardColor.RED, CardColor.BLUE));
        commander.setPower(2);
        commander.setToughness(2);
        commander.setOwnerId(player1.getId());
        commander.freeze();
        gd.format = DeckFormat.COMMANDER;
        gd.makeCommander(player1.getId(), commander);
        gd.playerCommandZones.put(player1.getId(), new ArrayList<>(List.of(commander)));
        return commander;
    }
}
