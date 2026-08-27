package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AbundantGrowth;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IshgardTheHolySee.class, FaithAndGrief.class, FountainOfYouth.class,
        AbundantGrowth.class, GrizzlyBears.class})
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
    }

    @Test
    @DisplayName("Adventure returns up to two targeted artifact or enchantment cards")
    void adventureReturnsTargetedCards() {
        IshgardTheHolySee ishgard = new IshgardTheHolySee();
        FountainOfYouth artifact = new FountainOfYouth();
        AbundantGrowth enchantment = new AbundantGrowth();
        GrizzlyBears creature = new GrizzlyBears();
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
}
