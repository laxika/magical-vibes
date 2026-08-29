package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuicksmithRebelTest extends BaseCardTest {

    @Test
    @DisplayName("The targeted artifact deals 2 damage to a creature")
    void artifactDealsDamageToCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AngelsFeather());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castRebel(artifact.getId());

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The targeted artifact deals 2 damage to a player")
    void artifactDealsDamageToPlayer() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AngelsFeather());
        harness.setLife(player2, 20);
        castRebel(artifact.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The granted ability ends when Quicksmith Rebel leaves the battlefield")
    void grantedAbilityEndsWhenRebelLeaves() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AngelsFeather());
        castRebel(artifact.getId());

        Permanent rebel = harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Quicksmith Rebel"))
                .findFirst().orElseThrow();
        destroyRebel(rebel.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("The ETB target must be an artifact you control")
    void rejectsIllegalTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new QuicksmithRebel()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact you control");
    }

    private void castRebel(UUID targetId) {
        harness.setHand(player1, List.of(new QuicksmithRebel()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void destroyRebel(UUID targetId) {
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
