package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MarchOfRecklessJoy.class, Shock.class, SavannahLions.class,
        GrizzlyBears.class, HillGiant.class})
class MarchOfRecklessJoyTest extends BaseCardTest {

    @Test
    void exilingRedCardsFromHandReducesGenericCost() {
        Shock firstRedCard = new Shock();
        Shock secondRedCard = new Shock();
        Shock libraryCard = new Shock();
        harness.setHand(player1, List.of(new MarchOfRecklessJoy(), firstRedCard, secondRedCard));
        harness.setLibrary(player1, List.of(libraryCard));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstantForXWithDiscards(player1, 0, 2, List.of(), List.of(1, 2));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card())
                .containsExactlyInAnyOrder(firstRedCard, secondRedCard, libraryCard);
    }

    @Test
    void allowsPlayingUpToTwoExiledCards() {
        SavannahLions firstCard = new SavannahLions();
        GrizzlyBears secondCard = new GrizzlyBears();
        HillGiant thirdCard = new HillGiant();
        harness.setHand(player1, List.of(new MarchOfRecklessJoy()));
        harness.setLibrary(player1, List.of(firstCard, secondCard, thirdCard));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstantForX(player1, 0, 3, List.of());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castFromExile(player1, firstCard.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, secondCard.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromExile(player1, thirdCard.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No permission to play this exiled card");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard())
                .containsExactly(firstCard, secondCard);
        assertThat(gd.findExiledCard(thirdCard.getId())).isNotNull();
    }
}
