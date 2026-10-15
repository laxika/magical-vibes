package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.j.JadarGhoulcallerOfNephalia;
import com.github.laxika.magicalvibes.cards.k.KamiOfAncientLaw;
import com.github.laxika.magicalvibes.cards.t.TanaTheBloodsower;
import com.github.laxika.magicalvibes.cards.t.ThrasiosTritonHero;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DeckFormat;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StudyHall.class, KamiOfAncientLaw.class, JadarGhoulcallerOfNephalia.class})
class StudyHallTest extends BaseCardTest {

    @Test
    void colorlessAbilityAddsColorlessMana() {
        addReadyStudyHall();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void anyColorManaSpentOnCommanderCastScriesByCommanderCastCount() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addReadyStudyHall();
        harness.setLibrary(player1, List.of(new KamiOfAncientLaw(), new KamiOfAncientLaw()));
        Card commander = addCommanderToCommandZone();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactlyInAnyOrder("WHITE", "BLUE", "BLACK", "RED", "GREEN");
        harness.handleListChoice(player1, ManaColor.BLACK.name());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        gs.castCommander(gd, player1, commander.getId(),
                () -> gs.playCard(gd, player1, 0, null, null, null));

        assertThat(gd.commanderCastsFromCommandZoneThisGame.get(player1.getId())).isEqualTo(1);
        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getCard().getName().equals("Study Hall"));

        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(1);
    }

    @Test
    void manaDoesNotTriggerScryForANonCommanderSpell() {
        addReadyStudyHall();
        harness.setHand(player1, List.of(new KamiOfAncientLaw()));

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, ManaColor.WHITE.name());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getCard().getName().equals("Study Hall"));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    void coloredManaTriggersForCommanderCastFromHandAfterEarlierCommandZoneCast() {
        addReadyStudyHall();
        Card commander = addCommanderToCommandZone();
        gd.playerCommandZones.get(player1.getId()).clear();
        gd.commanderCastsFromCommandZoneThisGame.put(player1.getId(), 1);
        gd.commanderTaxByCardId.put(commander.getId(), 2);
        harness.setHand(player1, List.of(commander));
        harness.setLibrary(player1, List.of(new KamiOfAncientLaw()));

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, ManaColor.BLACK.name());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(1);
    }

    @Test
    void coloredManaStillTriggersAfterStudyHallLeavesTheBattlefield() {
        Permanent studyHall = addReadyStudyHall();
        Card commander = addCommanderToCommandZone();
        harness.setLibrary(player1, List.of(new KamiOfAncientLaw()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, ManaColor.BLACK.name());
        gd.playerBattlefields.get(player1.getId()).remove(studyHall);
        harness.setGraveyard(player1, List.of(studyHall.getCard()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        gs.castCommander(gd, player1, commander.getId(),
                () -> gs.playCard(gd, player1, 0, null, null, null));
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(1);
    }

    @Test
    void colorlessManaSpentOnCommanderDoesNotTriggerScry() {
        addReadyStudyHall();
        Card commander = addCommanderToCommandZone();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.addMana(player1, ManaColor.BLACK, 1);

        gs.castCommander(gd, player1, commander.getId(),
                () -> gs.playCard(gd, player1, 0, null, null, null));

        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getCard().getName().equals("Study Hall"));
    }

    @Test
    @CardUsed({TanaTheBloodsower.class, ThrasiosTritonHero.class})
    void partnerCommandersHaveSeparateCastCountsForScry() {
        addReadyStudyHall();
        Card firstCommander = new TanaTheBloodsower();
        Card secondCommander = new ThrasiosTritonHero();
        gd.format = DeckFormat.COMMANDER;
        gd.playerCommanders.put(player1.getId(), new ArrayList<>(List.of(firstCommander, secondCommander)));
        gd.playerCommandZones.put(player1.getId(), new ArrayList<>(List.of(secondCommander)));
        gd.commanderTaxByCardId.put(firstCommander.getId(), 2);
        gd.commanderTaxByCardId.put(secondCommander.getId(), 0);
        gd.commanderCastsFromCommandZoneThisGame.put(player1.getId(), 1);
        harness.addToBattlefield(player1, firstCommander);
        harness.setLibrary(player1, List.of(new KamiOfAncientLaw(), new KamiOfAncientLaw()));

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());
        harness.addMana(player1, ManaColor.GREEN, 1);
        gs.castCommander(gd, player1, secondCommander.getId(),
                () -> gs.playCard(gd, player1, 0, null, null, null));
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(1);
    }

    private Permanent addReadyStudyHall() {
        return harness.addToBattlefieldAndReturn(player1, new StudyHall());
    }

    private Card addCommanderToCommandZone() {
        Card commander = new JadarGhoulcallerOfNephalia();
        gd.format = DeckFormat.COMMANDER;
        gd.makeCommander(player1.getId(), commander);
        gd.playerCommandZones.put(player1.getId(), new ArrayList<>(List.of(commander)));
        return commander;
    }
}
