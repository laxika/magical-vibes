package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveChargeCountersFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GolemFoundryTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has artifact-cast trigger with may charge counter")
    void hasArtifactCastTrigger() {
        GolemFoundry card = new GolemFoundry();

        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst())
                .isInstanceOf(MayEffect.class);
        MayEffect may = (MayEffect) card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst();
        assertThat(may.wrapped()).isInstanceOf(SpellCastTriggerEffect.class);
    }

    @Test
    @DisplayName("Has activated ability with remove 3 charge counters cost and token creation")
    void hasActivatedAbility() {
        GolemFoundry card = new GolemFoundry();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().getEffects())
                .anyMatch(e -> e instanceof RemoveChargeCountersFromSourceCost rc && rc.count() == 3)
                .anyMatch(e -> e instanceof CreateCreatureTokenEffect);
    }

    // ===== Charge counter trigger =====

    @Test
    @DisplayName("Casting an artifact spell triggers may-ability for charge counter")
    void castingArtifactTriggersCounter() {
        harness.addToBattlefield(player1, new GolemFoundry());
        harness.setHand(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.COLORLESS, 0);

        harness.castArtifact(player1, 0);

        // Trigger fires immediately at cast time — no resolution needed
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting the may-ability puts a charge counter on Golem Foundry")
    void acceptingAddsChargeCounter() {
        harness.addToBattlefield(player1, new GolemFoundry());
        harness.setHand(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.COLORLESS, 0);

        harness.castArtifact(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities(); // resolve charge counter triggered ability
        harness.passBothPriorities(); // resolve Spellbook

        Permanent foundry = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Golem Foundry"))
                .findFirst().orElseThrow();
        assertThat(foundry.getChargeCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the may-ability does not add a charge counter")
    void decliningDoesNotAddChargeCounter() {
        harness.addToBattlefield(player1, new GolemFoundry());
        harness.setHand(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.COLORLESS, 0);

        harness.castArtifact(player1, 0);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities(); // resolve Spellbook

        Permanent foundry = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Golem Foundry"))
                .findFirst().orElseThrow();
        assertThat(foundry.getChargeCounters()).isEqualTo(0);
    }

    @Test
    @DisplayName("Casting a non-artifact spell does not trigger")
    void nonArtifactSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new GolemFoundry());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        // No may-ability prompt for charge counter
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);

        Permanent foundry = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Golem Foundry"))
                .findFirst().orElseThrow();
        assertThat(foundry.getChargeCounters()).isEqualTo(0);
    }

    @Test
    @DisplayName("Opponent casting an artifact spell does not trigger")
    void opponentArtifactDoesNotTrigger() {
        harness.addToBattlefield(player1, new GolemFoundry());
        harness.setHand(player2, List.of(new Spellbook()));
        harness.addMana(player2, ManaColor.COLORLESS, 0);
        harness.forceActivePlayer(player2);

        harness.castArtifact(player2, 0);
        harness.passBothPriorities(); // resolve Spellbook

        Permanent foundry = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Golem Foundry"))
                .findFirst().orElseThrow();
        assertThat(foundry.getChargeCounters()).isEqualTo(0);
    }

    // ===== Activated ability — token creation =====

    @Test
    @DisplayName("Activating with 3 charge counters creates a 3/3 Golem token")
    void activateCreatesGolemToken() {
        harness.addToBattlefield(player1, new GolemFoundry());

        Permanent foundry = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Golem Foundry"))
                .findFirst().orElseThrow();
        foundry.setChargeCounters(3);

        int foundryIndex = gd.playerBattlefields.get(player1.getId()).indexOf(foundry);
        harness.activateAbility(player1, foundryIndex, null, null);
        harness.passBothPriorities(); // resolve activated ability

        // Charge counters are removed
        assertThat(foundry.getChargeCounters()).isEqualTo(0);

        // 3/3 Golem token is on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Golem")
                        && p.getCard().getPower() == 3
                        && p.getCard().getToughness() == 3
                        && p.getCard().getAdditionalTypes().contains(CardType.ARTIFACT));
    }

    @Test
    @DisplayName("Cannot activate with fewer than 3 charge counters")
    void cannotActivateWithFewerThanThreeCounters() {
        harness.addToBattlefield(player1, new GolemFoundry());

        Permanent foundry = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Golem Foundry"))
                .findFirst().orElseThrow();
        foundry.setChargeCounters(2);

        int foundryIndex = gd.playerBattlefields.get(player1.getId()).indexOf(foundry);
        assertThatThrownBy(() -> harness.activateAbility(player1, foundryIndex, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activating with more than 3 charge counters only removes 3")
    void activateRemovesExactlyThreeCounters() {
        harness.addToBattlefield(player1, new GolemFoundry());

        Permanent foundry = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Golem Foundry"))
                .findFirst().orElseThrow();
        foundry.setChargeCounters(5);

        int foundryIndex = gd.playerBattlefields.get(player1.getId()).indexOf(foundry);
        harness.activateAbility(player1, foundryIndex, null, null);
        harness.passBothPriorities();

        assertThat(foundry.getChargeCounters()).isEqualTo(2);
    }
}
