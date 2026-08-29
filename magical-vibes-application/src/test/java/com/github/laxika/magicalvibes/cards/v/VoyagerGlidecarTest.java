package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VoyagerGlidecarTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield prompts for scry 1")
    void entersWithScryOne() {
        harness.setHand(player1, List.of(new VoyagerGlidecar()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    @DisplayName("Tapping three other creatures animates Voyager Glidecar, grants flying, and adds a counter")
    void tappingThreeOtherCreaturesActivatesGlidecar() {
        Permanent glidecar = addReady(new VoyagerGlidecar());
        Permanent first = addReady(new GrizzlyBears());
        Permanent second = addReady(new GrizzlyBears());
        Permanent third = addReady(new GrizzlyBears());

        activate(glidecar);

        assertThat(gqs.isCreature(gd, glidecar)).isTrue();
        assertThat(gqs.isArtifact(glidecar)).isTrue();
        assertThat(gqs.hasKeyword(gd, glidecar, Keyword.FLYING)).isTrue();
        assertThat(glidecar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(third.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Animation and flying wear off at end of turn but the counter remains")
    void temporaryEffectsWearOff() {
        Permanent glidecar = addReady(new VoyagerGlidecar());
        addReady(new GrizzlyBears());
        addReady(new GrizzlyBears());
        addReady(new GrizzlyBears());

        activate(glidecar);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, glidecar)).isFalse();
        assertThat(gqs.hasKeyword(gd, glidecar, Keyword.FLYING)).isFalse();
        assertThat(glidecar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate without three other untapped creatures")
    void cannotActivateWithoutThreeOtherCreatures() {
        Permanent glidecar = addReady(new VoyagerGlidecar());
        addReady(new GrizzlyBears());
        addReady(new GrizzlyBears());

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(glidecar);
        assertThatThrownBy(() -> harness.activateAbility(player1, index, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReady(Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }

    private void activate(Permanent glidecar) {
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(glidecar);
        harness.activateAbility(player1, index, null, null);
        harness.passBothPriorities();
    }
}
