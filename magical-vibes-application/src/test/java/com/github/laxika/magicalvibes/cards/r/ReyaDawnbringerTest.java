package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReyaDawnbringer.class, RagingKavu.class, AngelOfMercy.class, HolyDay.class})
class ReyaDawnbringerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Reya Dawnbringer puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new ReyaDawnbringer()));
        harness.addMana(player1, ManaColor.WHITE, 9);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Reya Dawnbringer");
    }

    @Test
    @DisplayName("Cannot cast Reya Dawnbringer without enough mana")
    void cannotCastWithoutMana() {
        harness.setHand(player1, List.of(new ReyaDawnbringer()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Resolving puts Reya Dawnbringer on the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new ReyaDawnbringer()));
        harness.addMana(player1, ManaColor.WHITE, 9);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Reya Dawnbringer");
    }

    @Test
    @DisplayName("Upkeep trigger chooses a target before going on the stack")
    void upkeepTriggerPutsAbilityOnStack() {
        RagingKavu target = new RagingKavu();
        addCreatureReady(player1, new ReyaDawnbringer());
        harness.setGraveyard(player1, List.of(target));

        advanceToUpkeep(player1);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.minCount()).isEqualTo(1);
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.validCardIds()).containsExactly(target.getId());
        assertThat(gd.stack).isEmpty();

        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Reya Dawnbringer");
    }

    @Test
    @DisplayName("Upkeep trigger asks for a target even with summoning sickness")
    void triggerFiresWithSummoningSickness() {
        RagingKavu target = new RagingKavu();
        Permanent reya = new Permanent(new ReyaDawnbringer());
        gd.playerBattlefields.get(player1.getId()).add(reya);
        harness.setGraveyard(player1, List.of(target));

        advanceToUpkeep(player1);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(target.getId());
    }

    @Test
    @DisplayName("No trigger during opponent's upkeep")
    void noTriggerDuringOpponentsUpkeep() {
        addCreatureReady(player1, new ReyaDawnbringer());
        harness.setGraveyard(player1, List.of(new RagingKavu()));

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .noneMatch(text -> text.contains("Reya Dawnbringer"));
    }

    @Test
    @DisplayName("Returns the chosen creature from the graveyard to the battlefield")
    void returnsCreatureFromGraveyardToBattlefield() {
        RagingKavu target = new RagingKavu();
        addCreatureReady(player1, new ReyaDawnbringer());
        harness.setGraveyard(player1, List.of(target));

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Raging Kavu");
        harness.assertNotInGraveyard(player1, "Raging Kavu");
    }

    @Test
    @DisplayName("Player may decline after choosing a graveyard target")
    void playerCanDeclineGraveyardChoice() {
        RagingKavu target = new RagingKavu();
        addCreatureReady(player1, new ReyaDawnbringer());
        harness.setGraveyard(player1, List.of(target));

        advanceToUpkeep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Raging Kavu");
        harness.assertNotOnBattlefield(player1, "Raging Kavu");
    }

    @Test
    @DisplayName("Choosing a specific creature when multiple are in the graveyard")
    void choosesSpecificCreatureFromGraveyard() {
        RagingKavu ragingKavu = new RagingKavu();
        AngelOfMercy angel = new AngelOfMercy();
        addCreatureReady(player1, new ReyaDawnbringer());
        harness.setGraveyard(player1, List.of(ragingKavu, angel));

        advanceToUpkeep(player1);
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(ragingKavu.getId(), angel.getId());
        harness.handleMultipleCardsChosen(player1, List.of(angel.getId()));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Angel of Mercy");
        harness.assertInGraveyard(player1, "Raging Kavu");
        harness.assertNotInGraveyard(player1, "Angel of Mercy");
    }

    @Test
    @DisplayName("Does not put the trigger on the stack with an empty graveyard")
    void noEffectWithEmptyGraveyard() {
        addCreatureReady(player1, new ReyaDawnbringer());

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Does not put the trigger on the stack when no creature card is in the graveyard")
    void noEffectWithOnlyNonCreaturesInGraveyard() {
        addCreatureReady(player1, new ReyaDawnbringer());
        harness.setGraveyard(player1, List.of(new HolyDay()));

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Holy Day");
    }

    @Test
    @DisplayName("Returned creature's ETB ability triggers")
    void returnedCreatureTriggersETB() {
        AngelOfMercy angel = new AngelOfMercy();
        addCreatureReady(player1, new ReyaDawnbringer());
        harness.setGraveyard(player1, List.of(angel));
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(angel.getId()));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Angel of Mercy");

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Cannot choose a non-creature card as the graveyard target")
    void cannotChooseInvalidIndex() {
        HolyDay holyDay = new HolyDay();
        RagingKavu target = new RagingKavu();
        addCreatureReady(player1, new ReyaDawnbringer());
        harness.setGraveyard(player1, List.of(holyDay, target));

        advanceToUpkeep(player1);
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(target.getId());

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(holyDay.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card");
    }

    @Test
    @DisplayName("Opponent cannot choose a graveyard target for the controller")
    void opponentCannotChoose() {
        RagingKavu target = new RagingKavu();
        addCreatureReady(player1, new ReyaDawnbringer());
        harness.setGraveyard(player1, List.of(target));

        advanceToUpkeep(player1);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player2, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not your turn to choose");
    }

    @Test
    @DisplayName("The chosen target fizzles if it leaves the graveyard before resolution")
    void targetFizzlingWhenItLeavesGraveyard() {
        RagingKavu target = new RagingKavu();
        addCreatureReady(player1, new ReyaDawnbringer());
        harness.setGraveyard(player1, List.of(target));

        advanceToUpkeep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(text -> text.contains("Reya Dawnbringer") && text.contains("fizzles"));
        harness.assertNotOnBattlefield(player1, "Raging Kavu");
    }
}
