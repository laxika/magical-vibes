package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

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
    @Test
    @CardUsed({WearTear.class, FountainOfYouth.class, AngelicChorus.class})
    @DisplayName("A chosen split card is cast as the half chosen with its target, fuse included since it is in hand")
    void castsChosenSplitCardAsTheChosenHalf() {
        WearTear wearTear = new WearTear();
        harness.setHand(player1, List.of(new WordOfCommand()));
        harness.setHand(player2, List.of(wearTear));
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.addToBattlefield(player1, new AngelicChorus());
        UUID fountainId = harness.getPermanentId(player1, "Fountain of Youth");
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, true);

        // The card is played from its owner's hand, so fuse is offered (CR 702.102a).
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Wear — Destroy target artifact", "Tear — Destroy target enchantment",
                        "Fuse — Wear and then Tear");
        harness.handleListChoice(player1, "Wear — Destroy target artifact");
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getId().equals(wearTear.getId()));
        harness.handlePermanentChosen(player1, fountainId);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Wear"));
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Fountain of Youth");
        harness.assertOnBattlefield(player1, "Angelic Chorus");
        assertThat(gd.mindControlledPlayerId).isNull();
    }
}
