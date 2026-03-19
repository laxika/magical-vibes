package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VectorAspTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Vector Asp has one activated ability granting infect")
    void hasInfectActivatedAbility() {
        VectorAsp card = new VectorAsp();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{B}");
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().get(0).isNeedsTarget()).isFalse();
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(GrantKeywordEffect.class);
        GrantKeywordEffect infect = (GrantKeywordEffect) card.getActivatedAbilities().get(0).getEffects().getFirst();
        assertThat(infect.keyword()).isEqualTo(Keyword.INFECT);
        assertThat(infect.scope()).isEqualTo(GrantScope.SELF);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Vector Asp puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new VectorAsp()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Vector Asp");
    }

    @Test
    @DisplayName("Resolving puts Vector Asp onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new VectorAsp()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Vector Asp"));
    }

    // ===== Infect ability =====

    @Test
    @DisplayName("Activating infect ability puts it on the stack")
    void activatingInfectPutsOnStack() {
        Permanent asp = addAspReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Vector Asp");
        assertThat(entry.getTargetId()).isEqualTo(asp.getId());
    }

    @Test
    @DisplayName("Resolving infect ability grants infect until end of turn")
    void resolvingInfectAbilityGrantsInfect() {
        Permanent asp = addAspReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, asp, Keyword.INFECT)).isTrue();
    }

    @Test
    @DisplayName("Infect granted by ability resets at end of turn cleanup")
    void infectResetsAtEndOfTurn() {
        Permanent asp = addAspReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, asp, Keyword.INFECT)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, asp, Keyword.INFECT)).isFalse();
    }

    // ===== Activation constraints =====

    @Test
    @DisplayName("Activating ability does NOT tap Vector Asp")
    void activatingAbilityDoesNotTap() {
        Permanent asp = addAspReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(asp.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate ability without black mana")
    void cannotActivateWithoutBlackMana() {
        addAspReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Can activate ability when tapped")
    void canActivateWhenTapped() {
        Permanent asp = addAspReady(player1);
        asp.tap();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Vector Asp");
    }

    @Test
    @DisplayName("Can activate ability with summoning sickness")
    void canActivateWithSummoningSickness() {
        Permanent asp = new Permanent(new VectorAsp());
        gd.playerBattlefields.get(player1.getId()).add(asp);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Vector Asp");
    }

    // ===== Combat with infect =====

    @Test
    @DisplayName("Vector Asp with infect deals poison counters to defending player when unblocked")
    void dealsPoison() {
        Permanent asp = addAspReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        // Activate infect ability and resolve it
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        asp.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(1);
        // Infect does not deal regular damage to players
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Vector Asp without infect deals regular damage to defending player")
    void dealsRegularDamageWithoutInfect() {
        harness.setLife(player2, 20);
        Permanent asp = addAspReady(player1);
        asp.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(0);
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Ability fizzles if Vector Asp is removed before resolution")
    void abilityFizzlesIfSourceRemoved() {
        addAspReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    // ===== Helper methods =====

    private Permanent addAspReady(Player player) {
        Permanent perm = new Permanent(new VectorAsp());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
