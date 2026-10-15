package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SyrJoshuaAndSyrSaxon.class, TurnToFrog.class, SparkDouble.class})
class SyrJoshuaAndSyrSaxonTest extends BaseCardTest {

    @Test
    @DisplayName("A lone copy does not grant Battle Cry to itself")
    void loneCopyDoesNotGrantBattleCry() {
        Permanent syr = harness.addToBattlefieldAndReturn(player1, new SyrJoshuaAndSyrSaxon());

        assertThat(gqs.hasKeyword(gd, syr, Keyword.BATTLE_CRY)).isFalse();
    }

    @Test
    @DisplayName("Each other copy under the same controller has Battle Cry")
    void otherCopyGainsBattleCry() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SyrJoshuaAndSyrSaxon());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SyrJoshuaAndSyrSaxon());

        assertThat(gqs.hasKeyword(gd, first, Keyword.BATTLE_CRY)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.BATTLE_CRY)).isTrue();
    }

    @Test
    @DisplayName("A copy controlled by an opponent does not grant Battle Cry across controllers")
    void doesNotGrantBattleCryAcrossControllers() {
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new SyrJoshuaAndSyrSaxon());
        Permanent ours = harness.addToBattlefieldAndReturn(player1, new SyrJoshuaAndSyrSaxon());

        assertThat(gqs.hasKeyword(gd, theirs, Keyword.BATTLE_CRY)).isFalse();
        assertThat(gqs.hasKeyword(gd, ours, Keyword.BATTLE_CRY)).isFalse();
    }

    @Test
    @DisplayName("Exactly two copies under one controller survive the legend rule")
    void exactlyTwoCopiesSurviveLegendRule() {
        harness.addToBattlefieldAndReturn(player1, new SyrJoshuaAndSyrSaxon());
        harness.addToBattlefieldAndReturn(player1, new SyrJoshuaAndSyrSaxon());

        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.permanentChoiceContext()).isNull();
    }

    @Test
    @DisplayName("A third copy under one controller restores the legend rule")
    void thirdCopyRestoresLegendRule() {
        harness.addToBattlefieldAndReturn(player1, new SyrJoshuaAndSyrSaxon());
        harness.addToBattlefieldAndReturn(player1, new SyrJoshuaAndSyrSaxon());
        harness.addToBattlefieldAndReturn(player1, new SyrJoshuaAndSyrSaxon());

        harness.runStateBasedActions();

        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.LegendRule.class);
    }

    @Test
    void doubleTeamConjuresOneCopyAndIsLostByBothCards() {
        Permanent syr = addCreatureReady(player1, new SyrJoshuaAndSyrSaxon());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, syr, Keyword.DOUBLE_TEAM)).isFalse();
        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card instanceof SyrJoshuaAndSyrSaxon)
                .singleElement()
                .satisfies(card -> assertThat(card.hasKeyword(Keyword.DOUBLE_TEAM)).isFalse());
    }

    @Test
    void bothAttackingCopiesBoostEachOtherButNotThemselves() {
        Permanent first = addCreatureReady(player1, new SyrJoshuaAndSyrSaxon());
        Permanent second = addCreatureReady(player1, new SyrJoshuaAndSyrSaxon());
        int firstPower = gqs.getEffectivePower(gd, first);
        int secondPower = gqs.getEffectivePower(gd, second);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1));
            resolveAllTriggers();
        });

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(firstPower + 1);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(secondPower + 1);
    }

    @Test
    void oneCopyRetainingTheAbilityProtectsBothLegends() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SyrJoshuaAndSyrSaxon());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SyrJoshuaAndSyrSaxon());
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, first.getId());
        harness.runStateBasedActions();

        assertThat(gd.interaction.permanentChoiceContext()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactlyInAnyOrder(first, second);
    }

    @Test
    void nonlegendaryCopyDoesNotCountTowardExactlyTwoLegends() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SyrJoshuaAndSyrSaxon());
        harness.addToBattlefieldAndReturn(player1, new SyrJoshuaAndSyrSaxon());
        harness.castFromHand(player1, new SparkDouble(), "{3}{U}");
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, first.getId());

        harness.runStateBasedActions();

        assertThat(gd.interaction.permanentChoiceContext()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
    }

    @Test
    void multipleBattleCryInstancesTriggerSeparately() {
        Permanent original = addCreatureReady(player1, new SyrJoshuaAndSyrSaxon());
        for (int i = 0; i < 2; i++) {
            harness.castFromHand(player1, new SparkDouble(), "{3}{U}");
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player1, true);
            harness.handlePermanentChosen(player1, original.getId());
        }
        gd.playerBattlefields.get(player1.getId())
                .forEach(permanent -> permanent.setSummoningSick(false));
        int originalPower = gqs.getEffectivePower(gd, original);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1, 2));
            resolveAllTriggers();
        });

        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(originalPower + 4);
    }
}
