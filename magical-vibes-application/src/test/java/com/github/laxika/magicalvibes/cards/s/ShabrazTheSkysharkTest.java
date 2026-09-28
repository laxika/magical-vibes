package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShabrazTheSkyshark.class, EliteVanguard.class, GrizzlyBears.class})
class ShabrazTheSkysharkTest extends BaseCardTest {

    @Test
    @DisplayName("Partner with lets the target player search for Brallin")
    void partnerWithSearchesTargetPlayersLibrary() {
        Card brallin = namedCard("Brallin, Skyshark Rider");
        harness.setLibrary(player2, List.of(brallin));
        harness.setHand(player2, List.of());

        harness.enterBattlefieldAndReturn(player1, new ShabrazTheSkyshark());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.validPlayerIds()).contains(player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(brallin);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Drawing a card puts a +1/+1 counter on Shabraz and gains 1 life")
    void drawingPutsCounterAndGainsLife() {
        Permanent shabraz = harness.addToBattlefieldAndReturn(player1, new ShabrazTheSkyshark());
        harness.setLife(player1, 20);

        advanceToDraw(player1);

        assertThat(shabraz.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);

        advanceToDraw(player2);

        assertThat(shabraz.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("The activated ability grants flying to a Human until end of turn")
    void grantsFlyingToHumanUntilEndOfTurn() {
        harness.addToBattlefield(player1, new ShabrazTheSkyshark());
        Permanent human = harness.addToBattlefieldAndReturn(player1, new EliteVanguard());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, human.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, human, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, human, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The activated ability cannot target a non-Human")
    void rejectsNonHumanTarget() {
        harness.addToBattlefield(player1, new ShabrazTheSkyshark());
        Permanent nonHuman = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, nonHuman.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Human");
    }

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Card namedCard(String name) {
        Card card = new Card();
        card.setName(name);
        return card;
    }
}
