package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DaruLancer;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SkirkProspector;
import com.github.laxika.magicalvibes.cards.s.SnarlingUndorak;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

@CardUsed({HarshMercy.class, DaruLancer.class, Plains.class, SkirkProspector.class, SnarlingUndorak.class})
class HarshMercyTest extends BaseCardTest {
    @Test
    void chosenTypesAreUnionedAcrossPlayers() {
        harness.addToBattlefield(player1, new DaruLancer());
        harness.addToBattlefield(player1, new SkirkProspector());
        harness.addToBattlefield(player2, new SnarlingUndorak());
        harness.addToBattlefield(player2, new SkirkProspector());
        cast();

        org.assertj.core.api.Assertions.assertThat(
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleListChoice(player1, "HUMAN");
        org.assertj.core.api.Assertions.assertThat(
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleListChoice(player2, "BEAST");

        harness.assertOnBattlefield(player1, "Daru Lancer");
        harness.assertOnBattlefield(player2, "Snarling Undorak");
        harness.assertNotOnBattlefield(player1, "Skirk Prospector");
        harness.assertNotOnBattlefield(player2, "Skirk Prospector");
    }

    @Test
    void creaturesCannotBeRegenerated() {
        harness.addToBattlefield(player1, new DaruLancer());
        var goblin = harness.addToBattlefieldAndReturn(player1, new SkirkProspector());
        goblin.setRegenerationShield(1);
        cast();

        harness.handleListChoice(player1, "HUMAN");
        harness.handleListChoice(player2, "HUMAN");

        harness.assertNotOnBattlefield(player1, "Skirk Prospector");
        harness.assertInGraveyard(player1, "Skirk Prospector");
    }

    @Test
    void onlyCreaturesAreDestroyed() {
        harness.addToBattlefield(player1, new DaruLancer());
        harness.addToBattlefield(player1, new Plains());
        cast();

        harness.handleListChoice(player1, "GOBLIN");
        harness.handleListChoice(player2, "GOBLIN");

        harness.assertNotOnBattlefield(player1, "Daru Lancer");
        harness.assertOnBattlefield(player1, "Plains");
    }

    @Test
    void opponentsChoiceProtectsCreaturesRegardlessOfController() {
        harness.addToBattlefield(player1, new SkirkProspector());
        harness.addToBattlefield(player2, new DaruLancer());
        harness.addToBattlefield(player2, new SnarlingUndorak());
        cast();

        harness.handleListChoice(player1, "SOLDIER");
        harness.handleListChoice(player2, "GOBLIN");

        harness.assertOnBattlefield(player1, "Skirk Prospector");
        harness.assertOnBattlefield(player2, "Daru Lancer");
        harness.assertInGraveyard(player2, "Snarling Undorak");
        org.assertj.core.api.Assertions.assertThat(gd.stack).isEmpty();
    }

    @Test
    void destructionWaitsUntilBothPlayersHaveChosen() {
        harness.addToBattlefield(player1, new DaruLancer());
        harness.addToBattlefield(player2, new SkirkProspector());
        cast();

        harness.handleListChoice(player1, "BEAST");

        harness.assertOnBattlefield(player1, "Daru Lancer");
        harness.assertOnBattlefield(player2, "Skirk Prospector");

        harness.handleListChoice(player2, "BEAST");

        harness.assertInGraveyard(player1, "Daru Lancer");
        harness.assertInGraveyard(player2, "Skirk Prospector");
        org.assertj.core.api.Assertions.assertThat(gd.stack).isEmpty();
    }

    @Test
    void playersChooseEvenWhenNeitherControlsCreatures() {
        cast();

        org.assertj.core.api.Assertions.assertThat(
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleListChoice(player1, "HUMAN");
        org.assertj.core.api.Assertions.assertThat(
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleListChoice(player2, "HUMAN");

        harness.assertInGraveyard(player1, "Harsh Mercy");
        org.assertj.core.api.Assertions.assertThat(gd.stack).isEmpty();
        org.assertj.core.api.Assertions.assertThat(
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
    }

    @Test
    void faceDownCreatureIsDestroyedEvenWhenItsPrintedTypeWasChosen() {
        harness.setHand(player1, List.of(new DaruLancer()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        var lancer = findPermanent(player1, "Daru Lancer");
        org.assertj.core.api.Assertions.assertThat(lancer.isFaceDown()).isTrue();
        cast();

        harness.handleListChoice(player1, "HUMAN");
        harness.handleListChoice(player2, "SOLDIER");

        org.assertj.core.api.Assertions.assertThat(gd.playerBattlefields.get(player1.getId()))
                .doesNotContain(lancer);
        harness.assertInGraveyard(player1, "Daru Lancer");
    }

    private void cast() {
        harness.castFromHand(player1, new HarshMercy(), "{2}{W}");
        harness.passBothPriorities();
    }
}
