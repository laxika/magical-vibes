package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
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

@CardUsed({CharnelSerenade.class, Forest.class, GrizzlyBears.class})
class CharnelSerenadeTest extends BaseCardTest {

    @Test
    @DisplayName("Surveils 3, returns a creature with a finality counter, and suspends itself")
    void surveilsReturnsCreatureAndExilesItself() {
        Card topCard = new Forest();
        Card secondCard = new Forest();
        Card thirdCard = new Forest();
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, secondCard, thirdCard));
        harness.setGraveyard(player1, List.of(creature));

        CharnelSerenade card = new CharnelSerenade();
        harness.setHand(player1, List.of(card));
        addNormalMana();

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(topCard, secondCard, thirdCard);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1, 2), List.of()));
        harness.passBothPriorities();

        harness.handleGraveyardCardChosen(player1, 0);

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(creature.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.suspendedSpellExiles)
                .containsExactly(new GameData.SuspendedSpellExile(card.getId(), player1.getId(), 3));
    }

    @Test
    @DisplayName("Suspend casts Charnel Serenade for free and exiles it again")
    void suspendCastsForFree() {
        Card topCard = new Forest();
        Card secondCard = new Forest();
        Card thirdCard = new Forest();
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, secondCard, thirdCard));
        harness.setGraveyard(player1, List.of(creature));

        CharnelSerenade card = new CharnelSerenade();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateHandAbility(player1, 0, null);

        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1, 2), List.of()));
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(creature.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        assertThat(gd.suspendedSpellExiles)
                .containsExactly(new GameData.SuspendedSpellExile(card.getId(), player1.getId(), 3));
    }

    private void addNormalMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 2);
    }
}
