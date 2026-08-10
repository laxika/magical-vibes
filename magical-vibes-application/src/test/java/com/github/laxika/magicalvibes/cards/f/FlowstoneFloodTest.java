package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FlowstoneFloodTest extends BaseCardTest {

    @Test
    @DisplayName("Destroying a land without buyback puts Flowstone Flood in the graveyard")
    void destroysLandWithoutBuyback() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        FlowstoneFlood spell = new FlowstoneFlood();
        harness.setHand(player1, List.of(spell));
        addMana();
        int startingLife = gd.getLife(player1.getId());

        harness.castSorcery(player1, 0, land.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(land);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(land.getCard());
        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
    }

    @Test
    @DisplayName("Buyback pays life, randomly discards, destroys the land, and returns Flowstone Flood")
    void buybackPaysLifeDiscardsAndReturnsToHand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        FlowstoneFlood spell = new FlowstoneFlood();
        Card discarded = new GrizzlyBears();
        harness.setHand(player1, List.of(spell, discarded));
        addMana();
        int startingLife = gd.getLife(player1.getId());

        harness.castSorceryWithBuyback(player1, 0, land.getId());
        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife - 3);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(land);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spell);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(spell);
    }

    @Test
    @DisplayName("Buyback cannot be paid without a card to discard")
    void buybackRequiresRandomDiscard() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        FlowstoneFlood spell = new FlowstoneFlood();
        harness.setHand(player1, List.of(spell));
        addMana();
        int startingLife = gd.getLife(player1.getId());

        assertThatThrownBy(() -> harness.castSorceryWithBuyback(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spell);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land);
        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(4);
    }

    @Test
    @DisplayName("Flowstone Flood cannot target a creature")
    void cannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FlowstoneFlood()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
