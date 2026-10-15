package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BodyDropper;
import com.github.laxika.magicalvibes.cards.c.CorruptCourtOfficial;
import com.github.laxika.magicalvibes.cards.s.Stifle;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({WorkshopWarchief.class, CorruptCourtOfficial.class, BodyDropper.class})
class WorkshopWarchiefTest extends BaseCardTest {

    @Test
    @DisplayName("Normal cast gains life and creates a Rhino Warrior on death")
    void normalCastGainsLifeAndCreatesTokenOnDeath() {
        harness.addToBattlefield(player1, new BodyDropper());
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new WorkshopWarchief()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(13);
        Permanent warchief = findPermanent(player1, "Workshop Warchief");
        assertThat(gqs.hasKeyword(gd, warchief, Keyword.HASTE)).isFalse();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, warchief.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Workshop Warchief");
        assertThat(findPermanents(player1, "Rhino Warrior")).hasSize(1);
    }

    @Test
    @DisplayName("Blitz grants haste, gains life, draws on death, and creates a Rhino Warrior")
    void blitzGrantsHasteGainsLifeDrawsAndCreatesToken() {
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new WorkshopWarchief()));
        harness.setLibrary(player1, List.of(new CorruptCourtOfficial()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        resolveAllTriggers();

        Permanent warchief = findPermanent(player1, "Workshop Warchief");
        assertThat(gd.getLife(player1.getId())).isEqualTo(13);
        assertThat(gqs.hasKeyword(gd, warchief, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(warchief);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Workshop Warchief");
        harness.assertInHand(player1, "Corrupt Court Official");
        assertThat(findPermanents(player1, "Rhino Warrior")).hasSize(1);
    }

    @Test
    @DisplayName("Blitz grants haste before the life gain trigger resolves")
    void blitzHasHasteImmediately() {
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new WorkshopWarchief()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Workshop Warchief"), Keyword.HASTE)).isTrue();
    }

    @Test
    @CardUsed(Stifle.class)
    @DisplayName("Countering the life gain trigger does not prevent the blitz sacrifice")
    void counteredLifeGainDoesNotPreventBlitzSacrifice() {
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new WorkshopWarchief()));
        harness.setLibrary(player1, List.of(new CorruptCourtOfficial(), new WorkshopWarchief()));
        harness.setHand(player2, List.of(new Stifle()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        var lifeGainTriggerId = gd.stack.getLast().getTargetableId();
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, lifeGainTriggerId);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Workshop Warchief");
        harness.assertInHand(player1, "Corrupt Court Official");
        assertThat(findPermanents(player1, "Rhino Warrior")).hasSize(1);
    }

    @Test
    @CardUsed(Stifle.class)
    @DisplayName("Blitz haste persists after a countered sacrifice and cleanup")
    void blitzHastePersistsAcrossCleanup() {
        harness.setHand(player1, List.of(new WorkshopWarchief()));
        harness.setHand(player2, List.of(new Stifle()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        resolveAllTriggers();

        harness.passUntil(TurnStep.END_STEP);
        harness.addMana(player2, ManaColor.BLUE, 1);
        var sacrificeTriggerId = gd.stack.getLast().getTargetableId();
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, sacrificeTriggerId);
        resolveAllTriggers();
        harness.passUntil(player2, TurnStep.UPKEEP);

        Permanent warchief = findPermanent(player1, "Workshop Warchief");
        assertThat(gqs.hasKeyword(gd, warchief, Keyword.HASTE)).isTrue();
        assertThat(findPermanents(player1, "Rhino Warrior")).isEmpty();
    }

    @Test
    @DisplayName("Blitz death before life gain resolves still draws and creates the correct Rhino")
    void blitzDeathBeforeLifeGainResolves() {
        harness.addToBattlefield(player1, new BodyDropper());
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new WorkshopWarchief()));
        harness.setLibrary(player1, List.of(new CorruptCourtOfficial(), new WorkshopWarchief()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        Permanent warchief = findPermanent(player1, "Workshop Warchief");
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, warchief.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(13);
        harness.assertInGraveyard(player1, "Workshop Warchief");
        harness.assertInHand(player1, "Corrupt Court Official");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(findPermanents(player1, "Rhino Warrior")).hasSize(1);
        Permanent rhino = findPermanent(player1, "Rhino Warrior");
        assertThat(gqs.getEffectivePower(gd, rhino)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, rhino)).isEqualTo(4);
        assertThat(rhino.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(rhino.getCard().getSubtypes()).containsExactly(CardSubtype.RHINO, CardSubtype.WARRIOR);
        assertThat(rhino.isTapped()).isFalse();

        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(findPermanents(player1, "Rhino Warrior")).hasSize(1);
    }

    @Test
    @DisplayName("Death without paying blitz creates a Rhino without drawing")
    void normalDeathDoesNotDraw() {
        harness.addToBattlefield(player1, new BodyDropper());
        harness.setHand(player1, List.of(new WorkshopWarchief()));
        harness.setLibrary(player1, List.of(new CorruptCourtOfficial(), new WorkshopWarchief()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, findPermanent(player1, "Workshop Warchief").getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Workshop Warchief");
        assertThat(findPermanents(player1, "Rhino Warrior")).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Normally cast Warchief survives the end step without drawing")
    void normalCastSurvivesEndStep() {
        harness.setHand(player1, List.of(new WorkshopWarchief()));
        harness.setLibrary(player1, List.of(new CorruptCourtOfficial()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Workshop Warchief");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(findPermanents(player1, "Rhino Warrior")).isEmpty();
    }
}
