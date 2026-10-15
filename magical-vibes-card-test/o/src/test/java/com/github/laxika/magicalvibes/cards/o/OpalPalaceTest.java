package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GoreclawTerrorOfQalSisma;
import com.github.laxika.magicalvibes.cards.k.KozilekTheGreatDistortion;
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

@CardUsed({OpalPalace.class, GoreclawTerrorOfQalSisma.class, KozilekTheGreatDistortion.class})
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
    void commanderEntersWithCountersEqualToCommandZoneCastsIncludingCurrentCast() {
        GoreclawTerrorOfQalSisma commander = prepareCommander();
        gd.commanderTaxByCardId.put(commander.getId(), 4);
        harness.addToBattlefield(player1, new OpalPalace());

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        harness.addMana(player1, ManaColor.COLORLESS, 7);
        gs.castCommander(gd, player1, commander.getId(),
                () -> gs.playCard(gd, player1, 0, null, null, null));
        harness.passBothPriorities();

        var permanent = findPermanent(player1, commander.getName());
        assertThat(permanent.getCounters().get(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void firstCommandZoneCastEntersWithOneCounter() {
        var commander = prepareCommander();
        producePalaceMana();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.castCommander(gd, player1, commander.getId(),
                () -> gs.playCard(gd, player1, 0, null, null, null));
        harness.passBothPriorities();

        assertThat(findPermanent(player1, commander.getName()).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
    }

    @Test
    void colorlessManaFromFirstAbilityDoesNotGrantCounters() {
        var commander = prepareCommander();
        harness.addToBattlefield(player1, new OpalPalace());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        gs.castCommander(gd, player1, commander.getId(),
                () -> gs.playCard(gd, player1, 0, null, null, null));
        harness.passBothPriorities();

        assertThat(findPermanent(player1, commander.getName()).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
    }

    @Test
    void commanderCastFromHandReceivesCountersForPreviousCommandZoneCasts() {
        var commander = prepareCommander();
        gd.commanderTaxByCardId.put(commander.getId(), 4);
        gd.playerCommandZones.get(player1.getId()).clear();
        harness.setHand(player1, List.of(commander));
        producePalaceMana();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, commander.getName()).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(2);
    }

    @Test
    void commanderCastFromHandWithoutCommandZoneCastsReceivesNoCounters() {
        var commander = prepareCommander();
        gd.playerCommandZones.get(player1.getId()).clear();
        harness.setHand(player1, List.of(commander));
        producePalaceMana();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, commander.getName()).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
    }

    @Test
    void manaCanCastNonCommanderWithoutGrantingCounters() {
        prepareCommander();
        var creature = new GoreclawTerrorOfQalSisma();
        harness.setHand(player1, List.of(creature));
        producePalaceMana();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, creature.getName()).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
    }

    @Test
    void spendingManaFromTwoPalacesAddsBothCounterGrants() {
        var commander = prepareCommander();
        gd.commanderTaxByCardId.put(commander.getId(), 4);
        producePalaceMana();
        harness.addToBattlefield(player1, new OpalPalace());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 1, 1, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        gs.castCommander(gd, player1, commander.getId(),
                () -> gs.playCard(gd, player1, 0, null, null, null));
        harness.passBothPriorities();

        assertThat(findPermanent(player1, commander.getName()).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(6);
    }

    @Test
    void colorlessCommanderProducesNoManaFromSecondAbility() {
        gd.format = DeckFormat.COMMANDER;
        gd.makeCommander(player1.getId(), new KozilekTheGreatDistortion());
        harness.addToBattlefield(player1, new OpalPalace());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        for (ManaColor color : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isZero();
        }
        assertThat(findPermanent(player1, "Opal Palace").isTapped()).isTrue();
    }

    @Test
    void noCommanderProducesNoManaFromSecondAbility() {
        harness.addToBattlefield(player1, new OpalPalace());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        for (ManaColor color : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isZero();
        }
        assertThat(findPermanent(player1, "Opal Palace").isTapped()).isTrue();
    }

    private void producePalaceMana() {
        harness.addToBattlefield(player1, new OpalPalace());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());
    }

    private GoreclawTerrorOfQalSisma prepareCommander() {
        GoreclawTerrorOfQalSisma commander = new GoreclawTerrorOfQalSisma();
        gd.format = DeckFormat.COMMANDER;
        gd.makeCommander(player1.getId(), commander);
        gd.playerCommandZones.put(player1.getId(), new ArrayList<>(List.of(commander)));
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.clearPriorityPassed();
        return commander;
    }
}
