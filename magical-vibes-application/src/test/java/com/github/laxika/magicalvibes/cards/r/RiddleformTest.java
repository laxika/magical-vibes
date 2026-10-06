package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JacesIngenuity;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Riddleform.class, GrizzlyBears.class, JacesIngenuity.class})
class RiddleformTest extends BaseCardTest {

    @BeforeEach
    void setUp() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private Permanent addRiddleform(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Riddleform());
    }

    @Test
    @DisplayName("Casting a noncreature spell and accepting makes Riddleform a 3/3 flying Sphinx")
    void noncreatureSpellAnimatesWhenAccepted() {
        Permanent riddleform = addRiddleform(player1);
        harness.setHand(player1, List.of(new JacesIngenuity()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, riddleform)).isTrue();
        assertThat(gqs.getEffectivePower(gd, riddleform)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, riddleform)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, riddleform, Keyword.FLYING)).isTrue();
        assertThat(riddleform.getTransientSubtypes()).contains(CardSubtype.SPHINX);
    }

    @Test
    @DisplayName("Declining the trigger leaves Riddleform a non-creature enchantment")
    void decliningLeavesNonCreature() {
        Permanent riddleform = addRiddleform(player1);
        harness.setHand(player1, List.of(new JacesIngenuity()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castInstant(player1, 0);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, riddleform)).isFalse();
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger the animation")
    void creatureSpellDoesNotTrigger() {
        addRiddleform(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Animation wears off at end of turn")
    void animationWearsOffAtEndOfTurn() {
        Permanent riddleform = addRiddleform(player1);
        harness.setHand(player1, List.of(new JacesIngenuity()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castInstant(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, riddleform)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, riddleform)).isFalse();
        assertThat(riddleform.getTransientSubtypes()).doesNotContain(CardSubtype.SPHINX);
    }

    @Test
    @DisplayName("{2}{U}: Scry 1 enters a scry with one card")
    void scryAbilityEntersScryWithOneCard() {
        addRiddleform(player1);
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    @DisplayName("Scry keeping the card on top preserves it and clears the interaction")
    void scryKeepOnTopResolves() {
        addRiddleform(player1);
        harness.addMana(player1, ManaColor.BLUE, 3);
        var top = gd.playerDecks.get(player1.getId()).get(0);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId()).get(0)).isSameAs(top);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    @DisplayName("An opponent's noncreature spell does not animate Riddleform")
    void opponentSpellDoesNotTrigger() {
        Permanent riddleform = addRiddleform(player1);
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Riddleform()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castEnchantment(player2, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, riddleform)).isFalse();
    }

    @Test
    @DisplayName("Declining a later trigger preserves an earlier animation")
    void decliningLaterTriggerPreservesAnimation() {
        Permanent riddleform = addRiddleform(player1);
        harness.setHand(player1, List.of(new JacesIngenuity(), new JacesIngenuity()));
        harness.addMana(player1, ManaColor.BLUE, 10);

        harness.castInstant(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castInstant(player1, 0);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, riddleform)).isTrue();
        assertThat(gqs.isEnchantment(gd, riddleform)).isTrue();
        assertThat(gqs.getEffectivePower(gd, riddleform)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, riddleform)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, riddleform, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Scry can put the top card on the bottom without animating Riddleform")
    void scryPutOnBottomResolves() {
        Permanent riddleform = addRiddleform(player1);
        Riddleform top = new Riddleform();
        Riddleform next = new Riddleform();
        harness.setLibrary(player1, List.of(top, next));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next, top);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gqs.isCreature(gd, riddleform)).isFalse();
    }

    @Test
    @DisplayName("Scry with an empty library resolves without a choice or animation")
    void scryWithEmptyLibraryResolves() {
        Permanent riddleform = addRiddleform(player1);
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.isCreature(gd, riddleform)).isFalse();
    }
}
