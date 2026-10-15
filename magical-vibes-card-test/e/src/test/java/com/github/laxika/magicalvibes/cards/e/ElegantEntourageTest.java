package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.CivicGardener;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElegantEntourage.class, CivicGardener.class})
class ElegantEntourageTest extends BaseCardTest {

    @Test
    void anotherCreatureEnteringBoostsChosenCreatureAndGrantsTrampleUntilEndOfTurn() {
        Permanent entourage = harness.addToBattlefieldAndReturn(player1, new ElegantEntourage());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CivicGardener());

        harness.castFromHand(player1, new CivicGardener(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, entourage)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void enteringCreatureCanBeTargetedButEntourageCannot() {
        Permanent entourage = harness.addToBattlefieldAndReturn(player1, new ElegantEntourage());
        harness.castFromHand(player1, new CivicGardener(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent entering = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent != entourage)
                .findFirst().orElseThrow();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(entering.getId());
        harness.handlePermanentChosen(player1, entering.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, entering)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, entering)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, entering, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, entourage, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void canTargetOpponentsCreature() {
        harness.addToBattlefield(player1, new ElegantEntourage());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CivicGardener());
        harness.castFromHand(player1, new CivicGardener(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void ownEntryDoesNotTriggerAlliance() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CivicGardener());
        harness.castFromHand(player1, new ElegantEntourage(), "{3}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void opponentsCreatureEnteringDoesNotTriggerAlliance() {
        Permanent entourage = harness.addToBattlefieldAndReturn(player1, new ElegantEntourage());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CivicGardener());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new CivicGardener(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, entourage, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void repeatedEntriesStackBoostsOnTheSameTarget() {
        harness.addToBattlefield(player1, new ElegantEntourage());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CivicGardener());
        for (int i = 0; i < 2; i++) {
            harness.castFromHand(player1, new CivicGardener(), "{1}{G}");
            harness.passBothPriorities();
            harness.passBothPriorities();
            harness.handlePermanentChosen(player1, target.getId());
            harness.passBothPriorities();
        }

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
    }
}
