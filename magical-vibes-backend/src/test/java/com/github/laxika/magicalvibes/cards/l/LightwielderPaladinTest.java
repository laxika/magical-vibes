package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ExilePermanentDamagedPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.cards.b.BlackKnight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LightwielderPaladinTest extends BaseCardTest {

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        GameData gd = harness.getGameData();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Has MayEffect-wrapped ExilePermanentDamagedPlayerControlsEffect combat damage trigger")
    void hasCorrectEffect() {
        LightwielderPaladin card = new LightwielderPaladin();

        assertThat(card.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER).getFirst())
                .isInstanceOf(MayEffect.class);
        MayEffect may = (MayEffect) card.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER).getFirst();
        assertThat(may.wrapped()).isInstanceOf(ExilePermanentDamagedPlayerControlsEffect.class);
    }

    @Test
    @DisplayName("Combat damage trigger presents may ability choice when defender has black permanent")
    void combatDamageTriggerPresentsMayChoice() {
        Permanent paladin = addReadyCreature(player1, new LightwielderPaladin());
        paladin.setAttacking(true);
        addReadyCreature(player2, new BlackKnight());

        resolveCombat();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
    }

    @Test
    @DisplayName("Accepting may and choosing a black permanent exiles it")
    void exilesBlackPermanent() {
        Permanent paladin = addReadyCreature(player1, new LightwielderPaladin());
        paladin.setAttacking(true);
        Permanent blackKnight = addReadyCreature(player2, new BlackKnight());

        resolveCombat();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_PERMANENT_CHOICE);
        assertThat(gd.interaction.multiPermanentChoiceContext().validIds())
                .contains(blackKnight.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(blackKnight.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Black Knight"));
        assertThat(gd.exile).anyMatch(c -> c.getName().equals("Black Knight"));
    }

    @Test
    @DisplayName("Accepting may and choosing a red permanent exiles it")
    void exilesRedPermanent() {
        Permanent paladin = addReadyCreature(player1, new LightwielderPaladin());
        paladin.setAttacking(true);
        Permanent dragon = addReadyCreature(player2, new ShivanDragon());

        resolveCombat();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_PERMANENT_CHOICE);
        harness.handleMultiplePermanentsChosen(player1, List.of(dragon.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Shivan Dragon"));
        assertThat(gd.exile).anyMatch(c -> c.getName().equals("Shivan Dragon"));
    }

    @Test
    @DisplayName("Declining the may ability does nothing")
    void declineDoesNothing() {
        Permanent paladin = addReadyCreature(player1, new LightwielderPaladin());
        paladin.setAttacking(true);
        Permanent blackKnight = addReadyCreature(player2, new BlackKnight());

        resolveCombat();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Black Knight"));
        assertThat(gd.gameLog).anyMatch(log -> log.contains("declines"));
    }

    @Test
    @DisplayName("No trigger when defender has no black or red permanents")
    void noTriggerWhenNoValidTargets() {
        Permanent paladin = addReadyCreature(player1, new LightwielderPaladin());
        paladin.setAttacking(true);
        addReadyCreature(player2, new GrizzlyBears()); // green, not a valid target

        resolveCombat();

        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
    }

    @Test
    @DisplayName("No trigger when defender has no permanents")
    void noTriggerWhenNoPermanents() {
        Permanent paladin = addReadyCreature(player1, new LightwielderPaladin());
        paladin.setAttacking(true);

        resolveCombat();

        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
    }

    @Test
    @DisplayName("Valid targets only include black or red permanents of the damaged player")
    void onlyBlackOrRedFromDamagedPlayer() {
        Permanent paladin = addReadyCreature(player1, new LightwielderPaladin());
        paladin.setAttacking(true);
        Permanent ownBlack = addReadyCreature(player1, new BlackKnight());  // own black permanent
        Permanent enemyGreen = addReadyCreature(player2, new GrizzlyBears()); // enemy green
        Permanent enemyBlack = addReadyCreature(player2, new BlackKnight()); // enemy black

        resolveCombat();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_PERMANENT_CHOICE);
        assertThat(gd.interaction.multiPermanentChoiceContext().validIds())
                .contains(enemyBlack.getId())
                .doesNotContain(ownBlack.getId())
                .doesNotContain(enemyGreen.getId());
    }

    @Test
    @DisplayName("Paladin deals combat damage even if may is declined")
    void dealsCombatDamage() {
        harness.setLife(player2, 20);
        Permanent paladin = addReadyCreature(player1, new LightwielderPaladin());
        paladin.setAttacking(true);
        addReadyCreature(player2, new BlackKnight());

        resolveCombat();

        harness.handleMayAbilityChosen(player1, false);

        // Lightwielder Paladin is 4/4, should deal 4 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Game advances after exile choice is made")
    void gameAdvancesAfterChoice() {
        Permanent paladin = addReadyCreature(player1, new LightwielderPaladin());
        paladin.setAttacking(true);
        Permanent blackKnight = addReadyCreature(player2, new BlackKnight());

        resolveCombat();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(blackKnight.getId()));

        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }
}
