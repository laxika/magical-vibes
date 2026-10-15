package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AnjeFalkenrath;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.DeckFormat;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkyfirePhoenix.class, GrizzlyBears.class, AnjeFalkenrath.class})
class SkyfirePhoenixTest extends BaseCardTest {

    @Test
    void returnsFromGraveyardWhenCommanderIsCast() {
        prepareMainPhase();
        SkyfirePhoenix phoenix = new SkyfirePhoenix();
        harness.setGraveyard(player1, List.of(phoenix));
        Card commander = addCommanderToCommandZone();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        gs.castCommander(gd, player1, commander.getId(),
                () -> gs.playCard(gd, player1, 0, null, null, null));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(phoenix.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(phoenix.getId()));
    }

    @Test
    void doesNotReturnWhenNonCommanderSpellIsCast() {
        prepareMainPhase();
        SkyfirePhoenix phoenix = new SkyfirePhoenix();
        harness.setGraveyard(player1, List.of(phoenix));
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(phoenix.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(phoenix.getId()));
    }

    @Test
    void returnsWhenCommanderIsCastFromHand() {
        prepareMainPhase();
        SkyfirePhoenix phoenix = new SkyfirePhoenix();
        harness.setGraveyard(player1, List.of(phoenix));
        AnjeFalkenrath commander = new AnjeFalkenrath();
        commander.setOwnerId(player1.getId());
        gd.format = DeckFormat.COMMANDER;
        gd.makeCommander(player1.getId(), commander);

        harness.castFromHand(player1, commander, "{1}{B}{R}");
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(phoenix.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(phoenix.getId()));
    }

    @Test
    void doesNotReturnWhenOpponentCastsTheirCommander() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        SkyfirePhoenix phoenix = new SkyfirePhoenix();
        harness.setGraveyard(player1, List.of(phoenix));
        AnjeFalkenrath commander = new AnjeFalkenrath();
        commander.setOwnerId(player2.getId());
        gd.format = DeckFormat.COMMANDER;
        gd.makeCommander(player2.getId(), commander);
        gd.playerCommandZones.put(player2.getId(), new ArrayList<>(List.of(commander)));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        gs.castCommander(gd, player2, commander.getId(),
                () -> harness.castCreature(player2, 0));
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(phoenix);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void returnsEachPhoenixInGraveyardWhenCommanderIsCast() {
        prepareMainPhase();
        SkyfirePhoenix first = new SkyfirePhoenix();
        SkyfirePhoenix second = new SkyfirePhoenix();
        harness.setGraveyard(player1, List.of(first, second));
        AnjeFalkenrath commander = new AnjeFalkenrath();
        commander.setOwnerId(player1.getId());
        gd.format = DeckFormat.COMMANDER;
        gd.makeCommander(player1.getId(), commander);
        gd.playerCommandZones.put(player1.getId(), new ArrayList<>(List.of(commander)));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        gs.castCommander(gd, player1, commander.getId(),
                () -> harness.castCreature(player1, 0));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(first.getId(), second.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private Card addCommanderToCommandZone() {
        Card commander = new Card();
        commander.setName("Test Commander");
        commander.setType(CardType.CREATURE);
        commander.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        commander.setManaCost("{1}");
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
