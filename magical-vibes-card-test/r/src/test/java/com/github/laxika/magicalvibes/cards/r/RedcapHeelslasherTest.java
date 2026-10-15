package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AlabasterHostSanctifier;
import com.github.laxika.magicalvibes.cards.v.VanquishTheWeak;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({RedcapHeelslasher.class, AlabasterHostSanctifier.class, VanquishTheWeak.class})
class RedcapHeelslasherTest extends BaseCardTest {

    @Test
    @DisplayName("Backup puts a counter on another creature and grants first strike")
    void backsUpAnotherCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new AlabasterHostSanctifier());
        Permanent heelslasher = castRedcapHeelslasher();

        resolveEtbTargeting(bears);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bears.getGrantedKeywords()).containsExactly(Keyword.FIRST_STRIKE);
        assertThat(heelslasher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Backup targeting the source puts on the counter but does not grant first strike")
    void backingUpSourceDoesNotGrantFirstStrike() {
        Permanent heelslasher = castRedcapHeelslasher();

        resolveEtbTargeting(heelslasher);

        assertThat(heelslasher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(heelslasher.getGrantedKeywords()).doesNotContain(Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("Backup's granted first strike expires at the end of the turn")
    void grantedFirstStrikeExpiresAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new AlabasterHostSanctifier());
        castRedcapHeelslasher();
        resolveEtbTargeting(bears);

        assertThat(bears.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Backup can put a counter on an opponent's creature and grant it first strike")
    void backsUpOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlabasterHostSanctifier());
        Permanent source = castRedcapHeelslasher();

        resolveEtbTargeting(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getGrantedKeywords()).containsExactly(Keyword.FIRST_STRIKE);
        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Backup still grants its counter and first strike if its source leaves before resolution")
    void backupResolvesAfterSourceDies() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AlabasterHostSanctifier());
        Permanent source = castRedcapHeelslasher();
        harness.handlePermanentChosen(player1, target.getId());

        destroyInResponse(source);
        harness.assertInGraveyard(player1, "Redcap Heelslasher");
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getGrantedKeywords()).containsExactly(Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("Backup does not affect its source when its chosen target dies before resolution")
    void backupDoesNotRetargetWhenTargetDies() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AlabasterHostSanctifier());
        Permanent source = castRedcapHeelslasher();
        harness.handlePermanentChosen(player1, target.getId());

        destroyInResponse(target);
        harness.assertInGraveyard(player1, "Alabaster Host Sanctifier");
        harness.passBothPriorities();

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(source.getGrantedKeywords()).doesNotContain(Keyword.FIRST_STRIKE);
        assertThat(gd.stack).isEmpty();
    }

    private void destroyInResponse(Permanent target) {
        harness.setHand(player2, List.of(new VanquishTheWeak()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
    }

    private Permanent castRedcapHeelslasher() {
        harness.castFromHand(player1, new RedcapHeelslasher(), "{3}{R}");
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof RedcapHeelslasher)
                .findFirst()
                .orElseThrow();
    }

    private void resolveEtbTargeting(Permanent target) {
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
