package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.b.BairdArgivianRecruiter;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
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

@CardUsed({ToriDAvenantFuryRider.class, GoblinPiker.class, SavannahLions.class, GrizzlyBears.class,
        BairdArgivianRecruiter.class, LightningStrike.class})
class ToriDAvenantFuryRiderTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts, grants trample to, and untaps the matching other attackers")
    void affectsMatchingOtherAttackers() {
        Permanent tori = addCreatureReady(player1, new ToriDAvenantFuryRider());
        Permanent redAttacker = addCreatureReady(player1, new GoblinPiker());
        Permanent whiteAttacker = addCreatureReady(player1, new SavannahLions());
        Permanent greenAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent redNonAttacker = addCreatureReady(player1, new GoblinPiker());
        Permanent opponentRed = addCreatureReady(player2, new GoblinPiker());

        declareAttackers(List.of(0, 1, 2, 3));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, tori)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, redAttacker)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, redAttacker)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, redAttacker, Keyword.TRAMPLE)).isTrue();
        assertThat(PermanentGrants.powerBoostOf(gd, whiteAttacker)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, whiteAttacker)).isEqualTo(1);
        assertThat(whiteAttacker.isTapped()).isFalse();
        assertThat(PermanentGrants.powerBoostOf(gd, greenAttacker)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, greenAttacker)).isEqualTo(1);
        assertThat(greenAttacker.isTapped()).isTrue();
        assertThat(PermanentGrants.powerBoostOf(gd, redNonAttacker)).isZero();
        assertThat(gqs.hasKeyword(gd, redNonAttacker, Keyword.TRAMPLE)).isFalse();
        assertThat(PermanentGrants.powerBoostOf(gd, opponentRed)).isZero();
        assertThat(gqs.hasKeyword(gd, opponentRed, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The temporary boost and trample grant wear off at end of turn")
    void temporaryEffectsWearOffAtEndOfTurn() {
        addCreatureReady(player1, new ToriDAvenantFuryRider());
        Permanent redAttacker = addCreatureReady(player1, new GoblinPiker());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, redAttacker)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, redAttacker, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, redAttacker)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, redAttacker)).isZero();
        assertThat(gqs.hasKeyword(gd, redAttacker, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @CardUsed({ToriDAvenantFuryRider.class, BairdArgivianRecruiter.class})
    @DisplayName("A red-white attacker receives every benefit and remains attacking after untapping")
    void redWhiteAttackerReceivesAllBenefits() {
        Permanent tori = addCreatureReady(player1, new ToriDAvenantFuryRider());
        Permanent baird = addCreatureReady(player1, new BairdArgivianRecruiter());
        Permanent opponentBaird = addCreatureReady(player2, new BairdArgivianRecruiter());
        opponentBaird.tap();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1));
            assertThat(baird.isTapped()).isTrue();
            resolveAllTriggers();
        });

        assertThat(PermanentGrants.powerBoostOf(gd, tori)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, tori)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, baird)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, baird)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, baird, Keyword.TRAMPLE)).isTrue();
        assertThat(baird.isTapped()).isFalse();
        assertThat(baird.isAttacking()).isTrue();
        assertThat(PermanentGrants.powerBoostOf(gd, opponentBaird)).isZero();
        assertThat(gqs.hasKeyword(gd, opponentBaird, Keyword.TRAMPLE)).isFalse();
        assertThat(opponentBaird.isTapped()).isTrue();
    }

    @Test
    @CardUsed({ToriDAvenantFuryRider.class, BairdArgivianRecruiter.class})
    @DisplayName("Other creatures attacking without Tori do not trigger her ability")
    void doesNotTriggerWhenToriDoesNotAttack() {
        addCreatureReady(player1, new ToriDAvenantFuryRider());
        Permanent baird = addCreatureReady(player1, new BairdArgivianRecruiter());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, baird)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, baird)).isZero();
        assertThat(gqs.hasKeyword(gd, baird, Keyword.TRAMPLE)).isFalse();
        assertThat(baird.isTapped()).isTrue();
    }

    @Test
    @CardUsed({ToriDAvenantFuryRider.class, BairdArgivianRecruiter.class, LightningStrike.class})
    @DisplayName("Tori's attack trigger resolves after she is destroyed in response")
    void attackTriggerResolvesAfterToriLeavesBattlefield() {
        Permanent tori = addCreatureReady(player1, new ToriDAvenantFuryRider());
        Permanent baird = addCreatureReady(player1, new BairdArgivianRecruiter());
        harness.setHand(player2, List.of(new LightningStrike()));

        declareAttackers(List.of(0, 1));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, tori.getId());
        harness.assertNotOnBattlefield(player1, "Tori D'Avenant, Fury Rider");
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, baird)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, baird)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, baird, Keyword.TRAMPLE)).isTrue();
        assertThat(baird.isTapped()).isFalse();
    }
}
