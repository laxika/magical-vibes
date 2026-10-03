package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChampionsOfTheShoal.class, ChangelingWayfinder.class})
class ChampionsOfTheShoalTest extends BaseCardTest {

    @Test
    @DisplayName("ETB taps up to one target creature and puts a stun counter on it")
    void etbTapsAndStunsTarget() {
        Card beheldCard = new ChangelingWayfinder();
        Permanent beheldPermanent = harness.addToBattlefieldAndReturn(player1, beheldCard);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChangelingWayfinder());
        harness.setHand(player1, List.of(new ChampionsOfTheShoal()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        castWithBeholdAndTarget(beheldPermanent, target);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    private void castWithBeholdAndTarget(Permanent beheldPermanent, Permanent target) {
        gs.playCard(gd, player1, 0, 0, target.getId(), null, List.of(), List.of(), false,
                null, null, null, null, null, false, null, null, null, null, List.of(), false,
                beheldPermanent.getId(), null);
    }

    @Test
    @DisplayName("Becomes-tapped trigger taps and stuns up to one target creature")
    void becomesTappedTapsAndStunsTarget() {
        addCreatureReady(player1, new ChampionsOfTheShoal());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChangelingWayfinder());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    @DisplayName("The beheld card returns to its owner's hand when Champions leaves")
    void beheldCardReturnsWhenSourceLeaves() {
        Card beheldCard = new ChangelingWayfinder();
        Permanent beheldPermanent = harness.addToBattlefieldAndReturn(player1, beheldCard);
        harness.setHand(player1, List.of(new ChampionsOfTheShoal()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreatureWithBeholdPermanent(player1, 0, beheldPermanent.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        Permanent source = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof ChampionsOfTheShoal)
                .findFirst().orElseThrow();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, source));

        assertThat(gd.findExiledCard(beheldCard.getId())).isNotNull();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(beheldCard);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(beheldCard.getId())).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(beheldCard);
    }

    @Test
    @DisplayName("A Merfolk card from hand can pay the exile cost with no ETB target")
    void beholdCardFromHandAndDeclineEtbTarget() {
        Card beheldCard = new ChangelingWayfinder();
        harness.setHand(player1, List.of(new ChampionsOfTheShoal(), beheldCard));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreatureWithBeholdHandCard(player1, 0, 1);
        assertThat(gd.findExiledCard(beheldCard.getId())).isNotNull();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(beheldCard);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        Permanent source = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof ChampionsOfTheShoal)
                .findFirst().orElseThrow();
        assertThat(source.isTapped()).isFalse();
        assertThat(source.getCounterCount(CounterType.STUN)).isZero();
        assertThat(gd.findExiledCard(beheldCard.getId())).isNotNull();
    }

    @Test
    @DisplayName("The becomes-tapped trigger can decline to target a creature")
    void becomesTappedCanChooseNoTarget() {
        Permanent source = addCreatureReady(player1, new ChampionsOfTheShoal());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new ChangelingWayfinder());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(other.isTapped()).isFalse();
        assertThat(other.getCounterCount(CounterType.STUN)).isZero();
        assertThat(source.getCounterCount(CounterType.STUN)).isZero();
    }
}
