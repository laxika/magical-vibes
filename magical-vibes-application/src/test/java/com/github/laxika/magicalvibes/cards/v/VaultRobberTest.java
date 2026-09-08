package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VaultRobberTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature card and creates a Treasure token")
    void exilesCreatureAndCreatesTreasure() {
        Permanent robber = harness.addToBattlefieldAndReturn(player1, new VaultRobber());
        robber.setSummoningSick(false);
        GrizzlyBears creatureCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creatureCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int robberIndex = gd.playerBattlefields.get(player1.getId()).indexOf(robber);
        harness.activateAbility(player1, robberIndex, null, null);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.GraveyardExileCostChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(creatureCard.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(creatureCard.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(robber.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot activate without a creature card in the graveyard")
    void cannotActivateWithoutCreatureCard() {
        Permanent robber = harness.addToBattlefieldAndReturn(player1, new VaultRobber());
        robber.setSummoningSick(false);
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int robberIndex = gd.playerBattlefields.get(player1.getId()).indexOf(robber);
        assertThatThrownBy(() -> harness.activateAbility(player1, robberIndex, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }
}
