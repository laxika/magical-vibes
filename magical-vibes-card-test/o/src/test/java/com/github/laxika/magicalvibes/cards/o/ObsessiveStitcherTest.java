package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ObsessiveStitcher.class, Forest.class, GrizzlyBears.class})
class ObsessiveStitcherTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card, then discards a card")
    void drawsThenDiscards() {
        harness.addToBattlefield(player1, new ObsessiveStitcher());
        findPermanent(player1, "Obsessive Stitcher").setSummoningSick(false);
        Card discarded = new Forest();
        Card drawn = new GrizzlyBears();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("Sacrifices itself and returns a creature card from the graveyard")
    void sacrificesAndReturnsCreature() {
        harness.addToBattlefield(player1, new ObsessiveStitcher());
        findPermanent(player1, "Obsessive Stitcher").setSummoningSick(false);
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
        harness.assertInGraveyard(player1, "Obsessive Stitcher");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Cannot target a non-creature card in the graveyard")
    void cannotTargetNonCreatureCard() {
        harness.addToBattlefield(player1, new ObsessiveStitcher());
        findPermanent(player1, "Obsessive Stitcher").setSummoningSick(false);
        Card nonCreature = new Forest();
        harness.setGraveyard(player1, List.of(nonCreature));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 1, null, nonCreature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canDiscardTheCardJustDrawn() {
        harness.addToBattlefield(player1, new ObsessiveStitcher());
        findPermanent(player1, "Obsessive Stitcher").setSummoningSick(false);
        Card kept = new Forest();
        Card drawn = new ObsessiveStitcher();
        harness.setHand(player1, List.of(kept));
        harness.setLibrary(player1, List.of(drawn));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept, drawn);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(drawn);
        assertThat(findPermanent(player1, "Obsessive Stitcher").isTapped()).isTrue();
    }

    @Test
    void cannotTargetOpponentsGraveyard() {
        harness.addToBattlefield(player1, new ObsessiveStitcher());
        findPermanent(player1, "Obsessive Stitcher").setSummoningSick(false);
        Card creature = new ObsessiveStitcher();
        harness.setGraveyard(player2, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 1, null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Obsessive Stitcher");
    }

    @Test
    void sacrificeIsPaidBeforeResolutionAndRemainsPaidWhenTargetLeaves() {
        harness.addToBattlefield(player1, new ObsessiveStitcher());
        findPermanent(player1, "Obsessive Stitcher").setSummoningSick(false);
        Card creature = new ObsessiveStitcher();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, creature.getId(), Zone.GRAVEYARD);

        harness.assertNotOnBattlefield(player1, "Obsessive Stitcher");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        gd.playerGraveyards.get(player1.getId()).remove(creature);
        harness.setHand(player1, List.of(creature));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Obsessive Stitcher");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    void cannotActivateTapAbilitiesWhileSummoningSick() {
        harness.addToBattlefield(player1, new ObsessiveStitcher());
        findPermanent(player1, "Obsessive Stitcher").setSummoningSick(true);
        Card creature = new ObsessiveStitcher();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 1, null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Obsessive Stitcher");
    }
}
