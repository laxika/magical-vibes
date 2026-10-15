package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.s.SummonShiva;
import com.github.laxika.magicalvibes.cards.b.BusterSword;
import com.github.laxika.magicalvibes.cards.g.Gaelicat;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IshgardTheHolySee.class, FaithAndGrief.class, BusterSword.class,
        SummonShiva.class, Gaelicat.class})
class IshgardTheHolySeeTest extends BaseCardTest {

    @Test
    @DisplayName("Ishgard enters tapped and produces white mana")
    void entersTappedAndProducesWhiteMana() {
        harness.setHand(player1, List.of(new IshgardTheHolySee()));

        harness.playLand(player1, 0);
        Permanent ishgard = findPermanent(player1, "Ishgard, the Holy See");
        assertThat(ishgard.isTapped()).isTrue();

        ishgard.untap();
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Adventure exiles Ishgard and permits its land face to be played")
    void adventureExilesAndPermitsLandPlay() {
        IshgardTheHolySee ishgard = new IshgardTheHolySee();
        harness.setHand(player1, List.of(ishgard));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCardWithAdventure(gd, player1, 0, 0, null, null, List.of());
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(FaithAndGrief.class);

        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(ishgard.getId()));
        assertThat(gd.exilePlayPermissions.get(ishgard.getId())).isEqualTo(player1.getId());

        harness.castFromExile(player1, ishgard.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(ishgard.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(ishgard.getId()));
        assertThat(findPermanent(player1, "Ishgard, the Holy See").isTapped()).isTrue();
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Adventure returns up to two targeted artifact or enchantment cards")
    void adventureReturnsTargetedCards() {
        IshgardTheHolySee ishgard = new IshgardTheHolySee();
        BusterSword artifact = new BusterSword();
        SummonShiva enchantment = new SummonShiva();
        Gaelicat creature = new Gaelicat();
        harness.setHand(player1, List.of(ishgard));
        harness.setGraveyard(player1, List.of(artifact, enchantment, creature));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCardWithAdventure(gd, player1, 0, 0, null, null, List.of());

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).containsExactly(artifact, enchantment);

        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId(), enchantment.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .contains(artifact, enchantment)
                .doesNotContain(creature);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(ishgard.getId()));
    }

    @Test
    void adventureCastingHelperAllowsChoosingGraveyardTargets() {
        IshgardTheHolySee ishgard = new IshgardTheHolySee();
        BusterSword artifact = new BusterSword();
        harness.setHand(player1, List.of(ishgard));
        harness.setGraveyard(player1, List.of(artifact));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castAdventure(player1, 0, List.of());

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).containsExactly(artifact);
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(artifact);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(ishgard);
    }

    @Test
    void canChooseZeroTargetsEvenWhenEligibleCardsExist() {
        IshgardTheHolySee ishgard = new IshgardTheHolySee();
        BusterSword artifact = new BusterSword();
        harness.setHand(player1, List.of(ishgard));
        harness.setGraveyard(player1, List.of(artifact));
        harness.addMana(player1, ManaColor.WHITE, 5);

        gs.playCardWithAdventure(gd, player1, 0, 0, null, null, List.of());
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(artifact);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(ishgard);
        assertThat(gd.exilePlayPermissions.get(ishgard.getId())).isEqualTo(player1.getId());
    }

    @Test
    void canReturnOnlyOneCardAndCannotTargetOpponentsGraveyard() {
        IshgardTheHolySee ishgard = new IshgardTheHolySee();
        SummonShiva enchantment = new SummonShiva();
        BusterSword opponentArtifact = new BusterSword();
        harness.setHand(player1, List.of(ishgard));
        harness.setGraveyard(player1, List.of(enchantment));
        harness.setGraveyard(player2, List.of(opponentArtifact));
        harness.addMana(player1, ManaColor.WHITE, 5);

        gs.playCardWithAdventure(gd, player1, 0, 0, null, null, List.of());
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.cards()).containsExactly(enchantment);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(opponentArtifact.getId()))).isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(enchantment.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(enchantment);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentArtifact);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(ishgard);
    }

    @Test
    void cannotChooseMoreThanTwoTargetsOrChooseTheSameCardTwice() {
        BusterSword first = new BusterSword();
        BusterSword second = new BusterSword();
        SummonShiva third = new SummonShiva();
        harness.setHand(player1, List.of(new IshgardTheHolySee()));
        harness.setGraveyard(player1, List.of(first, second, third));
        harness.addMana(player1, ManaColor.WHITE, 5);

        gs.playCardWithAdventure(gd, player1, 0, 0, null, null, List.of());
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(first.getId(), first.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(first, second).doesNotContain(third);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(third);
    }

    @Test
    void stillResolvesWhenOnlyOneOfTwoTargetsLeavesTheGraveyard() {
        IshgardTheHolySee ishgard = new IshgardTheHolySee();
        BusterSword artifact = new BusterSword();
        SummonShiva enchantment = new SummonShiva();
        harness.setHand(player1, List.of(ishgard));
        harness.setGraveyard(player1, List.of(artifact, enchantment));
        harness.addMana(player1, ManaColor.WHITE, 5);

        gs.playCardWithAdventure(gd, player1, 0, 0, null, null, List.of());
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId(), enchantment.getId()));
        harness.setGraveyard(player1, List.of(enchantment));
        harness.setExile(player1, List.of(artifact));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(enchantment).doesNotContain(artifact);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(artifact, ishgard);
        assertThat(gd.exilePlayPermissions.get(ishgard.getId())).isEqualTo(player1.getId());
    }

    @Test
    void goesToGraveyardWithoutAdventurePermissionWhenAllTargetsAreIllegal() {
        IshgardTheHolySee ishgard = new IshgardTheHolySee();
        BusterSword artifact = new BusterSword();
        harness.setHand(player1, List.of(ishgard));
        harness.setGraveyard(player1, List.of(artifact));
        harness.addMana(player1, ManaColor.WHITE, 5);

        gs.playCardWithAdventure(gd, player1, 0, 0, null, null, List.of());
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(artifact));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ishgard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(artifact).doesNotContain(ishgard);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(ishgard.getId());
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(artifact);
    }
}
