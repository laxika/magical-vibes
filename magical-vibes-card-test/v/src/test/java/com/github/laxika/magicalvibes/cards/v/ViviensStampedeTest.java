package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ViviensStampede.class, GrizzlyBears.class, VedalkenOrrery.class})
class ViviensStampedeTest extends BaseCardTest {

    @Test
    @DisplayName("Vivien's Stampede gives your creatures vigilance, trample, and melee")
    void grantsCombatKeywordsToOwnCreatures() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        harness.castFromHand(player1, new ViviensStampede(), "{4}{G}{G}");
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.MELEE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.MELEE)).isFalse();
    }

    @Test
    @DisplayName("Melee boosts attackers and the delayed ability draws after combat damage")
    void meleeBoostsAndDelayedDrawFiresAtNextMainPhase() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        harness.castFromHand(player1, new ViviensStampede(), "{4}{G}{G}");
        harness.passBothPriorities();
        int handSizeAfterCasting = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
        assertThat(attacker.getEffectivePower()).isEqualTo(3);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeAfterCasting);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeAfterCasting + 1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.getEffectivePower()).isEqualTo(2);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.MELEE)).isFalse();
    }

    @Test
    void doesNotGrantKeywordsToCreaturesEnteringAfterResolution() {
        harness.castFromHand(player1, new ViviensStampede(), "{4}{G}{G}");
        harness.passBothPriorities();

        Permanent lateCreature = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, lateCreature, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, lateCreature, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, lateCreature, Keyword.MELEE)).isFalse();
    }

    @Test
    void drawsOnceForMultipleCreaturesDamagingTheSamePlayer() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.castFromHand(player1, new ViviensStampede(), "{4}{G}{G}");
        harness.passBothPriorities();
        int handSize = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(0, 1));
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
        harness.passBothPriorities();

        harness.assertLife(player2, 14);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
    }

    @Test
    void delayedAbilityTriggersEvenWhenNoPlayerWasDealtCombatDamage() {
        harness.castFromHand(player1, new ViviensStampede(), "{4}{G}{G}");
        harness.passBothPriorities();
        int handSize = gd.playerHands.get(player1.getId()).size();

        harness.passUntilWithNoAttackers(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
    }

    @Test
    void drawsAtOpponentsNextMainPhaseWhenCastWithFlashPermission() {
        harness.addToBattlefield(player1, new VedalkenOrrery());
        addCreatureReady(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new ViviensStampede(), "{4}{G}{G}");
        harness.passBothPriorities();
        int handSize = gd.playerHands.get(player1.getId()).size();

        declareAttackers(player2, List.of(0));
        harness.passUntil(player2, TurnStep.POSTCOMBAT_MAIN);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
    }
}
