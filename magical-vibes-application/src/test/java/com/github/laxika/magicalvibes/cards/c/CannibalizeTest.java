package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.w.WallOfRazors;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Cannibalize.class, CravenGiant.class, WallOfRazors.class})
class CannibalizeTest extends BaseCardTest {

    private void castCannibalizeWithoutResolving(Permanent first, Permanent second) {
        harness.setHand(player1, List.of(new Cannibalize()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, List.of(first.getId(), second.getId()));
    }

    private void castCannibalize(Permanent first, Permanent second) {
        castCannibalizeWithoutResolving(first, second);
        harness.passBothPriorities();
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
    @DisplayName("A target that changes controllers before resolution is not affected")
    void changedControllerTargetIsNotAffected() {
        Permanent wall = addCreatureReady(player2, new WallOfRazors());
        Permanent giant = addCreatureReady(player2, new CravenGiant());

        castCannibalizeWithoutResolving(wall, giant);
        gd.playerBattlefields.get(player2.getId()).remove(wall);
        gd.playerBattlefields.get(player1.getId()).add(wall);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(wall);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName())
                .contains("Craven Giant");
    }

    @Test
    @DisplayName("Requires both targets to be controlled by the same player")
    void rejectsTargetsWithDifferentControllers() {
        Permanent own = addCreatureReady(player1, new WallOfRazors());
        Permanent theirs = addCreatureReady(player2, new CravenGiant());

        harness.setHand(player1, List.of(new Cannibalize()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(own.getId(), theirs.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
