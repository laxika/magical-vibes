package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.Gravecrawler;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SawbladeSlinger.class, Gravecrawler.class, Ornithopter.class})
class SawbladeSlingerTest extends BaseCardTest {

    private static final String ARTIFACT_MODE = "Destroy target artifact an opponent controls";
    private static final String FIGHT_MODE = "This creature fights target Zombie an opponent controls";

    @Test
    @DisplayName("ETB mode destroys an artifact an opponent controls")
    void destroysOpponentArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        castSlinger(ARTIFACT_MODE, artifact.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(artifact);
        harness.assertInGraveyard(player2, "Ornithopter");
    }

    @Test
    @DisplayName("ETB mode fights a Zombie an opponent controls")
    void fightsOpponentZombie() {
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new Gravecrawler());

        castSlinger(FIGHT_MODE, zombie.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Gravecrawler");
        Permanent slinger = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(slinger.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("ETB can choose no mode")
    void choosesNoMode() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        castSlinger(ChooseOneEffect.NO_MODE_LABEL, null);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(artifact);
        harness.assertOnBattlefield(player1, "Sawblade Slinger");
    }

    @Test
    @DisplayName("ETB cannot target an artifact controlled by its controller")
    void cannotTargetOwnArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.addToBattlefield(player2, new Gravecrawler());

        assertThatThrownBy(() -> castSlinger(ARTIFACT_MODE, artifact.getId()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
    }

    private void castSlinger(String mode, UUID targetId) {
        harness.setHand(player1, java.util.List.of(new SawbladeSlinger()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, mode);
        if (targetId != null) {
            harness.handlePermanentChosen(player1, targetId);
        }
    }

    @Test
    @DisplayName("Entering without being cast still allows choosing the fight mode")
    void canChooseFightWhenEnteringWithoutBeingCast() {
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new Gravecrawler());
        Permanent slinger = harness.enterBattlefieldAndReturn(player1, new SawbladeSlinger());
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .processNextTriggeredModalTrigger(gd));

        harness.handleListChoice(player1, FIGHT_MODE);
        harness.handlePermanentChosen(player1, zombie.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Gravecrawler");
        assertThat(slinger.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Fight mode cannot target its controller's Zombie")
    void cannotFightOwnZombie() {
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new Gravecrawler());
        harness.addToBattlefield(player2, new Ornithopter());

        assertThatThrownBy(() -> castSlinger(FIGHT_MODE, zombie.getId()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(zombie.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Fight mode cannot target an opposing non-Zombie")
    void cannotFightNonZombie() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SawbladeSlinger());
        harness.addToBattlefield(player2, new Ornithopter());

        assertThatThrownBy(() -> castSlinger(FIGHT_MODE, creature.getId()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(creature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Artifact mode cannot target an opposing nonartifact Zombie")
    void cannotDestroyNonArtifact() {
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new Gravecrawler());

        assertThatThrownBy(() -> castSlinger(ARTIFACT_MODE, zombie.getId()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(zombie.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("No fight damage is dealt if the Slinger leaves before its trigger resolves")
    void noFightWhenSourceLeaves() {
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new Gravecrawler());
        castSlinger(FIGHT_MODE, zombie.getId());
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
        castSlinger(ARTIFACT_MODE, artifact.getId());
        Permanent slinger = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, slinger));

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Ornithopter");
        harness.assertNotOnBattlefield(player2, "Ornithopter");
    }
}
