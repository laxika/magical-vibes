package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WordOfCommand.class, DarkRitual.class, Forest.class, LlanowarElves.class})
class WordOfCommandTest extends BaseCardTest {

    @Test
    void choosesAndCastsAHandCardUnderControl() {
        WordOfCommand wordOfCommand = new WordOfCommand();
        DarkRitual darkRitual = new DarkRitual();
        harness.setHand(player1, List.of(wordOfCommand));
        harness.setHand(player2, List.of(darkRitual));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.WordOfCommandCardChoice.class);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isEqualTo(3);
        assertThat(gd.mindControllerPlayerId).isNull();
        assertThat(gd.mindControlledPlayerId).isNull();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
    }

    @Test
    void onlyAllowsManaFromControlledLandsWhilePlayingTheCard() {
        harness.setHand(player1, List.of(new WordOfCommand()));
        harness.setHand(player2, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new LlanowarElves());

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handleCardChosen(player1, 0);

        assertThatThrownBy(() -> gs.tapPermanent(gd, player1, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only mana abilities of lands you control");

        gs.tapPermanent(gd, player1, 0);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void cannotDeclineToPlayAnAffordableChosenCard() {
        harness.setHand(player1, List.of(new WordOfCommand()));
        harness.setHand(player2, List.of(new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handleCardChosen(player1, 0);

        assertThatThrownBy(() -> harness.handleMayAbilityChosen(player1, false))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void castsCreatureDuringTheOtherPlayersTurn() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(new WordOfCommand()));
        harness.setHand(player2, List.of(elves));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(elves);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(elves.getId()));
    }

    @Test
    void releasesControlWhileTheChosenSpellWaitsOnTheStack() {
        DarkRitual ritual = new DarkRitual();
        harness.setHand(player1, List.of(new WordOfCommand()));
        harness.setHand(player2, List.of(ritual));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(ritual.getId()));
        assertThat(gd.mindControllerPlayerId).isNull();
        assertThat(gd.mindControlledPlayerId).isNull();
        resolveAllTriggers();
    }

    @Test
    void cannotActivateLandManaThatCannotBeSpentOnTheChosenCard() {
        harness.setHand(player1, List.of(new WordOfCommand()));
        harness.setHand(player2, List.of(new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addToBattlefield(player2, new Forest());

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handleCardChosen(player1, 0);

        assertThatThrownBy(() -> gs.tapPermanent(gd, player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void emptyHandFinishesWithoutAChoiceOrPlayerControl() {
        harness.setHand(player1, List.of(new WordOfCommand()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.mindControllerPlayerId).isNull();
        assertThat(gd.mindControlledPlayerId).isNull();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
    }

    @Test
    void canTargetOnlyAnOpponent() {
        harness.setHand(player1, List.of(new WordOfCommand()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
