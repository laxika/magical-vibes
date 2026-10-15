package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.CutthroatContender;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.s.Stifle;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NightClubber.class, CutthroatContender.class, Murder.class, Stifle.class})
class NightClubberTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives only opponents' creatures -1/-1 until end of turn")
    void etbDebuffsOpponentsCreaturesOnly() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new NightClubber());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new NightClubber());
        harness.setHand(player1, List.of(new NightClubber()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Normal cast does not grant blitz haste or delayed sacrifice")
    void normalCastDoesNotUseBlitz() {
        harness.setHand(player1, List.of(new NightClubber()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent clubber = findPermanent(player1, "Night Clubber");
        assertThat(gqs.hasKeyword(gd, clubber, Keyword.HASTE)).isFalse();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(clubber);
    }

    @Test
    @DisplayName("Blitz grants haste, draws on death, and sacrifices at the next end step")
    void blitzGrantsHasteDrawsAndSacrifices() {
        harness.setHand(player1, List.of(new NightClubber()));
        harness.setLibrary(player1, List.of(new NightClubber()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        resolveAllTriggers();

        Permanent clubber = findPermanent(player1, "Night Clubber");
        assertThat(gqs.hasKeyword(gd, clubber, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(clubber);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Night Clubber");
        harness.assertInHand(player1, "Night Clubber");
    }

    @Test
    void debuffKillsOneToughnessCreaturesAndDoesNotAffectLaterEntrants() {
        harness.addToBattlefield(player2, new CutthroatContender());
        Permanent survivor = harness.addToBattlefieldAndReturn(player2, new NightClubber());
        harness.setHand(player1, List.of(new NightClubber()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Cutthroat Contender");
        assertThat(gqs.getEffectiveToughness(gd, survivor)).isEqualTo(1);
        Permanent lateEntrant = harness.addToBattlefieldAndReturn(player2, new CutthroatContender());
        assertThat(gqs.getEffectiveToughness(gd, lateEntrant)).isEqualTo(1);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.CLEANUP);
        assertThat(gqs.getEffectivePower(gd, survivor)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, survivor)).isEqualTo(2);
    }

    @Test
    void blitzHasHasteBeforeItsEnterTriggerResolves() {
        harness.setHand(player1, List.of(new NightClubber()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        Permanent clubber = findPermanent(player1, "Night Clubber");
        assertThat(gqs.hasKeyword(gd, clubber, Keyword.HASTE)).isTrue();
    }

    @Test
    void counteringEnterTriggerDoesNotPreventBlitzSacrificeOrDeathDraw() {
        harness.setHand(player1, List.of(new NightClubber()));
        harness.setLibrary(player1, List.of(new Murder()));
        harness.setHand(player2, List.of(new Stifle()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        harness.castInstant(player2, 0, gd.stack.getLast().getCard().getId());
        resolveAllTriggers();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Night Clubber");
        harness.assertInHand(player1, "Murder");
    }

    @Test
    void blitzHastePersistsAfterCounteringTheDelayedSacrifice() {
        harness.setHand(player1, List.of(new NightClubber(), new Stifle()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        resolveAllTriggers();
        Permanent clubber = findPermanent(player1, "Night Clubber");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, gd.stack.getLast().getCard().getId());
        resolveAllTriggers();
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(clubber);
        assertThat(gqs.hasKeyword(gd, clubber, Keyword.HASTE)).isTrue();
    }

    @Test
    void blitzDrawsWhenDestroyedBeforeItsEnterTriggerResolves() {
        harness.setHand(player1, List.of(new NightClubber()));
        harness.setLibrary(player1, List.of(new CutthroatContender()));
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Night Clubber"));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Night Clubber");
        harness.assertInHand(player1, "Cutthroat Contender");
    }

    @Test
    void normallyCastCreatureDoesNotDrawWhenDestroyed() {
        harness.setHand(player1, List.of(new NightClubber()));
        harness.setLibrary(player1, List.of(new CutthroatContender()));
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Night Clubber"));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Night Clubber");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
}
