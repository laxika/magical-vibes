package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KnightOfMeadowgrain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChampionOfTheClachanTest extends BaseCardTest {

    @Test
    @DisplayName("Can behold a Kithkin permanent and returns it to its owner's hand when Champion leaves")
    void beholdsPermanentAndReturnsItToHand() {
        Card beheldCard = new KnightOfMeadowgrain();
        Permanent beheldPermanent = harness.addToBattlefieldAndReturn(player1, beheldCard);
        harness.addToBattlefield(player1, new KnightOfMeadowgrain());
        harness.setHand(player1, List.of(new ChampionOfTheClachan()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreatureWithBeholdPermanent(player1, 0, beheldPermanent.getId());
        harness.passBothPriorities();

        Permanent champion = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof ChampionOfTheClachan)
                .findFirst().orElseThrow();
        assertThat(gd.findExiledCard(beheldCard.getId())).isNotNull();
        assertThat(gd.exileReturnOnPermanentLeave).containsKey(champion.getId());
        Permanent otherKithkin = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof KnightOfMeadowgrain)
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, champion)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, otherKithkin)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, otherKithkin)).isEqualTo(3);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, champion));

        assertThat(gd.findExiledCard(beheldCard.getId())).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(beheldCard);
    }

    @Test
    @DisplayName("Can behold a Kithkin card from hand")
    void beholdsCardFromHand() {
        Card beheldCard = new KnightOfMeadowgrain();
        harness.setHand(player1, List.of(new ChampionOfTheClachan(), beheldCard));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreatureWithBeholdHandCard(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(beheldCard.getId())).isNotNull();
        Permanent champion = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof ChampionOfTheClachan)
                .findFirst().orElseThrow();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, champion));

        assertThat(gd.playerHands.get(player1.getId())).contains(beheldCard);
    }

    @Test
    @DisplayName("Rejects a non-Kithkin behold choice")
    void rejectsNonKithkinBeholdChoice() {
        Card nonKithkin = new GrizzlyBears();
        harness.setHand(player1, List.of(new ChampionOfTheClachan(), nonKithkin));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castCreatureWithBeholdHandCard(player1, 0, 1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2).contains(nonKithkin);
        assertThat(gd.findExiledCard(nonKithkin.getId())).isNull();
    }
}
