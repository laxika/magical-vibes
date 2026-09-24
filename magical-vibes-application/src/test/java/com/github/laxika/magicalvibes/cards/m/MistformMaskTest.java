package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MistformMask.class, MistformMutant.class, Swamp.class})
class MistformMaskTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the Aura ability prompts for a creature type")
    void activatingPromptsForCreatureType() {
        addAttachedMask();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).contains(CardSubtype.WALL.name());
    }

    @Test
    @DisplayName("The enchanted creature becomes the chosen type until end of turn")
    void enchantedCreatureBecomesChosenType() {
        Permanent creature = addAttachedMask();
        Permanent otherCreature = addCreatureReady(player1, new MistformMutant());

        activateMask(CardSubtype.GOBLIN);

        assertThat(gqs.effectiveCreatureSubtypes(gd, creature)).containsExactly(CardSubtype.GOBLIN);
        assertThat(gqs.effectiveCreatureSubtypes(gd, otherCreature))
                .containsExactlyInAnyOrder(CardSubtype.ILLUSION, CardSubtype.MUTANT);
    }

    @Test
    @DisplayName("Wall is a legal creature type choice")
    void wallCanBeChosen() {
        Permanent creature = addAttachedMask();

        activateMask(CardSubtype.WALL);

        assertThat(gqs.effectiveCreatureSubtypes(gd, creature)).containsExactly(CardSubtype.WALL);
    }

    @Test
    @DisplayName("The chosen creature type wears off at end of turn")
    void chosenCreatureTypeWearsOff() {
        Permanent creature = addAttachedMask();

        activateMask(CardSubtype.GOBLIN);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.effectiveCreatureSubtypes(gd, creature))
                .containsExactlyInAnyOrder(CardSubtype.ILLUSION, CardSubtype.MUTANT);
    }

    @Test
    @DisplayName("Mistform Mask can enchant an opponent's creature")
    void canEnchantOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new MistformMutant());
        harness.setHand(player1, List.of(new MistformMask()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent mask = findPermanent(player1, "Mistform Mask");
        activateMask(mask, CardSubtype.GOBLIN);

        assertThat(gqs.effectiveCreatureSubtypes(gd, creature)).containsExactly(CardSubtype.GOBLIN);
        assertThat(mask.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Mistform Mask cannot enchant a noncreature permanent")
    void cannotEnchantNonCreaturePermanent() {
        Permanent swamp = harness.addToBattlefieldAndReturn(player2, new Swamp());
        harness.setHand(player1, List.of(new MistformMask()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, swamp.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addAttachedMask() {
        Permanent creature = addCreatureReady(player1, new MistformMutant());
        Permanent mask = harness.addToBattlefieldAndReturn(player1, new MistformMask());
        mask.setAttachedTo(creature.getId());
        harness.forceActivePlayer(player1);
        return creature;
    }

    private void activateMask(CardSubtype subtype) {
        activateMask(findPermanent(player1, "Mistform Mask"), subtype);
    }

    private void activateMask(Permanent mask, CardSubtype subtype) {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(mask), null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, subtype.name());
    }
}
