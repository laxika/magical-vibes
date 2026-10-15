package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HoardSmelterDragon.class, FountainOfYouth.class, GrizzlyBears.class,
        LeoninScimitar.class, RodOfRuin.class})
class HoardSmelterDragonTest extends BaseCardTest {


    @Test
    @DisplayName("Destroys target artifact and gets +X/+0 where X is that artifact's mana value")
    void destroysArtifactAndBoostsSelf() {
        Permanent dragon = addCreatureReady(player1, new HoardSmelterDragon());
        harness.addToBattlefield(player2, new RodOfRuin()); // MV = 4
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Rod of Ruin");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        // Rod of Ruin is destroyed
        harness.assertNotOnBattlefield(player2, "Rod of Ruin");
        harness.assertInGraveyard(player2, "Rod of Ruin");

        // Dragon gets +4/+0 (Rod of Ruin MV = 4), so effective power = 5 + 4 = 9
        assertThat(dragon.getEffectivePower()).isEqualTo(9);
        assertThat(dragon.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Destroying a 0-MV artifact does not boost power")
    void zeroManaValueDoesNotBoost() {
        Permanent dragon = addCreatureReady(player1, new HoardSmelterDragon());
        harness.addToBattlefield(player2, new FountainOfYouth()); // MV = 0
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        // Fountain of Youth is destroyed
        harness.assertNotOnBattlefield(player2, "Fountain of Youth");

        // Dragon power unchanged (MV = 0)
        assertThat(dragon.getEffectivePower()).isEqualTo(5);
    }

    @Test
    @DisplayName("Boost stacks when ability is activated multiple times")
    void boostStacks() {
        Permanent dragon = addCreatureReady(player1, new HoardSmelterDragon());
        harness.addToBattlefield(player2, new LeoninScimitar()); // MV = 1
        harness.addToBattlefield(player2, new RodOfRuin());      // MV = 4
        harness.addMana(player1, ManaColor.RED, 8);

        // Destroy Leonin Scimitar first
        UUID scimitarId = harness.getPermanentId(player2, "Leonin Scimitar");
        harness.activateAbility(player1, 0, null, scimitarId);
        harness.passBothPriorities();

        assertThat(dragon.getEffectivePower()).isEqualTo(6); // 5 + 1

        // Destroy Rod of Ruin second
        UUID rodId = harness.getPermanentId(player2, "Rod of Ruin");
        harness.activateAbility(player1, 0, null, rodId);
        harness.passBothPriorities();

        assertThat(dragon.getEffectivePower()).isEqualTo(10); // 5 + 1 + 4
    }


    @Test
    @DisplayName("Cannot target a creature with the ability")
    void cannotTargetCreature() {
        addCreatureReady(player1, new HoardSmelterDragon());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 4);

        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }


    @Test
    @DisplayName("Ability fizzles if target artifact is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent dragon = addCreatureReady(player1, new HoardSmelterDragon());
        harness.addToBattlefield(player2, new RodOfRuin());
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Rod of Ruin");
        harness.activateAbility(player1, 0, null, targetId);

        // Remove the target before resolution
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        // Dragon gets no boost
        assertThat(dragon.getEffectivePower()).isEqualTo(5);
    }


    @Test
    @DisplayName("Boost applies even if target artifact is indestructible")
    void boostAppliesEvenIfIndestructible() {
        Permanent dragon = addCreatureReady(player1, new HoardSmelterDragon());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new RodOfRuin());
        artifact.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();

        // Artifact survives (indestructible)
        harness.assertOnBattlefield(player2, "Rod of Ruin");

        // Dragon still gets the boost
        assertThat(dragon.getEffectivePower()).isEqualTo(9); // 5 + 4
    }


    @Test
    @DisplayName("Power bonus expires during cleanup")
    void boostExpiresAtEndOfTurn() {
        Permanent dragon = addCreatureReady(player1, new HoardSmelterDragon());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new RodOfRuin());
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();
        assertThat(dragon.getEffectivePower()).isEqualTo(9);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);

        assertThat(dragon.getEffectivePower()).isEqualTo(5);
        assertThat(dragon.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Can destroy an artifact controlled by the Dragon's controller")
    void canTargetOwnArtifact() {
        Permanent dragon = addCreatureReady(player1, new HoardSmelterDragon());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new RodOfRuin());
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Rod of Ruin");
        harness.assertInGraveyard(player1, "Rod of Ruin");
        assertThat(dragon.getEffectivePower()).isEqualTo(9);
    }

    @Test
    @DisplayName("Artifact is still destroyed if the Dragon leaves before resolution")
    void destroysArtifactAfterSourceLeaves() {
        Permanent dragon = addCreatureReady(player1, new HoardSmelterDragon());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new RodOfRuin());
        harness.addMana(player1, ManaColor.RED, 4);
        harness.activateAbility(player1, 0, null, artifact.getId());

        gd.playerBattlefields.get(player1.getId()).remove(dragon);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Rod of Ruin");
        harness.assertInGraveyard(player2, "Rod of Ruin");
    }

    @Test
    @DisplayName("Ability can be activated while the Dragon is summoning sick and tapped")
    void canActivateWithoutTapCost() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new HoardSmelterDragon());
        dragon.setSummoningSick(true);
        dragon.tap();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new RodOfRuin());
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Rod of Ruin");
        assertThat(dragon.getEffectivePower()).isEqualTo(9);
        assertThat(dragon.isTapped()).isTrue();
    }
}
