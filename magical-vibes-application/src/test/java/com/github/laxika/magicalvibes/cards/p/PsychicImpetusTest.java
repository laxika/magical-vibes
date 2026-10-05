package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PsychicImpetus.class, GrizzlyBears.class, Mountain.class})
class PsychicImpetusTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +2/+2 and is goaded")
    void enchantedCreatureGetsBoostAndIsGoaded() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        castPsychicImpetus(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(als.getMustAttackRequirementCount(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking enchanted creature triggers scry 2")
    void attackingEnchantedCreatureTriggersScryTwo() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Mountain()));
        castPsychicImpetus(creature);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(2);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
    }

    @Test
    @DisplayName("Aura controller scries when an opponent's enchanted creature attacks")
    void opponentCreatureAttackLetsAuraControllerScry() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Mountain()));
        castPsychicImpetus(creature);

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(2);
        assertThat(scry.decidingPlayerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Psychic Impetus cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new PsychicImpetus()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Goaded creature must attack when able")
    void cannotOmitAbleEnchantedAttacker() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        castPsychicImpetus(creature);

        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("An unrelated attacker does not trigger scry while the enchanted creature is tapped")
    void unrelatedAttackerDoesNotTriggerScry() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Mountain()));
        castPsychicImpetus(creature);
        creature.setTapped(true);

        declareAttackers(player2, List.of(1));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Scry 2 with a one-card library allows putting that card on the bottom")
    void scryWithOneCardLibrary() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        GrizzlyBears card = new GrizzlyBears();
        harness.setLibrary(player1, List.of(card));
        castPsychicImpetus(creature);

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.decidingPlayerId()).isEqualTo(player1.getId());
        assertThat(scry.cards()).containsExactly(card);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(card);
    }

    private void castPsychicImpetus(Permanent creature) {
        harness.setHand(player1, List.of(new PsychicImpetus()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
    }
}
