package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.Gravecrawler;
import com.github.laxika.magicalvibes.cards.r.ReassemblingSkeleton;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmbalmersToolsTest extends BaseCardTest {

    // ===== Static: graveyard creature-card ability cost reduction =====

    @Test
    @DisplayName("Creature card's graveyard ability costs {1} less to activate")
    void graveyardCreatureAbilityCostsOneLess() {
        harness.setGraveyard(player1, List.of(new ReassemblingSkeleton()));
        harness.addToBattlefield(player1, new EmbalmersTools());
        // Reassembling Skeleton normally costs {1}{B}; with Embalmer's Tools it costs just {B}.
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Reassembling Skeleton");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Without Embalmer's Tools the reduced mana is not enough for the same ability")
    void withoutToolsReducedManaIsInsufficient() {
        harness.setGraveyard(player1, List.of(new ReassemblingSkeleton()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    // ===== Activated: tap a Zombie, target player mills a card =====

    @Test
    @DisplayName("Tapping a Zombie makes target player mill a card")
    void tapZombieMillsTargetPlayer() {
        harness.addToBattlefield(player1, new EmbalmersTools());
        harness.addToBattlefield(player1, new Gravecrawler());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(findPermanent(player1, "Gravecrawler").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate the mill ability without an untapped Zombie")
    void cannotActivateMillWithoutZombie() {
        harness.addToBattlefield(player1, new EmbalmersTools());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
