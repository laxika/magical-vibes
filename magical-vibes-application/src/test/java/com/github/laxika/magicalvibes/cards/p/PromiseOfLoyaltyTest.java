package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PromiseOfLoyalty.class, GrizzlyBears.class})
class PromiseOfLoyaltyTest extends BaseCardTest {

    @Test
    @DisplayName("Each player keeps a chosen creature, gives it a vow counter, and sacrifices the rest")
    void eachPlayerChoosesAndSacrificesRest() {
        Permanent p1Kept = addReadyCreature(player1);
        Permanent p1Sacrificed = addReadyCreature(player1);
        Permanent p2Kept = addReadyCreature(player2);
        Permanent p2Sacrificed = addReadyCreature(player2);

        cast();

        PendingInteraction.MultiPermanentChoice p1Choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(p1Choice).isNotNull();
        assertThat(p1Choice.playerId()).isEqualTo(player1.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(p1Kept.getId()));

        PendingInteraction.MultiPermanentChoice p2Choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(p2Choice).isNotNull();
        assertThat(p2Choice.playerId()).isEqualTo(player2.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(p2Kept.getId()));

        assertThat(p1Kept.getCounterCount(CounterType.VOW)).isEqualTo(1);
        assertThat(p2Kept.getCounterCount(CounterType.VOW)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(p1Kept).doesNotContain(p1Sacrificed);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(p2Kept).doesNotContain(p2Sacrificed);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A creature with a vow counter can't attack the Promise's controller or their planeswalkers")
    void vowCreatureCannotAttackCaster() {
        Permanent vowCreature = addReadyCreature(player2);
        cast();

        assertThat(vowCreature.getCounterCount(CounterType.VOW)).isEqualTo(1);

        assertThatThrownBy(() -> declareAttackers(player2,
                List.of(gd.playerBattlefields.get(player2.getId()).indexOf(vowCreature))))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast() {
        harness.setHand(player1, List.of(new PromiseOfLoyalty()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.forceActivePlayer(player1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private Permanent addReadyCreature(Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        creature.setSummoningSick(false);
        return creature;
    }
}
