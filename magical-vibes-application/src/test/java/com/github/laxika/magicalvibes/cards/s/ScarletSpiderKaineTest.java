package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScarletSpiderKaine.class, Forest.class})
class ScarletSpiderKaineTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card puts a +1/+1 counter on Scarlet Spider")
    void discardingCardPutsCounterOnScarletSpider() {
        Permanent scarletSpider = castScarletSpiderWithCardInHand();

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(scarletSpider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
    }

    @Test
    @DisplayName("Declining the discard leaves Scarlet Spider without a counter")
    void decliningDiscardDoesNotPutCounterOnScarletSpider() {
        Permanent scarletSpider = castScarletSpiderWithCardInHand();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(scarletSpider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void emptyHandDoesNotPutCounterOnScarletSpider() {
        harness.setHand(player1, List.of(new ScarletSpiderKaine()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(findPermanent(player1, "Scarlet Spider, Kaine")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void mayhemCanBePaidWithOneBlackMana() {
        castForMayhem(ManaColor.BLACK);
    }

    @Test
    void mayhemCanBePaidWithOneRedMana() {
        castForMayhem(ManaColor.RED);
    }

    @Test
    void mayhemRequiresThisCardToHaveBeenDiscarded() {
        ScarletSpiderKaine card = new ScarletSpiderKaine();
        ScarletSpiderKaine other = new ScarletSpiderKaine();
        harness.setGraveyard(player1, List.of(card, other));
        gd.cardsDiscardedOrCycledThisTurn.put(player1.getId(), new HashSet<>(Set.of(other.getId())));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card, other);
    }

    @Test
    void mayhemStillRequiresMainPhase() {
        prepareMayhem();
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Scarlet Spider, Kaine");
    }

    @Test
    void menacePreventsSingleBlocker() {
        addCreatureReady(player1, new ScarletSpiderKaine());
        addCreatureReady(player2, new ScarletSpiderKaine());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    private void castForMayhem(ManaColor color) {
        prepareMayhem();
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, color, 1);
        harness.castFromGraveyard(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertNotInGraveyard(player1, "Scarlet Spider, Kaine");
        harness.assertInGraveyard(player1, "Forest");
        assertThat(findPermanent(player1, "Scarlet Spider, Kaine")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
    }

    private void prepareMayhem() {
        ScarletSpiderKaine card = new ScarletSpiderKaine();
        harness.setGraveyard(player1, List.of(card));
        gd.cardsDiscardedOrCycledThisTurn.put(player1.getId(), new HashSet<>(Set.of(card.getId())));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private Permanent castScarletSpiderWithCardInHand() {
        harness.setHand(player1, List.of(new ScarletSpiderKaine(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        return findPermanent(player1, "Scarlet Spider, Kaine");
    }
}
