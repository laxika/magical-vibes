package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GoblinTombRaider;
import com.github.laxika.magicalvibes.cards.m.MineshaftSpider;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BreechesEagerPillager.class, GoblinTombRaider.class, MineshaftSpider.class})
class BreechesEagerPillagerTest extends BaseCardTest {

    private static final String TREASURE = "Create a Treasure token";
    private static final String CANT_BLOCK = "Target creature can't block this turn";
    private static final String EXILE = "Exile the top card of your library. You may play it this turn.";

    @Test
    @DisplayName("Each Pirate attack creates one trigger, and a mode cannot be chosen twice in a turn")
    void eachPirateAttackConsumesOneMode() {
        addCreatureReady(player1, new BreechesEagerPillager());
        addCreatureReady(player1, new GoblinTombRaider());
        Permanent blocker = addCreatureReady(player2, new MineshaftSpider());

        declareAttackers(List.of(0, 1));
        chooseMode(TREASURE);

        PendingInteraction.ColorChoice nextChoice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(nextChoice).isNotNull();
        assertThat(nextChoice.options()).doesNotContain(TREASURE);

        harness.handleListChoice(player1, CANT_BLOCK);
        harness.handlePermanentChosen(player1, blocker.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
        assertThat(blocker.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Exiles the top card and lets its controller play it this turn")
    void exilesTopCardWithPlayPermission() {
        addCreatureReady(player1, new BreechesEagerPillager());
        MineshaftSpider topCard = new MineshaftSpider();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(List.of(0));
        chooseMode(EXILE);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Does not trigger for a non-Pirate creature attacking")
    void ignoresNonPirateAttacks() {
        addCreatureReady(player1, new BreechesEagerPillager());
        addCreatureReady(player1, new MineshaftSpider());

        declareAttackers(List.of(1));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A fourth Pirate attack has no effect after all three modes are chosen")
    void excessPirateTriggersHaveNoEffect() {
        addCreatureReady(player1, new BreechesEagerPillager());
        addCreatureReady(player1, new GoblinTombRaider());
        addCreatureReady(player1, new GoblinTombRaider());
        addCreatureReady(player1, new GoblinTombRaider());
        Permanent blocker = addCreatureReady(player2, new MineshaftSpider());
        MineshaftSpider topCard = new MineshaftSpider();
        MineshaftSpider secondCard = new MineshaftSpider();
        harness.setLibrary(player1, List.of(topCard, secondCard));

        declareAttackers(List.of(0, 1, 2, 3));
        chooseMode(TREASURE);
        harness.handleListChoice(player1, CANT_BLOCK);
        harness.handlePermanentChosen(player1, blocker.getId());
        harness.handleListChoice(player1, EXILE);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
        assertThat(blocker.isCantBlockThisTurn()).isTrue();
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard);
    }

    @Test
    @DisplayName("The exile mode does nothing when the library is empty")
    void exileModeWithEmptyLibrary() {
        addCreatureReady(player1, new BreechesEagerPillager());
        harness.setLibrary(player1, List.of());

        declareAttackers(List.of(0));
        chooseMode(EXILE);
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.exilePlayPermissions).isEmpty();
        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    @Test
    @DisplayName("The blocking restriction can target a creature its controller controls")
    void canTargetOwnCreature() {
        Permanent breeches = addCreatureReady(player1, new BreechesEagerPillager());

        declareAttackers(List.of(0));
        chooseMode(CANT_BLOCK);
        harness.handlePermanentChosen(player1, breeches.getId());
        resolveAllTriggers();

        assertThat(breeches.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("An exiled creature can be cast in the main phase by paying its mana cost")
    void castsExiledCreatureWithNormalManaCost() {
        addCreatureReady(player1, new BreechesEagerPillager());
        GoblinTombRaider topCard = new GoblinTombRaider();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(List.of(0));
        chooseMode(EXILE);
        resolveAllTriggers();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Goblin Tomb Raider");
        assertThat(gd.findExiledCard(topCard.getId())).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    private void chooseMode(String mode) {
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, mode);
    }
}
