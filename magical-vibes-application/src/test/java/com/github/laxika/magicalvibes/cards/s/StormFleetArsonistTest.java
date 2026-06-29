package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.RaidConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerSacrificesPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StormFleetArsonistTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has raid-conditional ETB sacrifice effect targeting opponent")
    void hasRaidEtbSacrificeEffect() {
        StormFleetArsonist card = new StormFleetArsonist();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.getTargetFilter()).isInstanceOf(PlayerPredicateTargetFilter.class);

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(RaidConditionalEffect.class);

        RaidConditionalEffect raid =
                (RaidConditionalEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(raid.wrapped()).isInstanceOf(TargetPlayerSacrificesPermanentsEffect.class);

        TargetPlayerSacrificesPermanentsEffect sacrifice =
                (TargetPlayerSacrificesPermanentsEffect) raid.wrapped();
        assertThat(sacrifice.count()).isEqualTo(1);
        assertThat(sacrifice.filter()).isInstanceOf(PermanentTruePredicate.class);
    }

    // ===== ETB with raid met =====

    @Test
    @DisplayName("ETB triggers sacrifice when raid is met (attacked this turn)")
    void etbTriggersWithRaid() {
        markAttackedThisTurn();
        castStormFleetArsonist();
        harness.passBothPriorities(); // resolve creature spell

        // ETB trigger should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Storm Fleet Arsonist");
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("ETB raid trigger makes target opponent sacrifice their only permanent")
    void etbMakesOpponentSacrificeOnlyPermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        markAttackedThisTurn();
        castStormFleetArsonist();

        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        // Opponent's only permanent should be auto-sacrificed
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("ETB raid trigger prompts opponent to choose when they have multiple permanents")
    void etbPromptsChoiceWithMultiplePermanents() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        markAttackedThisTurn();
        castStormFleetArsonist();

        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        // Opponent should be prompted to choose which permanent to sacrifice
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_PERMANENT_CHOICE);
        assertThat(gd.pendingForcedSacrificeCount).isEqualTo(1);
        assertThat(gd.pendingForcedSacrificePlayerId).isEqualTo(player2.getId());

        // Player2 chooses the first permanent
        List<Permanent> p2Battlefield = gd.playerBattlefields.get(player2.getId());
        UUID chosen = p2Battlefield.getFirst().getId();
        harness.handleMultiplePermanentsChosen(player2, List.of(chosen));

        // One sacrificed, one remains
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("ETB raid trigger does nothing when opponent has no permanents")
    void etbDoesNothingWithNoPermanents() {
        markAttackedThisTurn();
        castStormFleetArsonist();

        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("no permanents to sacrifice"));
    }

    // ===== ETB without raid =====

    @Test
    @DisplayName("ETB does NOT trigger without raid (did not attack this turn)")
    void etbDoesNotTriggerWithoutRaid() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castStormFleetArsonist();
        harness.passBothPriorities(); // resolve creature spell

        // No ETB trigger on the stack
        assertThat(gd.stack).isEmpty();

        // Creature is on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Storm Fleet Arsonist"));

        // Opponent's permanent unchanged
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    // ===== Raid lost before resolution (intervening-if) =====

    @Test
    @DisplayName("ETB does nothing if raid condition is lost before resolution")
    void etbFizzlesWhenRaidLost() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        markAttackedThisTurn();
        castStormFleetArsonist();
        harness.passBothPriorities(); // resolve creature spell — ETB trigger on stack

        // Remove the raid flag before ETB resolves
        gd.playersDeclaredAttackersThisTurn.clear();

        harness.passBothPriorities(); // resolve ETB trigger — raid no longer met

        // Opponent's permanent should still be there
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.gameLog).anyMatch(log -> log.contains("raid ability does nothing"));
    }

    // ===== Creature enters battlefield regardless =====

    @Test
    @DisplayName("Creature enters battlefield even without raid")
    void creatureEntersWithoutRaid() {
        castStormFleetArsonist();
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Storm Fleet Arsonist"));
    }

    // ===== Targeting =====

    @Test
    @DisplayName("Cannot cast targeting yourself")
    void cannotTargetYourself() {
        harness.setHand(player1, List.of(new StormFleetArsonist()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.getGameService().playCard(gd, player1, 0, 0, player1.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    // ===== Helpers =====

    private void markAttackedThisTurn() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
    }

    private void castStormFleetArsonist() {
        harness.setHand(player1, List.of(new StormFleetArsonist()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.getGameService().playCard(gd, player1, 0, 0, player2.getId(), null);
    }
}
