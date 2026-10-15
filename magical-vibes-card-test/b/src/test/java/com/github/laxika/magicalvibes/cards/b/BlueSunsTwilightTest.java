package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlueSunsTwilight.class, GrizzlyBears.class, SerraAngel.class})
class BlueSunsTwilightTest extends BaseCardTest {

    @Test
    @DisplayName("Gains permanent control of a creature with mana value X or less")
    void gainsPermanentControl() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        castAndResolve(2, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("At X=5, creates a token copy after gaining control")
    void createsTokenCopyAtFive() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        castAndResolve(5, target.getId());

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getCard().getPower()).isEqualTo(2);
        assertThat(tokens.getFirst().getCard().getToughness()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Does not create a token copy below X=5")
    void doesNotCreateTokenCopyBelowFive() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        castAndResolve(4, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Rejects a creature with mana value greater than X")
    void rejectsCreatureAboveManaValueLimit() {
        Permanent target = addCreatureReady(player2, new SerraAngel());

        assertThatThrownBy(() -> {
            harness.setHand(player1, List.of(new BlueSunsTwilight()));
            harness.addMana(player1, ManaColor.BLUE, 4);
            harness.castSorcery(player1, 0, 2, target.getId());
        }).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target an already controlled creature and copy it above X=5")
    void copiesOwnCreatureAboveFive() {
        Permanent target = addCreatureReady(player1, new SerraAngel());

        castAndResolve(6, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement().satisfies(token -> {
                    assertThat(token.getId()).isNotEqualTo(target.getId());
                    assertThat(token.isSummoningSick()).isTrue();
                    assertThat(token.hasKeyword(Keyword.FLYING)).isTrue();
                    assertThat(token.hasKeyword(Keyword.VIGILANCE)).isTrue();
                });
    }

    @Test
    @DisplayName("Control preserves counters and tapping but the copy does not inherit them")
    void copyDoesNotInheritCountersOrTappedState() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        castAndResolve(5, target.getId());

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(target.isSummoningSick()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement().satisfies(token -> {
                    assertThat(token.isTapped()).isFalse();
                    assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
                    assertThat(token.getEffectivePower()).isEqualTo(2);
                    assertThat(token.getEffectiveToughness()).isEqualTo(2);
                });
    }

    @Test
    @DisplayName("The control change and token persist through the next turn")
    void controlAndCopyPersistAcrossTurnBoundary() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        castAndResolve(5, target.getId());
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("An illegal target at resolution prevents both control and copying")
    void gainingHexproofInResponseStopsBothEffects() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BlueSunsTwilight()));
        harness.addMana(player1, ManaColor.BLUE, 7);
        harness.castSorcery(player1, 0, 5, target.getId());
        target.getGrantedKeywords().add(Keyword.HEXPROOF);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(target);
        harness.assertInGraveyard(player1, "Blue Sun's Twilight");
    }

    private void castAndResolve(int xValue, UUID targetId) {
        harness.setHand(player1, List.of(new BlueSunsTwilight()));
        harness.addMana(player1, ManaColor.BLUE, xValue + 2);
        harness.castAndResolveSorcery(player1, 0, xValue, targetId);
    }
}
