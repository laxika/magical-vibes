package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BrallinSkysharkRider;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShabrazTheSkyshark.class, BrallinSkysharkRider.class})
class ShabrazTheSkysharkTest extends BaseCardTest {

    @Test
    @DisplayName("Partner with lets the target player search for Brallin")
    void partnerWithSearchesTargetPlayersLibrary() {
        Card brallin = new BrallinSkysharkRider();
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
        harness.setLibrary(player1, List.of(new ShabrazTheSkyshark()));
        harness.setLibrary(player2, List.of(new ShabrazTheSkyshark()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        resolveAllTriggers();

        assertThat(shabraz.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));
        resolveAllTriggers();

        assertThat(shabraz.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("The activated ability grants flying to a Human until end of turn")
    void grantsFlyingToHumanUntilEndOfTurn() {
        harness.addToBattlefield(player1, new ShabrazTheSkyshark());
        Permanent human = harness.addToBattlefieldAndReturn(player1, new BrallinSkysharkRider());
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
        Permanent nonHuman = harness.addToBattlefieldAndReturn(player1, new ShabrazTheSkyshark());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, nonHuman.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Human");
    }

    @Test
    @DisplayName("The targeted player may decline the partner search")
    void partnerSearchCanBeDeclined() {
        Card brallin = new BrallinSkysharkRider();
        harness.setLibrary(player2, List.of(brallin));
        harness.setHand(player2, List.of());
        harness.enterBattlefieldAndReturn(player1, new ShabrazTheSkyshark());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(brallin);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Searching for Brallin does not count as drawing a card")
    void partnerSearchDoesNotTriggerDrawAbility() {
        Permanent shabraz = harness.enterBattlefieldAndReturn(player1, new ShabrazTheSkyshark());
        Card brallin = new BrallinSkysharkRider();
        harness.setLibrary(player1, List.of(brallin));
        harness.setHand(player1, List.of());
        harness.setLife(player1, 20);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(brallin);
        assertThat(shabraz.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Each of several draws produces a separate counter and life gain")
    void multipleDrawsEachTrigger() {
        Permanent shabraz = harness.addToBattlefieldAndReturn(player1, new ShabrazTheSkyshark());
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(new BrallinSkysharkRider(), new ShabrazTheSkyshark()));
        harness.inMutationScope(() -> {
            harness.getDrawService().resolveDrawCard(gd, player1.getId());
            harness.getDrawService().resolveDrawCard(gd, player1.getId());
        });
        resolveAllTriggers();

        assertThat(shabraz.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Life is still gained when Shabraz leaves before its draw trigger resolves")
    void gainsLifeWithoutSourceOnBattlefield() {
        Permanent shabraz = harness.addToBattlefieldAndReturn(player1, new ShabrazTheSkyshark());
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(new BrallinSkysharkRider()));
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(shabraz);
        gd.playerGraveyards.get(player1.getId()).add(shabraz.getCard());
        resolveAllTriggers();

        assertThat(shabraz.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Blue mana pays the hybrid cost to give an opponent's Human flying")
    void blueManaGrantsFlyingToOpponentsHuman() {
        harness.addToBattlefield(player1, new ShabrazTheSkyshark());
        Permanent human = harness.addToBattlefieldAndReturn(player2, new BrallinSkysharkRider());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, human.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, human, Keyword.FLYING)).isTrue();
    }
}
