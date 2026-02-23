package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToTargetEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AbunaAcolyteTest extends BaseCardTest {

    private void addAcolyteReady() {
        harness.addToBattlefield(player1, new AbunaAcolyte());
        Permanent acolyte = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Abuna Acolyte"))
                .findFirst().orElseThrow();
        acolyte.setSummoningSick(false);
    }

    // ===== Card structure =====

    @Test
    @DisplayName("Has two tap activated abilities")
    void hasCorrectAbilities() {
        AbunaAcolyte card = new AbunaAcolyte();

        assertThat(card.getActivatedAbilities()).hasSize(2);

        ActivatedAbility ability1 = card.getActivatedAbilities().get(0);
        assertThat(ability1.isRequiresTap()).isTrue();
        assertThat(ability1.getManaCost()).isNull();
        assertThat(ability1.getEffects()).hasSize(1);
        assertThat(ability1.getEffects().getFirst()).isInstanceOf(PreventDamageToTargetEffect.class);
        assertThat(((PreventDamageToTargetEffect) ability1.getEffects().getFirst()).amount()).isEqualTo(1);
        assertThat(ability1.isNeedsTarget()).isTrue();
        assertThat(ability1.getTargetFilter()).isNull();

        ActivatedAbility ability2 = card.getActivatedAbilities().get(1);
        assertThat(ability2.isRequiresTap()).isTrue();
        assertThat(ability2.getManaCost()).isNull();
        assertThat(ability2.getEffects()).hasSize(1);
        assertThat(ability2.getEffects().getFirst()).isInstanceOf(PreventDamageToTargetEffect.class);
        assertThat(((PreventDamageToTargetEffect) ability2.getEffects().getFirst()).amount()).isEqualTo(2);
        assertThat(ability2.isNeedsTarget()).isTrue();
        assertThat(ability2.getTargetFilter()).isInstanceOf(PermanentPredicateTargetFilter.class);
    }

    // ===== Ability 1: Prevent 1 damage to any target =====

    @Test
    @DisplayName("Ability 1 adds 1 prevention shield to target creature")
    void ability1PreventsOnCreature() {
        addAcolyteReady();
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(bears.getDamagePreventionShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability 1 adds 1 prevention shield to target player")
    void ability1PreventsOnPlayer() {
        addAcolyteReady();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability 1 prevention shield saves creature from lethal combat damage")
    void ability1ShieldSavesCreature() {
        // Set up defender (1/1) with 1 prevention shield, blocking a 1/1 attacker
        AbunaAcolyte acolyteCard = new AbunaAcolyte();
        Permanent defender = new Permanent(acolyteCard);
        defender.setSummoningSick(false);
        defender.setDamagePreventionShield(1);
        defender.setBlocking(true);
        defender.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(defender);

        AbunaAcolyte attackerCard = new AbunaAcolyte();
        Permanent attacker = new Permanent(attackerCard);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Defender took 1 damage - 1 prevented = 0 effective → survives
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Abuna Acolyte"));
    }

    // ===== Ability 2: Prevent 2 damage to target artifact creature =====

    @Test
    @DisplayName("Ability 2 adds 2 prevention shield to target artifact creature")
    void ability2PreventsOnArtifactCreature() {
        addAcolyteReady();
        harness.addToBattlefield(player1, new BottleGnomes());

        UUID targetId = harness.getPermanentId(player1, "Bottle Gnomes");
        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        Permanent gnomes = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Bottle Gnomes"))
                .findFirst().orElseThrow();
        assertThat(gnomes.getDamagePreventionShield()).isEqualTo(2);
    }

    @Test
    @DisplayName("Ability 2 cannot target non-artifact creature")
    void ability2CannotTargetNonArtifactCreature() {
        addAcolyteReady();
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Tap / summoning sickness restrictions =====

    @Test
    @DisplayName("Cannot activate with summoning sickness")
    void respectsSummoningSickness() {
        harness.addToBattlefield(player1, new AbunaAcolyte());
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenTapped() {
        addAcolyteReady();
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Tap the acolyte
        Permanent acolyte = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Abuna Acolyte"))
                .findFirst().orElseThrow();
        acolyte.tap();

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
