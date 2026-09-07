package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LostInThought.class, BottleGnomes.class, GrizzlyBears.class})
class LostInThoughtTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature cannot attack or activate abilities")
    void enchantedCreatureIsLocked() {
        Permanent creature = new Permanent(new BottleGnomes());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(creature);

        Permanent aura = new Permanent(new LostInThought());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player2.getId()).add(aura);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("The enchanted creature's controller may exile three graveyard cards to ignore the Aura")
    void exilingThreeCardsIgnoresTheAuraUntilEndOfTurn() {
        Permanent creature = new Permanent(new BottleGnomes());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(creature);
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent aura = new Permanent(new LostInThought());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player2.getId()).add(aura);

        harness.activateAbility(player1, 2, 0, null, null);
        harness.passBothPriorities();

        assertThat(aura.isAuraEffectsIgnoredThisTurn()).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Bottle Gnomes");
    }

    @Test
    @DisplayName("The Aura's controller cannot activate the bypass ability")
    void auraControllerCannotActivateBypass() {
        Permanent creature = new Permanent(new BottleGnomes());
        gd.playerBattlefields.get(player1.getId()).add(creature);

        Permanent aura = new Permanent(new LostInThought());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player2.getId()).add(aura);
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("enchanted permanent's controller");
    }

    @Test
    @DisplayName("The bypass ability wears off at end of turn")
    void bypassWearsOffAtEndOfTurn() {
        Permanent creature = new Permanent(new BottleGnomes());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(creature);
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent aura = new Permanent(new LostInThought());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player2.getId()).add(aura);

        harness.activateAbility(player1, 2, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(aura.isAuraEffectsIgnoredThisTurn()).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }
}
