package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChampionsOfTheShoalTest extends BaseCardTest {

    @Test
    @DisplayName("ETB taps up to one target creature and puts a stun counter on it")
    void etbTapsAndStunsTarget() {
        Card beheldCard = new CoralMerfolk();
        Permanent beheldPermanent = harness.addToBattlefieldAndReturn(player1, beheldCard);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
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
        Permanent source = new Permanent(new ChampionsOfTheShoal());
        source.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(source);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        source.tap();
        harness.inMutationScope(() -> {
            harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, source);
            harness.getTriggerCollectionService().processNextEntersTriggerTarget(gd);
        });
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
        Card beheldCard = new CoralMerfolk();
        Permanent beheldPermanent = harness.addToBattlefieldAndReturn(player1, beheldCard);
        harness.setHand(player1, List.of(new ChampionsOfTheShoal()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreatureWithBeholdPermanent(player1, 0, beheldPermanent.getId());
        harness.passBothPriorities();

        Permanent source = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof ChampionsOfTheShoal)
                .findFirst().orElseThrow();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, source));

        assertThat(gd.findExiledCard(beheldCard.getId())).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(beheldCard);
    }
}
