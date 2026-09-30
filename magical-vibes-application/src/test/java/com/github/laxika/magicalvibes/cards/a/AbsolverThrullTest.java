package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.h.HissingMiasma;
import com.github.laxika.magicalvibes.cards.m.Mortify;
import com.github.laxika.magicalvibes.cards.m.MourningThrull;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AbsolverThrull.class, HissingMiasma.class, MourningThrull.class, Mortify.class})
class AbsolverThrullTest extends BaseCardTest {

    @Test
    void entersAndDestroysTargetEnchantment() {
        harness.addToBattlefield(player2, new HissingMiasma());
        UUID enchantmentId = harness.getPermanentId(player2, "Hissing Miasma");
        castAbsolver(enchantmentId);

        harness.assertNotOnBattlefield(player2, "Hissing Miasma");
        harness.assertInGraveyard(player2, "Hissing Miasma");
    }

    @Test
    void deathExilesItHauntingTargetCreature() {
        harness.addToBattlefield(player2, new HissingMiasma());
        harness.addToBattlefield(player2, new MourningThrull());
        UUID enchantmentId = harness.getPermanentId(player2, "Hissing Miasma");
        UUID creatureId = harness.getPermanentId(player2, "Mourning Thrull");
        castAbsolver(enchantmentId);

        UUID absolverId = harness.getPermanentId(player1, "Absolver Thrull");
        destroyWithMortify(absolverId);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, creatureId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Absolver Thrull");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getName)
                .contains("Absolver Thrull");
    }

    @Test
    void enterTriggerCannotTargetCreature() {
        harness.addToBattlefield(player2, new MourningThrull());
        UUID creatureId = harness.getPermanentId(player2, "Mourning Thrull");
        harness.setHand(player1, List.of(new AbsolverThrull()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an enchantment");
    }

    @Test
    void hauntedCreatureDeathDestroysTargetEnchantment() {
        harness.addToBattlefield(player2, new HissingMiasma());
        harness.addToBattlefield(player2, new HissingMiasma());
        harness.addToBattlefield(player2, new MourningThrull());
        UUID firstEnchantmentId = findPermanents(player2, "Hissing Miasma").getFirst().getId();
        UUID secondEnchantmentId = findPermanents(player2, "Hissing Miasma").get(1).getId();
        UUID creatureId = harness.getPermanentId(player2, "Mourning Thrull");
        castAbsolver(firstEnchantmentId);

        UUID absolverId = harness.getPermanentId(player1, "Absolver Thrull");
        destroyWithMortify(absolverId);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, creatureId);
        harness.passBothPriorities();

        destroyWithMortify(creatureId);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, secondEnchantmentId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Mourning Thrull");
        harness.assertNotOnBattlefield(player2, "Hissing Miasma");
        harness.assertInGraveyard(player2, "Hissing Miasma");
    }

    private void castAbsolver(UUID targetId) {
        setupPlayer1Active();
        harness.setHand(player1, List.of(new AbsolverThrull()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void destroyWithMortify(UUID targetId) {
        setupPlayer2Active();
        harness.setHand(player2, List.of(new Mortify()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, targetId);
    }

    private void setupPlayer1Active() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
