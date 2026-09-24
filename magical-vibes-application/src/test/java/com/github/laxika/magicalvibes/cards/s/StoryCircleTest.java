package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StoryCircle.class, HillGiant.class, GrizzlyBears.class, Shock.class})
class StoryCircleTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Story Circle puts it on the stack as enchantment spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new StoryCircle()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
    }

    @Test
    @DisplayName("Resolving Story Circle enters the battlefield and awaits a color choice")
    void resolvingTriggersColorChoice() {
        harness.setHand(player1, List.of(new StoryCircle()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Story Circle");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Choosing a color sets chosenColor on the permanent")
    void choosingColorSetsOnPermanent() {
        harness.setHand(player1, List.of(new StoryCircle()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        assertThat(findPermanent(player1, "Story Circle").getChosenColor()).isEqualTo(CardColor.RED);
    }

    @Test
    @DisplayName("Color choice clears awaiting state")
    void colorChoiceClearsAwaitingState() {
        harness.setHand(player1, List.of(new StoryCircle()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
    }

    @Test
    @DisplayName("Color choice is logged")
    void colorChoiceIsLogged() {
        harness.setHand(player1, List.of(new StoryCircle()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("chooses black"));
    }

    @Test
    @DisplayName("Activating the prevention ability with {W} puts it on the stack")
    void activatingAbilityPutsOnStack() {
        Permanent storyCircle = addReadyStoryCircle(player1, CardColor.RED);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, battlefieldIndex(player1, storyCircle), null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Resolving the prevention ability asks for a source of the chosen color")
    void resolvingAbilityAsksForChosenColorSource() {
        Permanent storyCircle = addReadyStoryCircle(player1, CardColor.RED);
        Permanent redSource = addCreatureReady(player2, new HillGiant());
        Permanent greenSource = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        activateStoryCircle(player1, storyCircle);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).contains(redSource.getId()).doesNotContain(greenSource.getId());

        harness.handlePermanentChosen(player1, redSource.getId());

        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(shield -> shield.playerId().equals(player1.getId())
                        && shield.sourceId().equals(redSource.getId()));
    }

    @Test
    @DisplayName("Prevents the next combat damage from the chosen source and consumes the shield")
    void preventsCombatDamageFromChosenSource() {
        harness.setLife(player2, 20);
        Permanent storyCircle = addReadyStoryCircle(player2, CardColor.RED);
        Permanent attacker = addCreatureReady(player1, new HillGiant());
        harness.addMana(player2, ManaColor.WHITE, 1);

        activateAndChooseSource(player2, storyCircle, attacker);
        attacker.setAttacking(true);

        resolveCombat(player1);

        harness.assertLife(player2, 20);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("Damage from a source of an unchosen color is dealt and does not consume the shield")
    void doesNotPreventDamageFromUnchosenColorSource() {
        harness.setLife(player2, 20);
        Permanent storyCircle = addReadyStoryCircle(player2, CardColor.RED);
        Permanent chosenSource = addCreatureReady(player1, new HillGiant());
        Permanent greenAttacker = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player2, ManaColor.WHITE, 1);

        activateAndChooseSource(player2, storyCircle, chosenSource);
        greenAttacker.setAttacking(true);

        resolveCombat(player1);

        harness.assertLife(player2, 18);
        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(shield -> shield.playerId().equals(player2.getId())
                        && shield.sourceId().equals(chosenSource.getId()));
    }

    @Test
    @DisplayName("Damage from a different source of the chosen color is dealt")
    void doesNotPreventDamageFromDifferentChosenColorSource() {
        harness.setLife(player2, 20);
        Permanent storyCircle = addReadyStoryCircle(player2, CardColor.RED);
        Permanent chosenSource = addCreatureReady(player1, new HillGiant());
        Permanent otherRedSource = addCreatureReady(player1, new HillGiant());
        harness.addMana(player2, ManaColor.WHITE, 1);

        activateAndChooseSource(player2, storyCircle, chosenSource);
        otherRedSource.setAttacking(true);

        resolveCombat(player1);

        harness.assertLife(player2, 17);
        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(shield -> shield.playerId().equals(player2.getId())
                        && shield.sourceId().equals(chosenSource.getId()));
    }

    @Test
    @DisplayName("Multiple activations create separate source shields")
    void multipleActivationsPreventMultipleInstances() {
        harness.setLife(player2, 20);
        Permanent storyCircle = addReadyStoryCircle(player2, CardColor.RED);
        Permanent attacker1 = addCreatureReady(player1, new HillGiant());
        Permanent attacker2 = addCreatureReady(player1, new HillGiant());
        harness.addMana(player2, ManaColor.WHITE, 2);

        activateAndChooseSource(player2, storyCircle, attacker1);
        activateAndChooseSource(player2, storyCircle, attacker2);

        assertThat(gd.playerSourceNextDamageShields).hasSize(2);
        attacker1.setAttacking(true);
        attacker2.setAttacking(true);

        resolveCombat(player1);

        harness.assertLife(player2, 20);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("Source shields reset at the end of the turn")
    void preventionResetsAtEndOfTurn() {
        Permanent storyCircle = addReadyStoryCircle(player1, CardColor.RED);
        Permanent source = addCreatureReady(player2, new HillGiant());
        harness.addMana(player1, ManaColor.WHITE, 1);

        activateAndChooseSource(player1, storyCircle, source);

        assertThat(gd.playerSourceNextDamageShields).isNotEmpty();
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("The ability keeps its chosen color if Story Circle leaves before resolution")
    void abilityResolvesAfterSourceLeavesBattlefield() {
        Permanent storyCircle = addReadyStoryCircle(player1, CardColor.RED);
        Permanent source = addCreatureReady(player2, new HillGiant());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, battlefieldIndex(player1, storyCircle), null, null);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, storyCircle));
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).contains(source.getId());
        harness.handlePermanentChosen(player1, source.getId());

        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(shield -> shield.playerId().equals(player1.getId())
                        && shield.sourceId().equals(source.getId()));
    }

    @Test
    @DisplayName("A chosen red spell on the stack has its next damage to you prevented")
    void preventsDamageFromChosenSpellOnStack() {
        harness.setLife(player1, 20);
        Permanent storyCircle = addReadyStoryCircle(player1, CardColor.RED);
        Shock shock = new Shock();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());

        harness.addMana(player1, ManaColor.WHITE, 1);
        activateStoryCircle(player1, storyCircle);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).contains(shock.getId());
        harness.handlePermanentChosen(player1, shock.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("No source choice is offered when no permanent matches the chosen color")
    void noMatchingColorSource() {
        addReadyStoryCircle(player1, CardColor.RED);
        addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        activateStoryCircle(player1, findPermanent(player1, "Story Circle"));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("No permanents on the battlefield"));

        Permanent redSourceEnteringLater = addCreatureReady(player2, new HillGiant());
        redSourceEnteringLater.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 17);
    }

    private Permanent addReadyStoryCircle(Player player, CardColor chosenColor) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new StoryCircle());
        perm.setChosenColor(chosenColor);
        return perm;
    }

    private void activateStoryCircle(Player player, Permanent storyCircle) {
        harness.activateAbility(player, battlefieldIndex(player, storyCircle), null, null);
        harness.passBothPriorities();
    }

    private void activateAndChooseSource(Player player, Permanent storyCircle, Permanent source) {
        activateStoryCircle(player, storyCircle);
        harness.handlePermanentChosen(player, source.getId());
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

}
