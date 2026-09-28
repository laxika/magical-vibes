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

@CardUsed({ViviensStampede.class, GrizzlyBears.class})
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
}
