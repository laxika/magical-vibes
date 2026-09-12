package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SatsukiTheLivingLore.class)
class SatsukiTheLivingLoreTest extends BaseCardTest {

    private static final String RETURN_BATTLEFIELD_MODE =
            "Return target Saga or enchantment creature you control to its owner's hand";
    private static final String RETURN_GRAVEYARD_MODE =
            "Return target Saga card from your graveyard to your hand";

    @Test
    void tapAbilityPutsLoreCountersOnEachSagaYouControl() {
        Permanent firstSaga = harness.addToBattlefieldAndReturn(player1, saga("First Saga"));
        Permanent secondSaga = harness.addToBattlefieldAndReturn(player1, saga("Second Saga"));
        Permanent opponentSaga = harness.addToBattlefieldAndReturn(player2, saga("Opponent Saga"));
        Permanent satsuki = harness.addToBattlefieldAndReturn(player1, new SatsukiTheLivingLore());
        satsuki.setSummoningSick(false);

        harness.activateAbility(player1, battlefieldIndex(satsuki), 0, null, null);
        harness.passBothPriorities();

        assertThat(firstSaga.getCounterCount(CounterType.LORE)).isEqualTo(1);
        assertThat(secondSaga.getCounterCount(CounterType.LORE)).isEqualTo(1);
        assertThat(opponentSaga.getCounterCount(CounterType.LORE)).isZero();
    }

    @Test
    void deathTriggerReturnsTargetedSagaOrEnchantmentCreatureYouControl() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, saga("Battlefield Saga"));
        Permanent enchantmentCreature = harness.addToBattlefieldAndReturn(
                player1, enchantmentCreature("Enchantment Creature"));
        Permanent ordinaryCreature = harness.addToBattlefieldAndReturn(player1, creature("Ordinary Creature"));
        Permanent opponentSaga = harness.addToBattlefieldAndReturn(player2, saga("Opponent Saga"));
        harness.addToBattlefield(player1, new SatsukiTheLivingLore());

        killSatsuki();
        harness.handleListChoice(player1, RETURN_BATTLEFIELD_MODE);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds())
                .contains(saga.getId(), enchantmentCreature.getId())
                .doesNotContain(ordinaryCreature.getId(), opponentSaga.getId());

        harness.handlePermanentChosen(player1, saga.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).contains(saga.getCard().getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getId)
                .doesNotContain(saga.getId());
    }

    @Test
    void deathTriggerReturnsTargetSagaFromYourGraveyard() {
        Card saga = saga("Graveyard Saga");
        Card nonSaga = creature("Not a Saga");
        Card opponentSaga = saga("Opponent Graveyard Saga");
        harness.setGraveyard(player1, List.of(saga, nonSaga));
        harness.setGraveyard(player2, List.of(opponentSaga));
        harness.addToBattlefield(player1, new SatsukiTheLivingLore());

        killSatsuki();
        harness.handleListChoice(player1, RETURN_GRAVEYARD_MODE);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(saga.getId());

        harness.handleMultipleCardsChosen(player1, List.of(saga.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).contains(saga.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .contains(nonSaga.getId())
                .doesNotContain(saga.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).extracting(Card::getId)
                .containsExactly(opponentSaga.getId());
    }

    @Test
    void deathTriggerMayChooseNoMode() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, saga("Available Saga"));
        harness.addToBattlefield(player1, new SatsukiTheLivingLore());

        killSatsuki();
        harness.handleListChoice(player1, ChooseOneEffect.NO_MODE_LABEL);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getId)
                .contains(saga.getId());
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .doesNotContain(saga.getCard().getId());
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    private void killSatsuki() {
        Permanent satsuki = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof SatsukiTheLivingLore)
                .findFirst()
                .orElseThrow();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, satsuki));
        harness.passBothPriorities();
    }

    private Card saga(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ENCHANTMENT);
        card.setSubtypes(List.of(CardSubtype.SAGA));
        return card;
    }

    private Card enchantmentCreature(String name) {
        Card card = creature(name);
        card.setAdditionalTypes(Set.of(CardType.ENCHANTMENT));
        return card;
    }

    private Card creature(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(1);
        card.setToughness(1);
        return card;
    }
}
