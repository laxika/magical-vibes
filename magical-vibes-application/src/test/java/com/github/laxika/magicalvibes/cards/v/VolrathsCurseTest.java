package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VolrathsCurseTest extends BaseCardTest {

    /**
     * Adds {@code count} filler creatures to {@code playerId}'s battlefield. The sacrifice ability
     * is activated by a player who does not control the Aura, and the activation API addresses such
     * a permanent by an index that must fall outside the activator's own battlefield — the filler
     * pushes the Curse to a high enough index.
     */
    private void addFillers(java.util.UUID playerId, int count) {
        for (int i = 0; i < count; i++) {
            Permanent filler = new Permanent(new GrizzlyBears());
            filler.setSummoningSick(false);
            gd.playerBattlefields.get(playerId).add(filler);
        }
    }

    @Test
    @DisplayName("Enchanted creature cannot attack")
    void enchantedCreatureCannotAttack() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent curse = new Permanent(new VolrathsCurse());
        curse.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player2.getId()).add(curse);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Enchanted creature cannot activate its abilities")
    void enchantedCreatureCannotActivateAbilities() {
        Permanent gnomes = new Permanent(new BottleGnomes());
        gnomes.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(gnomes);

        Permanent curse = new Permanent(new VolrathsCurse());
        curse.setAttachedTo(gnomes.getId());
        gd.playerBattlefields.get(player2.getId()).add(curse);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("The enchanted creature's controller sacrifices a permanent to unlock its abilities this turn")
    void sacrificingAPermanentIgnoresTheCurse() {
        Permanent gnomes = new Permanent(new BottleGnomes());
        gnomes.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(gnomes);
        Permanent fountain = new Permanent(new FountainOfYouth());
        gd.playerBattlefields.get(player1.getId()).add(fountain);

        addFillers(player2.getId(), 2);
        Permanent curse = new Permanent(new VolrathsCurse());
        curse.setAttachedTo(gnomes.getId());
        gd.playerBattlefields.get(player2.getId()).add(curse);

        harness.activateAbility(player1, 2, 0, null, null);
        harness.handlePermanentChosen(player1, fountain.getId());
        harness.passBothPriorities();

        assertThat(curse.isAuraEffectsIgnoredThisTurn()).isTrue();
        harness.assertInGraveyard(player1, "Fountain of Youth");

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("The ignore effect wears off at end of turn")
    void ignoreEffectWearsOffAtEndOfTurn() {
        Permanent gnomes = new Permanent(new BottleGnomes());
        gnomes.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(gnomes);
        Permanent fountain = new Permanent(new FountainOfYouth());
        gd.playerBattlefields.get(player1.getId()).add(fountain);

        addFillers(player2.getId(), 2);
        Permanent curse = new Permanent(new VolrathsCurse());
        curse.setAttachedTo(gnomes.getId());
        gd.playerBattlefields.get(player2.getId()).add(curse);

        harness.activateAbility(player1, 2, 0, null, null);
        harness.handlePermanentChosen(player1, fountain.getId());
        harness.passBothPriorities();
        assertThat(curse.isAuraEffectsIgnoredThisTurn()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(curse.isAuraEffectsIgnoredThisTurn()).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Ignoring the Curse lets the enchanted creature attack")
    void ignoringTheCurseLetsTheCreatureAttack() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);
        Permanent fountain = new Permanent(new FountainOfYouth());
        gd.playerBattlefields.get(player1.getId()).add(fountain);

        addFillers(player2.getId(), 2);
        Permanent curse = new Permanent(new VolrathsCurse());
        curse.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player2.getId()).add(curse);

        harness.forceActivePlayer(player1);
        harness.activateAbility(player1, 2, 0, null, null);
        harness.handlePermanentChosen(player1, fountain.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(bears.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("The Aura's controller may not activate the sacrifice ability")
    void auraControllerCannotActivateSacrificeAbility() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent curse = new Permanent(new VolrathsCurse());
        curse.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player2.getId()).add(curse);
        Permanent fountain = new Permanent(new FountainOfYouth());
        gd.playerBattlefields.get(player2.getId()).add(fountain);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("enchanted permanent's controller");
    }

    @Test
    @DisplayName("{1}{U} returns the Aura to its owner's hand, freeing the creature")
    void bounceAbilityReturnsAuraToHand() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent curse = new Permanent(new VolrathsCurse());
        curse.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player2.getId()).add(curse);

        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.activateAbility(player2, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Volrath's Curse"));

        // With the Aura gone the creature can be declared as an attacker again.
        harness.forceActivePlayer(player1);
        harness.beginAttackerDeclarationInput();
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gs.declareAttackers(gd, player1, List.of(0));
    }
}
