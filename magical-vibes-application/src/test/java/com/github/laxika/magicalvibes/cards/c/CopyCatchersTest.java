package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DazzlingLights;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({CopyCatchers.class, DazzlingLights.class})
class CopyCatchersTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {1}{U} after surveiling creates a token copy")
    void paysToCreateTokenCopy() {
        Permanent copyCatchers = addCreatureReady(player1, new CopyCatchers());
        resolveSurveil(copyCatchers, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1);
    }

    @Test
    @DisplayName("Declining the payment creates no token copy")
    void declinesToCreateTokenCopy() {
        Permanent copyCatchers = addCreatureReady(player1, new CopyCatchers());
        resolveSurveil(copyCatchers, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .isEmpty();
    }

    @Test
    @DisplayName("An opponent surveiling does not trigger Copy Catchers")
    void opponentSurveilDoesNotTrigger() {
        Permanent copyCatchers = addCreatureReady(player1, new CopyCatchers());
        harness.setLibrary(player2, List.of(new CopyCatchers(), new CopyCatchers()));
        harness.setHand(player2, List.of(new DazzlingLights()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, copyCatchers.getId());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .isEmpty();
    }

    @Test
    @DisplayName("Surveiling an empty library still allows creating a copy")
    void emptyLibraryStillTriggers() {
        Permanent copyCatchers = addCreatureReady(player1, new CopyCatchers());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new DazzlingLights()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, copyCatchers.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1);
    }

    @Test
    @DisplayName("A token copy also triggers on later surveils, once per surveil action")
    void tokenCopyRetainsSurveilAbility() {
        Permanent copyCatchers = addCreatureReady(player1, new CopyCatchers());
        resolveSurveil(copyCatchers, true);
        harness.setLibrary(player1, List.of(new CopyCatchers(), new CopyCatchers()));
        harness.setHand(player1, List.of(new DazzlingLights()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, copyCatchers.getId());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));
        for (int trigger = 0; trigger < 2; trigger++) {
            harness.passBothPriorities();
            assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
            harness.handleMayAbilityChosen(player1, true);
        }
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(3);
        assertThat(gd.stack).isEmpty();
    }

    private void resolveSurveil(Permanent copyCatchers, boolean pay) {
        Card topCard = new CopyCatchers();
        Card secondCard = new CopyCatchers();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.setHand(player1, List.of(new DazzlingLights()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, copyCatchers.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, pay);
        harness.passBothPriorities();
    }
}
