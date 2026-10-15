package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeepfathomSkulker.class, Forest.class, FountainOfYouth.class, GrizzlyBears.class})
class DeepfathomSkulkerTest extends BaseCardTest {

    @Test
    @DisplayName("A creature you control dealing combat damage presents a may-draw choice")
    void combatDamagePresentsMayDrawChoice() {
        addCreatureReady(player1, new DeepfathomSkulker());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        harness.setLibrary(player1, List.of(new Forest()));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting the combat-damage trigger draws a card")
    void acceptingCombatDamageTriggerDrawsCard() {
        addCreatureReady(player1, new DeepfathomSkulker());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        harness.setLibrary(player1, List.of(new Forest()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card instanceof Forest);
    }

    @Test
    @DisplayName("The ability makes a target creature unblockable for the turn")
    void abilityMakesTargetCreatureUnblockable() {
        addCreatureReady(player1, new DeepfathomSkulker());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        addAbilityMana();

        harness.activateAbility(player1, 0, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.isCantBeBlocked()).isTrue();
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("The ability cannot target a noncreature permanent")
    void abilityCannotTargetNoncreature() {
        addCreatureReady(player1, new DeepfathomSkulker());
        Permanent target = addCreatureReady(player2, new FountainOfYouth());
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Skulker triggers for its own combat damage and drawing can be declined")
    void ownCombatDamageCanBeDeclined() {
        Permanent skulker = addCreatureReady(player1, new DeepfathomSkulker());
        skulker.setAttacking(true);
        harness.setLibrary(player1, List.of(new Forest()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each creature dealing combat damage creates a separate optional draw")
    void multipleCreaturesTriggerSeparately() {
        addCreatureReady(player1, new DeepfathomSkulker());
        addCreatureReady(player1, new GrizzlyBears()).setAttacking(true);
        addCreatureReady(player1, new GrizzlyBears()).setAttacking(true);
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);

        if (!gd.interaction.isAwaitingInput()) {
            harness.passBothPriorities();
        }
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's combat damage does not trigger Skulker")
    void opposingCreatureDoesNotTrigger() {
        addCreatureReady(player1, new DeepfathomSkulker());
        addCreatureReady(player2, new GrizzlyBears()).setAttacking(true);
        harness.setLibrary(player1, List.of(new Forest()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player2);
        harness.resolveCombatDamage();

        harness.assertLife(player1, 18);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("Colored mana cannot pay the colorless symbol")
    void abilityRequiresColorlessMana() {
        Permanent skulker = addCreatureReady(player1, new DeepfathomSkulker());
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, skulker.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(skulker.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("The ability requires four mana in total")
    void abilityCannotBeActivatedWithOnlyThreeMana() {
        Permanent skulker = addCreatureReady(player1, new DeepfathomSkulker());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, skulker.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability can target an opposing creature and expires at end of turn")
    void abilityTargetsOpponentAndExpires() {
        harness.addToBattlefield(player1, new DeepfathomSkulker());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isTrue();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isFalse();
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
