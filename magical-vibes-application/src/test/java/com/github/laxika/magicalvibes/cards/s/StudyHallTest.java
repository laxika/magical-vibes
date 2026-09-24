package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
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
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StudyHall.class, GrizzlyBears.class})
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
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        Card commander = addCommanderToCommandZone();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactlyInAnyOrder("WHITE", "BLUE", "BLACK", "RED", "GREEN");
        harness.handleListChoice(player1, ManaColor.BLUE.name());

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
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getCard().getName().equals("Study Hall"));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    private Permanent addReadyStudyHall() {
        Permanent studyHall = new Permanent(new StudyHall());
        studyHall.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(studyHall);
        return studyHall;
    }

    private Card addCommanderToCommandZone() {
        Card commander = new Card();
        commander.setName("Test Commander");
        commander.setType(CardType.CREATURE);
        commander.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        commander.setManaCost("{U}");
        commander.setPower(2);
        commander.setToughness(2);
        commander.setOwnerId(player1.getId());
        commander.freeze();
        gd.format = com.github.laxika.magicalvibes.model.DeckFormat.COMMANDER;
        gd.makeCommander(player1.getId(), commander);
        gd.playerCommandZones.put(player1.getId(), new ArrayList<>(List.of(commander)));
        return commander;
    }
}
