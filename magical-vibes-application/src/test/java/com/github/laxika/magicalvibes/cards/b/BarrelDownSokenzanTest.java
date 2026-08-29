package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BarrelDownSokenzanTest extends BaseCardTest {

    @Test
    @DisplayName("Returns the chosen Mountains and deals twice their number in damage")
    void returnsChosenMountainsAndDealsTwiceTheirNumberInDamage() {
        Permanent target = addCreatureReady(player2);
        Permanent firstMountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent secondMountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        castCard(target);
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(firstMountain.getId(), secondMountain.getId());
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultiplePermanentsChosen(player1, List.of(firstMountain.getId(), secondMountain.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(forest);
        assertThat(target.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Returning no Mountains deals no damage and is legal")
    void returningNoMountainsDealsNoDamage() {
        Permanent target = addCreatureReady(player2);
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());

        castCard(target);
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(mountain);
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new BarrelDownSokenzan()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castCard(Permanent target) {
        harness.setHand(player1, List.of(new BarrelDownSokenzan()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addCreatureReady(Player player) {
        return addCreatureReady(player, new ColossalDreadmaw());
    }
}
