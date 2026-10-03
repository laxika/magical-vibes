package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DevoutInvocation.class, CoralMerfolk.class})
class DevoutInvocationTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping all creatures creates a 4/4 Angel for each")
    void tapsAllCreaturesCreatesAngelForEach() {
        Permanent a = harness.addToBattlefieldAndReturn(player1, new CoralMerfolk());
        Permanent b = harness.addToBattlefieldAndReturn(player1, new CoralMerfolk());

        castDevoutInvocation();
        harness.handleMultiplePermanentsChosen(player1, List.of(a.getId(), b.getId()));

        assertThat(a.isTapped()).isTrue();
        assertThat(b.isTapped()).isTrue();
        assertThat(angels()).hasSize(2);
        assertThat(angels()).allSatisfy(angel -> {
            assertThat(angel.getCard().getPower()).isEqualTo(4);
            assertThat(angel.getCard().getToughness()).isEqualTo(4);
        });
    }

    @Test
    @DisplayName("Tapping a subset creates an Angel only for the creatures tapped")
    void tapsSubsetCreatesAngelPerTapped() {
        Permanent tapped = harness.addToBattlefieldAndReturn(player1, new CoralMerfolk());
        Permanent untapped = harness.addToBattlefieldAndReturn(player1, new CoralMerfolk());

        castDevoutInvocation();
        harness.handleMultiplePermanentsChosen(player1, List.of(tapped.getId()));

        assertThat(tapped.isTapped()).isTrue();
        assertThat(untapped.isTapped()).isFalse();
        assertThat(angels()).hasSize(1);
    }

    @Test
    @DisplayName("Tapping no creatures creates no Angels")
    void tapsNoneCreatesNoAngels() {
        Permanent a = harness.addToBattlefieldAndReturn(player1, new CoralMerfolk());

        castDevoutInvocation();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(a.isTapped()).isFalse();
        assertThat(angels()).isEmpty();
    }

    @Test
    @DisplayName("Resolves harmlessly with no untapped creatures")
    void noUntappedCreaturesResolvesHarmlessly() {
        Permanent tapped = harness.addToBattlefieldAndReturn(player1, new CoralMerfolk());
        tapped.tap();

        castDevoutInvocation();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(angels()).isEmpty();
    }

    @Test
    @DisplayName("Angels enter untapped and not attacking with flying")
    void angelsEnterUntappedAndNotAttacking() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CoralMerfolk());

        castDevoutInvocation();
        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId()));

        assertThat(angels()).hasSize(1).allSatisfy(angel -> {
            assertThat(angel.isTapped()).isFalse();
            assertThat(angel.isAttacking()).isFalse();
            assertThat(angel.getCard().getKeywords()).contains(Keyword.FLYING);
        });
    }

    @Test
    @DisplayName("Angels remain on the battlefield after combat")
    void angelsRemainAfterCombat() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CoralMerfolk());

        castDevoutInvocation();
        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId()));
        Permanent angel = angels().getFirst();

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(angel);
    }

    @Test
    @DisplayName("Only untapped creatures controlled by the caster can be chosen")
    void excludesTappedAndOpposingCreatures() {
        Permanent eligible = harness.addToBattlefieldAndReturn(player1, new CoralMerfolk());
        Permanent tapped = harness.addToBattlefieldAndReturn(player1, new CoralMerfolk());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new CoralMerfolk());
        tapped.tap();

        castDevoutInvocation();

        assertThatThrownBy(() ->
                harness.handleMultiplePermanentsChosen(player1, List.of(tapped.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() ->
                harness.handleMultiplePermanentsChosen(player1, List.of(opponent.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(eligible.getId()));

        assertThat(eligible.isTapped()).isTrue();
        assertThat(opponent.isTapped()).isFalse();
        assertThat(angels()).hasSize(1);
    }

    private List<Permanent> angels() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> "Angel".equals(permanent.getCard().getName()))
                .toList();
    }

    private void castDevoutInvocation() {
        harness.castFromHand(player1, new DevoutInvocation(), "{6}{W}");
        harness.passBothPriorities();
    }
}
