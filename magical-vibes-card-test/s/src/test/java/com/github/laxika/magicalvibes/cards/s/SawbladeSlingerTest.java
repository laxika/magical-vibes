package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.Gravecrawler;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SawbladeSlinger.class, Gravecrawler.class, Ornithopter.class})
class SawbladeSlingerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB mode destroys an artifact an opponent controls")
    void destroysOpponentArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        castSlinger(0, artifact.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(artifact);
        harness.assertInGraveyard(player2, "Ornithopter");
    }

    @Test
    @DisplayName("ETB mode fights a Zombie an opponent controls")
    void fightsOpponentZombie() {
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new Gravecrawler());

        castSlinger(1, zombie.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Gravecrawler");
        Permanent slinger = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(slinger.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("ETB can choose no mode")
    void choosesNoMode() {
        castSlinger(-1, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Sawblade Slinger");
    }

    @Test
    @DisplayName("ETB cannot target an artifact controlled by its controller")
    void cannotTargetOwnArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());

        assertThatThrownBy(() -> castSlinger(0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent controls");
    }

    private void castSlinger(int mode, UUID targetId) {
        harness.setHand(player1, java.util.List.of(new SawbladeSlinger()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        if (targetId == null) {
            harness.castCreature(player1, 0, mode);
        } else {
            harness.castCreature(player1, 0, mode, targetId);
        }
    }

    @Test
    @DisplayName("Entering without being cast still allows choosing the fight mode")
    void canChooseFightWhenEnteringWithoutBeingCast() {
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new Gravecrawler());
        Permanent slinger = harness.enterBattlefieldAndReturn(player1, new SawbladeSlinger());
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .processNextTriggeredModalTrigger(gd));

        harness.handleListChoice(player1, "This creature fights target Zombie an opponent controls");
        harness.handlePermanentChosen(player1, zombie.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Gravecrawler");
        assertThat(slinger.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Fight mode cannot target its controller's Zombie")
    void cannotFightOwnZombie() {
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new Gravecrawler());

        assertThatThrownBy(() -> castSlinger(1, zombie.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent controls");
    }

    @Test
    @DisplayName("Fight mode cannot target an opposing non-Zombie")
    void cannotFightNonZombie() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SawbladeSlinger());

        assertThatThrownBy(() -> castSlinger(1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Artifact mode cannot target an opposing nonartifact Zombie")
    void cannotDestroyNonArtifact() {
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new Gravecrawler());

        assertThatThrownBy(() -> castSlinger(0, zombie.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("No fight damage is dealt if the Slinger leaves before its trigger resolves")
    void noFightWhenSourceLeaves() {
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new Gravecrawler());
        castSlinger(1, zombie.getId());
        harness.passBothPriorities();
        Permanent slinger = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, slinger));

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Gravecrawler");
        assertThat(zombie.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Artifact destruction still resolves after the Slinger leaves")
    void destroysArtifactWhenSourceLeaves() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        castSlinger(0, artifact.getId());
        harness.passBothPriorities();
        Permanent slinger = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, slinger));

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Ornithopter");
        harness.assertNotOnBattlefield(player2, "Ornithopter");
    }
}
