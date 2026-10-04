package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.SilentDart;
import com.github.laxika.magicalvibes.cards.s.SiegeWurm;
import com.github.laxika.magicalvibes.cards.i.IzzetGuildgate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinCratermaker.class, SiegeWurm.class, SilentDart.class, IzzetGuildgate.class})
class GoblinCratermakerTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifice ability deals 2 damage to target creature")
    void dealsTwoDamageToTargetCreature() {
        addCreatureReady(player1, new GoblinCratermaker());
        Permanent target = addCreatureReady(player2, new SiegeWurm());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Goblin Cratermaker");
    }

    @Test
    @DisplayName("Sacrifice ability destroys target colorless nonland permanent")
    void destroysTargetColorlessNonlandPermanent() {
        addCreatureReady(player1, new GoblinCratermaker());
        harness.addToBattlefieldAndReturn(player2, new SilentDart());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Silent Dart");
        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Goblin Cratermaker");
        harness.assertInGraveyard(player2, "Silent Dart");
    }

    @Test
    @DisplayName("Damage ability cannot target a noncreature permanent")
    void damageAbilityCannotTargetNoncreature() {
        addCreatureReady(player1, new GoblinCratermaker());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SilentDart());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Destruction ability cannot target a colored permanent")
    void destructionAbilityCannotTargetColoredPermanent() {
        addCreatureReady(player1, new GoblinCratermaker());
        Permanent target = addCreatureReady(player2, new SiegeWurm());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Destruction ability cannot target a land")
    void destructionAbilityCannotTargetLand() {
        addCreatureReady(player1, new GoblinCratermaker());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IzzetGuildgate());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A tapped summoning-sick Cratermaker can activate and pays the sacrifice before resolution")
    void activatesWithoutTapCostAndSacrificesImmediately() {
        Permanent cratermaker = harness.addToBattlefieldAndReturn(player1, new GoblinCratermaker());
        cratermaker.setSummoningSick(true);
        cratermaker.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SiegeWurm());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Goblin Cratermaker");
        harness.assertInGraveyard(player1, "Goblin Cratermaker");
        assertThat(target.getMarkedDamage()).isZero();

        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Damage mode kills a creature with two toughness")
    void damageKillsCreature() {
        harness.addToBattlefield(player1, new GoblinCratermaker());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoblinCratermaker());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Goblin Cratermaker");
        harness.assertInGraveyard(player2, "Goblin Cratermaker");
        harness.assertNotOnBattlefield(player2, "Goblin Cratermaker");
    }

    @Test
    @DisplayName("Destruction mode can target an artifact you control")
    void destroysOwnArtifact() {
        harness.addToBattlefield(player1, new GoblinCratermaker());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SilentDart());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Silent Dart");
        harness.assertInGraveyard(player1, "Goblin Cratermaker");
    }

    @Test
    @DisplayName("Damage mode cannot target a player")
    void cannotDamagePlayer() {
        harness.addToBattlefield(player1, new GoblinCratermaker());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Goblin Cratermaker");
    }

    @Test
    @DisplayName("Cratermaker can target itself but the sacrificed target is gone at resolution")
    void canTargetItself() {
        Permanent cratermaker = harness.addToBattlefieldAndReturn(player1, new GoblinCratermaker());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, cratermaker.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Goblin Cratermaker");
        harness.assertNotOnBattlefield(player1, "Goblin Cratermaker");
        assertThat(cratermaker.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Activation requires one mana and does not sacrifice the source when mana is missing")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new GoblinCratermaker());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SiegeWurm());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Goblin Cratermaker");
        assertThat(target.getMarkedDamage()).isZero();
    }
}
