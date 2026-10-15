package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.w.WallOfRazors;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Cannibalize.class, CravenGiant.class, WallOfRazors.class})
class CannibalizeTest extends BaseCardTest {

    private void prepareCannibalize() {
        harness.setHand(player1, List.of(new Cannibalize()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void castCannibalizeWithoutResolving(Permanent first, Permanent second) {
        prepareCannibalize();
        harness.castSorcery(player1, 0, List.of(first.getId(), second.getId()));
    }

    private void castCannibalize(Permanent first, Permanent second) {
        prepareCannibalize();
        harness.castAndResolveSorcery(player1, 0, List.of(first.getId(), second.getId()));
    }

    @Test
    @DisplayName("The spell controller chooses which targeted creature to exile")
    void spellControllerChoosesExiledCreature() {
        Permanent wall = addCreatureReady(player2, new WallOfRazors());
        Permanent giant = addCreatureReady(player2, new CravenGiant());

        castCannibalize(wall, giant);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(wall.getId(), giant.getId());
    }

    @Test
    @DisplayName("Exiles the chosen creature and puts two +1/+1 counters on the other")
    void exilesChosenAndCountersOther() {
        Permanent wall = addCreatureReady(player2, new WallOfRazors());
        Permanent giant = addCreatureReady(player2, new CravenGiant());

        castCannibalize(wall, giant);
        harness.handlePermanentChosen(player1, wall.getId());

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName())
                .contains("Wall of Razors");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(giant);
        assertThat(giant.getCounterCount(com.github.laxika.magicalvibes.model.CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(2);
    }

    @Test
    @DisplayName("Exiles the second chosen creature and puts two +1/+1 counters on the first")
    void exilesSecondChosenAndCountersFirst() {
        Permanent wall = addCreatureReady(player2, new WallOfRazors());
        Permanent giant = addCreatureReady(player2, new CravenGiant());

        castCannibalize(wall, giant);
        harness.handlePermanentChosen(player1, giant.getId());

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName())
                .contains("Craven Giant");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(wall);
        assertThat(wall.getCounterCount(com.github.laxika.magicalvibes.model.CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(2);
    }

    @Test
    @DisplayName("Exiles the only legal target when the other target is gone")
    void exilesOnlyRemainingTarget() {
        Permanent wall = addCreatureReady(player2, new WallOfRazors());
        Permanent giant = addCreatureReady(player2, new CravenGiant());

        castCannibalizeWithoutResolving(wall, giant);
        gd.playerBattlefields.get(player2.getId()).remove(giant);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName())
                .contains("Wall of Razors");
    }

    @Test
    @DisplayName("Neither target is affected when they no longer share a controller")
    void differentControllersOnResolutionMakeBothTargetsIllegal() {
        Permanent wall = addCreatureReady(player2, new WallOfRazors());
        Permanent giant = addCreatureReady(player2, new CravenGiant());

        castCannibalizeWithoutResolving(wall, giant);
        gd.playerBattlefields.get(player2.getId()).remove(wall);
        gd.playerBattlefields.get(player1.getId()).add(wall);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(wall);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(giant);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(wall.getCounterCount(com.github.laxika.magicalvibes.model.CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(giant.getCounterCount(com.github.laxika.magicalvibes.model.CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Requires both targets to be controlled by the same player")
    void rejectsTargetsWithDifferentControllers() {
        Permanent own = addCreatureReady(player1, new WallOfRazors());
        Permanent theirs = addCreatureReady(player2, new CravenGiant());

        prepareCannibalize();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(own.getId(), theirs.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Both targets remain legal when they change to the same new controller")
    void bothTargetsCanChangeToSameNewController() {
        Permanent wall = addCreatureReady(player2, new WallOfRazors());
        Permanent giant = addCreatureReady(player2, new CravenGiant());

        castCannibalizeWithoutResolving(wall, giant);
        for (Permanent stolen : List.of(wall, giant)) {
            gd.stolenCreatures.put(stolen.getId(), player2.getId());
            gd.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(), "Control effect", null,
                    player1.getId(), new GainControlOfTargetEffect(ControlDuration.PERMANENT), stolen.getId(),
                    null, null, EffectDuration.PERMANENT, 0));
        }
        harness.inMutationScope(() -> GameTestEngineContext.get()
                .getBean(CreatureControlService.class)
                .reconcileControl(gd));
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(wall.getId(), giant.getId());
        harness.handlePermanentChosen(player1, wall.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(giant).doesNotContain(wall);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName()).contains("Wall of Razors");
        assertThat(giant.getCounterCount(com.github.laxika.magicalvibes.model.CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(2);
    }

    @Test
    @DisplayName("Exiles the second target when the first target has left the battlefield")
    void exilesSecondTargetWhenFirstIsGone() {
        Permanent wall = addCreatureReady(player2, new WallOfRazors());
        Permanent giant = addCreatureReady(player2, new CravenGiant());

        castCannibalizeWithoutResolving(wall, giant);
        gd.playerBattlefields.get(player2.getId()).remove(wall);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(giant);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName()).containsExactly("Craven Giant");
    }

    @Test
    @DisplayName("Does not resolve when both targets have left the battlefield")
    void bothTargetsGone() {
        Permanent wall = addCreatureReady(player2, new WallOfRazors());
        Permanent giant = addCreatureReady(player2, new CravenGiant());

        castCannibalizeWithoutResolving(wall, giant);
        gd.playerBattlefields.get(player2.getId()).removeAll(List.of(wall, giant));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Cannibalize");
    }

    @Test
    @DisplayName("Can target two creatures controlled by the spell controller")
    void canTargetOwnCreatures() {
        Permanent wall = addCreatureReady(player1, new WallOfRazors());
        Permanent giant = addCreatureReady(player1, new CravenGiant());

        castCannibalize(wall, giant);
        harness.handlePermanentChosen(player1, wall.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(giant).doesNotContain(wall);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName()).containsExactly("Wall of Razors");
        assertThat(giant.getCounterCount(com.github.laxika.magicalvibes.model.CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(2);
    }

    @Test
    @DisplayName("Requires two distinct creatures")
    void rejectsSameCreatureTwice() {
        Permanent wall = addCreatureReady(player2, new WallOfRazors());
        prepareCannibalize();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(wall.getId(), wall.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
