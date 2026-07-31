package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DevoutInvocationTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping all creatures creates a 4/4 Angel for each")
    void tapsAllCreaturesCreatesAngelForEach() {
        Permanent a = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent b = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

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
        Permanent tapped = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent untapped = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castDevoutInvocation();
        harness.handleMultiplePermanentsChosen(player1, List.of(tapped.getId()));

        assertThat(tapped.isTapped()).isTrue();
        assertThat(untapped.isTapped()).isFalse();
        assertThat(angels()).hasSize(1);
    }

    @Test
    @DisplayName("Tapping no creatures creates no Angels")
    void tapsNoneCreatesNoAngels() {
        Permanent a = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castDevoutInvocation();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(a.isTapped()).isFalse();
        assertThat(angels()).isEmpty();
    }

    @Test
    @DisplayName("Resolves harmlessly with no untapped creatures")
    void noUntappedCreaturesResolvesHarmlessly() {
        Permanent tapped = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        tapped.tap();

        castDevoutInvocation();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(angels()).isEmpty();
    }

    private List<Permanent> angels() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> "Angel".equals(permanent.getCard().getName()))
                .toList();
    }

    private void castDevoutInvocation() {
        harness.setHand(player1, List.of(new DevoutInvocation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
