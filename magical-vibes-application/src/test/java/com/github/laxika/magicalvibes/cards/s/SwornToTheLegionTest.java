package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SwornToTheLegion.class, GrizzlyBears.class, SteadfastPaladin.class})
class SwornToTheLegionTest extends BaseCardTest {

    @Test
    void givesDoubleTeamToNontokenCreaturesAlreadyOnTheBattlefield() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        GrizzlyBears tokenCard = new GrizzlyBears();
        tokenCard.setToken(true);
        Permanent token = addCreatureReady(player1, tokenCard);

        harness.enterBattlefieldAndReturn(player1, new SwornToTheLegion());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_TEAM)).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.DOUBLE_TEAM)).isFalse();
    }

    @Test
    void givesDoubleTeamToCreatureSpellsYouCast() {
        harness.addToBattlefield(player1, new SwornToTheLegion());
        GrizzlyBears bears = new GrizzlyBears();
        harness.castFromHand(player1, bears, "{1}{G}");
        resolveAllTriggers();

        Permanent permanent = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, permanent, Keyword.DOUBLE_TEAM)).isTrue();
    }

    @Test
    void enterTriggerUsesCreaturesPresentWhenItResolvesAndIgnoresOpponents() {
        Permanent opponent = addCreatureReady(player2, new SteadfastPaladin());
        harness.enterBattlefieldAndReturn(player1, new SwornToTheLegion());
        Permanent arriving = addCreatureReady(player1, new SteadfastPaladin());

        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, arriving, Keyword.DOUBLE_TEAM)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.DOUBLE_TEAM)).isFalse();
    }

    @Test
    void creaturesEnteringWithoutBeingCastDoNotGainDoubleTeamAfterEnterTriggerResolves() {
        harness.enterBattlefieldAndReturn(player1, new SwornToTheLegion());
        resolveAllTriggers();

        Permanent arriving = harness.enterBattlefieldAndReturn(player1, new SteadfastPaladin());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, arriving, Keyword.DOUBLE_TEAM)).isFalse();
    }

    @Test
    void opponentsCreatureSpellsDoNotGainDoubleTeam() {
        harness.addToBattlefield(player1, new SwornToTheLegion());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new SteadfastPaladin(), "{1}{W}");
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, findPermanent(player2, "Steadfast Paladin"), Keyword.DOUBLE_TEAM))
                .isFalse();
    }

    @Test
    void grantedDoubleTeamConjuresADuplicateAndRemovesItself() {
        Permanent paladin = addCreatureReady(player1, new SteadfastPaladin());
        harness.setHand(player1, List.of());
        harness.enterBattlefieldAndReturn(player1, new SwornToTheLegion());
        resolveAllTriggers();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1)
                .allMatch(card -> card instanceof SteadfastPaladin);
        assertThat(gqs.hasKeyword(gd, paladin, Keyword.DOUBLE_TEAM)).isFalse();
    }

    @Test
    void twoPerpetualGrantsProduceTwoDuplicatesOnAttack() {
        Permanent paladin = addCreatureReady(player1, new SteadfastPaladin());
        harness.setHand(player1, List.of());
        harness.enterBattlefieldAndReturn(player1, new SwornToTheLegion());
        resolveAllTriggers();
        harness.enterBattlefieldAndReturn(player1, new SwornToTheLegion());
        resolveAllTriggers();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2)
                .allMatch(card -> card instanceof SteadfastPaladin);
        assertThat(gqs.hasKeyword(gd, paladin, Keyword.DOUBLE_TEAM)).isFalse();
    }

    @Test
    void laterPerpetualGrantRestoresDoubleTeamAfterItWasUsed() {
        Permanent paladin = addCreatureReady(player1, new SteadfastPaladin());
        harness.setHand(player1, List.of());
        harness.enterBattlefieldAndReturn(player1, new SwornToTheLegion());
        resolveAllTriggers();
        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, paladin, Keyword.DOUBLE_TEAM)).isFalse();

        harness.enterBattlefieldAndReturn(player1, new SwornToTheLegion());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, paladin, Keyword.DOUBLE_TEAM)).isTrue();
    }

    @Test
    void twoCastTriggersGrantTwoIndependentInstancesOfDoubleTeam() {
        harness.addToBattlefield(player1, new SwornToTheLegion());
        harness.addToBattlefield(player1, new SwornToTheLegion());
        harness.castFromHand(player1, new SteadfastPaladin(), "{1}{W}");
        resolveAllTriggers();
        Permanent paladin = findPermanent(player1, "Steadfast Paladin");
        paladin.setSummoningSick(false);

        declareAttackers(List.of(2));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2)
                .allMatch(card -> card instanceof SteadfastPaladin);
        assertThat(gqs.hasKeyword(gd, paladin, Keyword.DOUBLE_TEAM)).isFalse();
    }

    @Test
    void conjuredCreatureGainsDoubleTeamWhenCast() {
        Permanent paladin = addCreatureReady(player1, new SteadfastPaladin());
        harness.setHand(player1, List.of());
        harness.enterBattlefieldAndReturn(player1, new SwornToTheLegion());
        resolveAllTriggers();
        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        SteadfastPaladin duplicate = (SteadfastPaladin) gd.playerHands.get(player1.getId()).getFirst();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.castFromHand(player1, duplicate, "{1}{W}");
        resolveAllTriggers();

        Permanent conjured = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(duplicate.getId()))
                .findFirst().orElseThrow();
        assertThat(gqs.hasKeyword(gd, conjured, Keyword.DOUBLE_TEAM)).isTrue();
        assertThat(gqs.hasKeyword(gd, paladin, Keyword.DOUBLE_TEAM)).isFalse();
    }

    @Test
    void enterGrantSurvivesReturningToHandAndTheEnchantmentLeaving() {
        Permanent paladin = addCreatureReady(player1, new SteadfastPaladin());
        Permanent legion = harness.enterBattlefieldAndReturn(player1, new SwornToTheLegion());
        resolveAllTriggers();
        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToHand(gd, paladin);
            harness.getPermanentRemovalService().removePermanentToHand(gd, legion);
        });

        harness.castFromHand(player1, paladin.getOriginalCard(), "{1}{W}");
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Steadfast Paladin"), Keyword.DOUBLE_TEAM))
                .isTrue();
    }
}
