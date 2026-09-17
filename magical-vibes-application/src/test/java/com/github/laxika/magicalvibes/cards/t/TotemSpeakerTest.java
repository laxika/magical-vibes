package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.EnormousBaloth;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TotemSpeaker.class, EnormousBaloth.class, FugitiveWizard.class})
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
}
