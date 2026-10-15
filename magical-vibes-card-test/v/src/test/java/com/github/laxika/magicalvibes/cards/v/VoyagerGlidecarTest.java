package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AlacrianJaguar;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VoyagerGlidecar.class, AlacrianJaguar.class})
class VoyagerGlidecarTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield prompts for scry 1")
    void entersWithScryOne() {
        harness.setHand(player1, List.of(new VoyagerGlidecar()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    @DisplayName("Tapping three other creatures animates Voyager Glidecar, grants flying, and adds a counter")
    void tappingThreeOtherCreaturesActivatesGlidecar() {
        Permanent glidecar = addCreatureReady(player1, new VoyagerGlidecar());
        Permanent first = addCreatureReady(player1, new AlacrianJaguar());
        Permanent second = addCreatureReady(player1, new AlacrianJaguar());
        Permanent third = addCreatureReady(player1, new AlacrianJaguar());

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
        Permanent glidecar = addCreatureReady(player1, new VoyagerGlidecar());
        addCreatureReady(player1, new AlacrianJaguar());
        addCreatureReady(player1, new AlacrianJaguar());
        addCreatureReady(player1, new AlacrianJaguar());

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
        Permanent glidecar = addCreatureReady(player1, new VoyagerGlidecar());
        addCreatureReady(player1, new AlacrianJaguar());
        addCreatureReady(player1, new AlacrianJaguar());

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(glidecar);
        assertThatThrownBy(() -> harness.activateAbility(player1, index, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void crewOneAnimatesWithoutFlyingOrCounter() {
        Permanent glidecar = harness.addToBattlefieldAndReturn(player1, new VoyagerGlidecar());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AlacrianJaguar());

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(glidecar);
        harness.activateAbility(player1, index, 1, null, null);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(glidecar.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, glidecar)).isTrue();
        assertThat(gqs.hasKeyword(gd, glidecar, Keyword.FLYING)).isFalse();
        assertThat(glidecar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void summoningSickCreaturesCanPayCostAndEffectsWaitForResolution() {
        Permanent glidecar = harness.addToBattlefieldAndReturn(player1, new VoyagerGlidecar());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new AlacrianJaguar());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new AlacrianJaguar());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new AlacrianJaguar());

        harness.activateAbility(player1, 0, null, null);

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(third.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, glidecar)).isFalse();
        assertThat(glidecar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, glidecar)).isTrue();
        assertThat(gqs.hasKeyword(gd, glidecar, Keyword.FLYING)).isTrue();
        assertThat(glidecar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(glidecar.isTapped()).isFalse();
    }

    @Test
    void tappedCreaturesAndOpponentsCreaturesCannotPayCost() {
        addCreatureReady(player1, new VoyagerGlidecar());
        addCreatureReady(player1, new AlacrianJaguar());
        addCreatureReady(player1, new AlacrianJaguar());
        Permanent tapped = addCreatureReady(player1, new AlacrianJaguar());
        tapped.tap();
        addCreatureReady(player2, new AlacrianJaguar());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void animatedGlidecarCannotCountItselfAmongThreeOtherCreatures() {
        Permanent glidecar = addCreatureReady(player1, new VoyagerGlidecar());
        Permanent first = addCreatureReady(player1, new AlacrianJaguar());
        Permanent second = addCreatureReady(player1, new AlacrianJaguar());
        addCreatureReady(player1, new AlacrianJaguar());
        activate(glidecar);
        first.untap();
        second.untap();

        assertThat(gqs.isCreature(gd, glidecar)).isTrue();
        assertThat(glidecar.isTapped()).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void scryCanPutTopCardOnBottom() {
        AlacrianJaguar top = new AlacrianJaguar();
        VoyagerGlidecar next = new VoyagerGlidecar();
        harness.setLibrary(player1, List.of(top, next));
        harness.setHand(player1, List.of(new VoyagerGlidecar()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next, top);
    }

    @Test
    void scryWithEmptyLibraryFinishesWithoutChoice() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new VoyagerGlidecar()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Voyager Glidecar");
    }

    private void activate(Permanent glidecar) {
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(glidecar);
        harness.activateAbility(player1, index, null, null);
        harness.passBothPriorities();
    }
}
