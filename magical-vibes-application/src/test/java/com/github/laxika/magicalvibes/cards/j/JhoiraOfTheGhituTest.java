package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.b.BlindPhantasm;
import com.github.laxika.magicalvibes.cards.d.DryadArbor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JhoiraOfTheGhitu.class, BlindPhantasm.class, DryadArbor.class})
class JhoiraOfTheGhituTest extends BaseCardTest {

    @Test
    void exilesNonlandCardAndPutsFourTimeCountersOnIt() {
        Permanent jhoira = jhoira();
        BlindPhantasm first = new BlindPhantasm();

        harness.setHand(player1, List.of(first));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        activate(jhoira, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(first);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(first);
        assertThat(gd.exiledCardTimeCounters).containsEntry(first.getId(), 4);
    }

    @Test
    void separateActivationsSuspendTheCardPaidForEachActivation() {
        Permanent jhoira = jhoira();
        BlindPhantasm first = new BlindPhantasm();
        BlindPhantasm second = new BlindPhantasm();

        harness.setHand(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gd.exiledCardTimeCounters)
                .containsEntry(first.getId(), 4)
                .containsEntry(second.getId(), 4);
    }

    @Test
    void lastTimeCounterOffersAFreeCastWithHaste() {
        Permanent jhoira = jhoira();
        BlindPhantasm card = new BlindPhantasm();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        activate(jhoira, 0);

        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent castPermanent = findPermanent(player1, "Blind Phantasm");
        assertThat(gqs.hasKeyword(gd, castPermanent, Keyword.HASTE)).isTrue();
    }

    @Test
    void decliningLastTimeCounterCastLeavesCardExiledWithoutCounters() {
        Permanent jhoira = jhoira();
        BlindPhantasm card = new BlindPhantasm();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        activate(jhoira, 0);

        for (int i = 0; i < 4; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(card);
        assertThat(findPermanents(player1, "Blind Phantasm")).isEmpty();
    }

    @Test
    void cannotExileALandAsTheActivationCost() {
        Permanent jhoira = jhoira();
        DryadArbor landCreature = new DryadArbor();
        harness.setHand(player1, List.of(landCreature));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(landCreature);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(landCreature);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    private Permanent jhoira() {
        return harness.addToBattlefieldAndReturn(player1, new JhoiraOfTheGhitu());
    }

    private void activate(Permanent jhoira, int handCardIndex) {
        int jhoiraIndex = gd.playerBattlefields.get(player1.getId()).indexOf(jhoira);
        harness.activateAbility(player1, jhoiraIndex, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        harness.handleCardChosen(player1, handCardIndex);
        harness.passBothPriorities();
    }
}
