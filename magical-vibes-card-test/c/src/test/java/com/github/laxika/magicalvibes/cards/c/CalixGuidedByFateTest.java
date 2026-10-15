package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CalixGuidedByFate.class, GloriousAnthem.class, GrizzlyBears.class, HolyStrength.class, Pacifism.class})
class CalixGuidedByFateTest extends BaseCardTest {

    @Test
    @DisplayName("Its own entry puts a +1/+1 counter on the chosen creature")
    void ownEntryPutsCounterOnChosenCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CalixGuidedByFate()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Another enchantment entering puts a +1/+1 counter on the chosen creature")
    void allyEnchantmentEntryPutsCounterOnChosenCreature() {
        addReadyCalix();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.EntersTriggerTarget.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Combat damage offers only nonlegendary enchantments to copy")
    void combatDamageOffersFilteredCopyChoice() {
        Permanent calix = addReadyCalix();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent anthem = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        Permanent pacifism = harness.addToBattlefieldAndReturn(player1, new Pacifism());
        pacifism.setAttachedTo(bears.getId());
        calix.setAttacking(true);

        declareAndResolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactly(anthem.getId(), pacifism.getId());
        assertThat(choice.validPermanentIds()).doesNotContain(calix.getId(), bears.getId());
    }

    @Test
    @DisplayName("Accepting one combat-damage copy consumes the ability for the turn")
    void acceptingCopyConsumesAbilityForTurn() {
        Permanent calix = addReadyCalix();
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        aura.setAttachedTo(enchanted.getId());
        Permanent anthem = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        calix.setAttacking(true);
        enchanted.setAttacking(true);

        declareAndResolveCombat();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.oncePerTurnTriggersFiredThisTurn).contains(calix.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, anthem.getId());
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.PermanentChoice) {
            harness.handlePermanentChosen(player1, calix.getId());
        }
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Glorious Anthem")).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Declining a combat-damage copy does not consume the ability")
    void decliningCopyDoesNotConsumeAbility() {
        Permanent calix = addReadyCalix();
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        aura.setAttachedTo(enchanted.getId());
        calix.setAttacking(true);
        enchanted.setAttacking(true);

        declareAndResolveCombat();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    @DisplayName("Copying an Aura lets its controller choose a creature for the copy to enchant")
    void auraCopyChoosesWhatToEnchant() {
        Permanent calix = addReadyCalix();
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        aura.setAttachedTo(calix.getId());
        calix.setAttacking(true);

        declareAndResolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).contains(calix.getId(), bears.getId());
        harness.handlePermanentChosen(player1, bears.getId());

        assertThat(findPermanents(player1, "Holy Strength"))
                .anySatisfy(copy -> {
                    assertThat(copy.getId()).isNotEqualTo(aura.getId());
                    assertThat(copy.getAttachedTo()).isEqualTo(bears.getId());
                });
        harness.handlePermanentChosen(player1, calix.getId());
        resolveAllTriggers();
        assertThat(calix.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An unenchanted ally dealing combat damage does not trigger copying")
    void unenchantedAllyDoesNotTriggerCopy() {
        addReadyCalix();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GloriousAnthem());
        bears.setAttacking(true);

        declareAndResolveCombat();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Glorious Anthem")).hasSize(1);
    }

    private Permanent addReadyCalix() {
        return addCreatureReady(player1, new CalixGuidedByFate());
    }

    private void declareAndResolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
