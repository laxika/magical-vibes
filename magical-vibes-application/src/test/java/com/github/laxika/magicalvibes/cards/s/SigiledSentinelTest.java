package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FinalFlourish;
import com.github.laxika.magicalvibes.cards.p.ProtocolKnight;
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

@CardUsed({SigiledSentinel.class, ProtocolKnight.class})
class SigiledSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("Backup puts a +1/+1 counter on another creature and grants vigilance")
    void backsUpAnotherCreature() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new ProtocolKnight());
        Permanent sentinel = castSigiledSentinel();

        resolveEtbTargeting(knight);

        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(knight.hasKeyword(Keyword.VIGILANCE)).isTrue();
        assertThat(sentinel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Backup targeting the source puts on the counter but does not grant vigilance")
    void backingUpSourceDoesNotGrantVigilance() {
        Permanent sentinel = castSigiledSentinel();

        resolveEtbTargeting(sentinel);

        assertThat(sentinel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(sentinel.getGrantedKeywords()).doesNotContain(Keyword.VIGILANCE);
    }

    @Test
    @DisplayName("Backup's granted vigilance expires at the end of the turn")
    void grantedVigilanceExpiresAtEndOfTurn() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new ProtocolKnight());
        castSigiledSentinel();
        resolveEtbTargeting(knight);

        assertThat(knight.hasKeyword(Keyword.VIGILANCE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(knight.hasKeyword(Keyword.VIGILANCE)).isFalse();
        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Backup can put a counter on an opponent's creature and grant it vigilance")
    void backsUpOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ProtocolKnight());
        castSigiledSentinel();

        resolveEtbTargeting(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.hasKeyword(Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @CardUsed(FinalFlourish.class)
    @DisplayName("Backup resolves even if Sigiled Sentinel leaves the battlefield in response")
    void backupResolvesAfterSourceLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ProtocolKnight());
        Permanent sentinel = castSigiledSentinel();
        harness.handlePermanentChosen(player1, target.getId());

        harness.setHand(player2, List.of(new FinalFlourish()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, sentinel.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sentinel);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.hasKeyword(Keyword.VIGILANCE)).isTrue();
    }

    private Permanent castSigiledSentinel() {
        harness.castFromHand(player1, new SigiledSentinel(), "{2}{W}");
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof SigiledSentinel)
                .findFirst()
                .orElseThrow();
    }

    private void resolveEtbTargeting(Permanent target) {
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
