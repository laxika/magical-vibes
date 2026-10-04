package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GlassblowersPuzzleknot.class, Forest.class})
class GlassblowersPuzzleknotTest extends BaseCardTest {

    @Test
    void enteringBattlefieldScriesThenGivesTwoEnergy() {
        Card first = new Forest();
        Card second = new Forest();
        Card rest = new Forest();
        harness.setLibrary(player1, List.of(first, second, rest));
        harness.castFromHand(player1, new GlassblowersPuzzleknot(), "{2}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(first, second);
        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    void sacrificedAbilityScriesThenGivesTwoEnergy() {
        Card first = new Forest();
        Card second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        Permanent puzzleknot = harness.addToBattlefieldAndReturn(player1, new GlassblowersPuzzleknot());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        int puzzleknotIndex = gd.playerBattlefields.get(player1.getId()).indexOf(puzzleknot);
        harness.activateAbility(player1, puzzleknotIndex, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(puzzleknot);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(puzzleknot.getCard());
    }

    @Test
    void enteringWithEmptyLibraryStillGivesEnergy() {
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new GlassblowersPuzzleknot(), "{2}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.playerEnergyCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    void activatedAbilityPaysSacrificeBeforeResolvingWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        Permanent puzzleknot = harness.addToBattlefieldAndReturn(player1, new GlassblowersPuzzleknot());
        puzzleknot.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(puzzleknot);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(puzzleknot.getCard());
        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    void scryWithOneCardDoesNotPreventEnergyGain() {
        Card onlyCard = new Forest();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.castFromHand(player1, new GlassblowersPuzzleknot(), "{2}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(onlyCard);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }
}
