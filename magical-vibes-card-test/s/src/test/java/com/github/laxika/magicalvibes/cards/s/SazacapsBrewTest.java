package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.BarkformHarvester;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SazacapsBrew.class, Forest.class, BarkformHarvester.class})
class SazacapsBrewTest extends BaseCardTest {

    @Test
    void withoutGiftTheTargetPlayerDrawsTwoAndTheAdditionalDiscardIsPaid() {
        Forest discarded = new Forest();
        harness.setHand(player1, List.of(new SazacapsBrew(), discarded));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithDiscard(player1, 0, player2.getId(), 1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(discarded.getId());
        assertThat(findPermanents(player2, "Fish")).isEmpty();
    }

    @Test
    void promisingGiftCreatesATappedFishAndBoostsTheGiftTarget() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new BarkformHarvester());
        Forest discarded = new Forest();
        harness.setHand(player1, List.of(new SazacapsBrew(), discarded));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        cast(List.of(player2.getId(), bear.getId()), true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(bear.getPowerModifier()).isEqualTo(2);
        Permanent fish = findPermanent(player2, "Fish");
        assertThat(fish).isNotNull();
        assertThat(fish.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(discarded.getId());
    }

    @Test
    void promisedGiftRequiresACreatureControlledByTheCaster() {
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new BarkformHarvester());
        harness.setHand(player1, List.of(new SazacapsBrew(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> cast(List.of(player2.getId(), opponentBear.getId()), true))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    private void cast(List<UUID> targetIds, boolean giftPromised) {
        gs.playCardWithGift(gd, player1, 0, 0, null, null, targetIds, 1, giftPromised);
    }

    @Test
    void casterCanDrawTheCardsWithoutPromisingAGift() {
        harness.setHand(player1, List.of(new SazacapsBrew(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstantWithDiscard(player1, 0, player1.getId(), 1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(findPermanents(player2, "Fish")).isEmpty();
    }

    @Test
    void cannotCastWithoutAnotherCardToDiscard() {
        harness.setHand(player1, List.of(new SazacapsBrew()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstantWithDiscard(player1, 0, player1.getId(), null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void promisingGiftRequiresChoosingACreature() {
        harness.setHand(player1, List.of(new SazacapsBrew(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> cast(List.of(player1.getId()), true))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotChooseACreatureWhenGiftWasNotPromised() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BarkformHarvester());
        harness.setHand(player1, List.of(new SazacapsBrew(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> cast(List.of(player1.getId(), creature.getId()), false))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void creatureLeavingBeforeResolutionDoesNotPreventGiftOrDraw() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BarkformHarvester());
        harness.setHand(player1, List.of(new SazacapsBrew(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 2);

        cast(List.of(player1.getId(), creature.getId()), true);
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerGraveyards.get(player1.getId()).add(creature.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(findPermanent(player2, "Fish").isTapped()).isTrue();
        assertThat(creature.getPowerModifier()).isZero();
    }

    @Test
    void creatureChangingControllerBeforeResolutionIsNotBoosted() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BarkformHarvester());
        harness.setHand(player1, List.of(new SazacapsBrew(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 2);

        cast(List.of(player1.getId(), creature.getId()), true);
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerBattlefields.get(player2.getId()).add(creature);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(findPermanent(player2, "Fish").isTapped()).isTrue();
        assertThat(creature.getPowerModifier()).isZero();
    }
}
