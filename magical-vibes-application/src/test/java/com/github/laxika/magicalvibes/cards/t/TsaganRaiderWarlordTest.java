package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FencingAce;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TsaganRaiderWarlord.class, FencingAce.class, GrizzlyBears.class})
class TsaganRaiderWarlordTest extends BaseCardTest {

    @Test
    void attackBoostScalesWithCreaturesHavingFirstOrDoubleStrike() {
        Permanent tsagan = addCreatureReady(player1, new TsaganRaiderWarlord());
        Permanent fencingAce = addCreatureReady(player1, new FencingAce());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, tsagan)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, fencingAce)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, tsagan)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, fencingAce)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }

    @Test
    void maxSpeedGivesTsaganDeathtouchAndOtherCreaturesFirstStrike() {
        Permanent tsagan = addCreatureReady(player1, new TsaganRaiderWarlord());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, tsagan, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isFalse();

        gd.playerSpeeds.put(player1.getId(), 4);

        assertThat(gqs.hasKeyword(gd, tsagan, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.FIRST_STRIKE)).isFalse();

        gd.playerSpeeds.put(player1.getId(), 3);

        assertThat(gqs.hasKeyword(gd, tsagan, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void maxSpeedCountsEachCreatureOnceEvenWithBothStrikeAbilities() {
        Permanent tsagan = addCreatureReady(player1, new TsaganRaiderWarlord());
        Permanent fencingAce = addCreatureReady(player1, new FencingAce());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentAce = addCreatureReady(player2, new FencingAce());
        gd.playerSpeeds.put(player1.getId(), 4);

        assertThat(gqs.hasKeyword(gd, tsagan, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, fencingAce, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, fencingAce, Keyword.DOUBLE_STRIKE)).isTrue();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, tsagan)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, fencingAce)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, opponentAce)).isEqualTo(1);
    }

    @Test
    void attackCountsStrikeAbilitiesAtResolutionAndLocksInTheBoost() {
        Permanent tsagan = addCreatureReady(player1, new TsaganRaiderWarlord());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        gd.playerSpeeds.put(player1.getId(), 3);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).hasSize(1);
        gd.playerSpeeds.put(player1.getId(), 4);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, tsagan)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);

        gd.playerSpeeds.put(player1.getId(), 3);
        assertThat(gqs.getEffectivePower(gd, tsagan)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
    }

    @Test
    void creaturesEnteringAfterResolutionDoNotReceiveTheAttackBoost() {
        Permanent tsagan = addCreatureReady(player1, new TsaganRaiderWarlord());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        Permanent lateBears = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, tsagan)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, lateBears)).isEqualTo(2);
    }

    @Test
    void enteringStartsEnginesAndDoubleStrikeIncreasesSpeedOnlyOncePerTurn() {
        harness.castFromHand(player1, new TsaganRaiderWarlord(), "{R}{W}{B}");
        resolveAllTriggers();
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);

        Permanent tsagan = findPermanent(player1, "Tsagan, Raider Warlord");
        tsagan.setSummoningSick(false);
        declareAttackers(List.of(0));
        resolveAllTriggers();
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 16);
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(2);
    }
}
