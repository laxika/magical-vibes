package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.i.Island;
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

@CardUsed({CastleVantress.class, Island.class})
class CastleVantressTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped without an Island")
    void entersTappedWithoutIsland() {
        harness.setHand(player1, List.of(new CastleVantress()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Castle Vantress").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when you control an Island")
    void entersUntappedWithIsland() {
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new CastleVantress()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Castle Vantress").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Mana ability adds blue mana")
    void manaAbilityAddsBlueMana() {
        harness.addToBattlefield(player1, new CastleVantress());

        harness.activateAbility(player1, 0, 0, null, null);

        Permanent land = findPermanent(player1, "Castle Vantress");
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Scry ability enters a scry-two interaction")
    void scryAbilityEntersScryTwoInteraction() {
        harness.addToBattlefield(player1, new CastleVantress());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
    }

    @Test
    void opponentsIslandDoesNotPreventEnteringTapped() {
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(new CastleVantress()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Castle Vantress").isTapped()).isTrue();
    }

    @Test
    void anotherCastleDoesNotCountAsAnIsland() {
        harness.addToBattlefield(player1, new CastleVantress());
        harness.setHand(player1, List.of(new CastleVantress()));

        harness.playLand(player1, 0);

        assertThat(findPermanents(player1, "Castle Vantress").get(1).isTapped()).isTrue();
    }

    @Test
    void scryPaysManaAndTapCostsAndSplitsTopAndBottom() {
        harness.addToBattlefield(player1, new CastleVantress());
        Island first = new Island();
        Island second = new Island();
        Island third = new Island();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(findPermanent(player1, "Castle Vantress").isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third, first);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void scryWithOnlyOneCardKeepsThatCard() {
        harness.addToBattlefield(player1, new CastleVantress());
        Island onlyCard = new Island();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(onlyCard);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void scryRequiresTwoBlueMana() {
        harness.addToBattlefield(player1, new CastleVantress());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(findPermanent(player1, "Castle Vantress").isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
