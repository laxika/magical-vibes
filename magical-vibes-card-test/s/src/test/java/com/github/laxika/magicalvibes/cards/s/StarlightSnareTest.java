package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StarlightSnare.class, Plains.class, SavannahLions.class})
class StarlightSnareTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Starlight Snare taps the enchanted creature")
    void enteringAuraTapsEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new SavannahLions());

        harness.setHand(player1, List.of(new StarlightSnare()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature does not untap during its controller's untap step")
    void enchantedCreatureDoesNotUntap() {
        Permanent creature = addCreatureReady(player2, new SavannahLions());
        creature.tap();
        attachStarlightSnare(player1, creature);

        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature untaps after Starlight Snare leaves the battlefield")
    void creatureUntapsAfterRemoval() {
        Permanent creature = addCreatureReady(player2, new SavannahLions());
        creature.tap();
        Permanent aura = attachStarlightSnare(player1, creature);
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Starlight Snare cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());

        harness.setHand(player1, List.of(new StarlightSnare()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("The enter trigger taps only after it resolves")
    void tapWaitsForEnterTriggerResolution() {
        Permanent creature = addCreatureReady(player2, new SavannahLions());
        harness.setHand(player1, List.of(new StarlightSnare()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Starlight Snare");
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The Aura also taps and locks its controller's own creature")
    void canEnchantOwnCreature() {
        Permanent creature = addCreatureReady(player1, new SavannahLions());
        harness.setHand(player1, List.of(new StarlightSnare()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        resolveAllTriggers();
        assertThat(creature.isTapped()).isTrue();

        harness.performUntapStep(player1);
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Only the enchanted creature is prevented from untapping")
    void otherCreaturesUntapNormally() {
        Permanent enchanted = addCreatureReady(player2, new SavannahLions());
        Permanent other = addCreatureReady(player2, new SavannahLions());
        enchanted.tap();
        other.tap();
        attachStarlightSnare(player1, enchanted);

        harness.performUntapStep(player2);

        assertThat(enchanted.isTapped()).isTrue();
        assertThat(other.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An already untapped enchanted creature is not tapped during the untap step")
    void untapRestrictionDoesNotTapCreature() {
        Permanent creature = addCreatureReady(player2, new SavannahLions());
        attachStarlightSnare(player1, creature);

        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isFalse();
    }

    private Permanent attachStarlightSnare(Player controller, Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new StarlightSnare());
        aura.setAttachedTo(creature.getId());
        return aura;
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(currentActivePlayer == player1 ? player2 : player1, TurnStep.UPKEEP);
    }
}
