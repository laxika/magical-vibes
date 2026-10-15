package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.Conspiracy;
import com.github.laxika.magicalvibes.cards.e.EnormousBaloth;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TotemSpeaker.class, EnormousBaloth.class, FugitiveWizard.class, Conspiracy.class})
class TotemSpeakerTest extends BaseCardTest {

    @Test
    @DisplayName("May gain 3 life when a Beast enters")
    void mayGainLifeWhenBeastEnters() {
        harness.addToBattlefield(player1, new TotemSpeaker());
        harness.setLife(player1, 20);

        harness.castFromHand(player1, new EnormousBaloth(), "{6}{G}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Does not gain life when declining")
    void doesNotGainLifeWhenDeclining() {
        harness.addToBattlefield(player1, new TotemSpeaker());
        harness.setLife(player1, 20);

        harness.castFromHand(player1, new EnormousBaloth(), "{6}{G}");
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Triggers for a Beast entering under an opponent's control")
    void triggersForOpponentsBeast() {
        harness.addToBattlefield(player1, new TotemSpeaker());
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new EnormousBaloth(), "{6}{G}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Does not trigger for a non-Beast creature")
    void doesNotTriggerForNonBeast() {
        harness.addToBattlefield(player1, new TotemSpeaker());
        harness.setLife(player1, 20);

        harness.castFromHand(player1, new FugitiveWizard(), "{U}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Triggers for its own entry when Conspiracy makes it a Beast")
    void triggersForOwnEntryAsBeast() {
        harness.castFromHand(player1, new Conspiracy(), "{3}{B}{B}");
        resolveAllTriggers();
        harness.handleListChoice(player1, CardSubtype.BEAST.name());
        harness.setLife(player1, 20);

        harness.castFromHand(player1, new TotemSpeaker(), "{4}{G}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Triggers for an opposing creature made a Beast by its controller's Conspiracy")
    void triggersForOpponentsCreatureMadeBeast() {
        harness.addToBattlefield(player2, new TotemSpeaker());
        harness.setLife(player2, 20);
        harness.castFromHand(player1, new Conspiracy(), "{3}{B}{B}");
        resolveAllTriggers();
        harness.handleListChoice(player1, CardSubtype.BEAST.name());

        harness.castFromHand(player1, new FugitiveWizard(), "{U}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        harness.assertLife(player2, 23);
    }

    @Test
    @DisplayName("Its controller's Conspiracy does not make an opposing non-Beast trigger")
    void doesNotApplyOwnConspiracyToOpponentsCreature() {
        harness.addToBattlefield(player1, new TotemSpeaker());
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new Conspiracy(), "{3}{B}{B}");
        resolveAllTriggers();
        harness.handleListChoice(player1, CardSubtype.BEAST.name());

        harness.enterBattlefieldAndReturn(player2, new FugitiveWizard());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertLife(player1, 20);
    }
}
