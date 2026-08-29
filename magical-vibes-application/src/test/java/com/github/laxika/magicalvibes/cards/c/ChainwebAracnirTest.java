package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.c.CloudSprite;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChainwebAracnir.class, CloudSprite.class, GrizzlyBears.class})
class ChainwebAracnirTest extends BaseCardTest {

    @Test
    void enteringDealsDamageToTargetOpposingFlyingCreature() {
        ChainwebAracnir aracnir = new ChainwebAracnir();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CloudSprite());
        harness.setHand(player1, List.of(aracnir));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Cloud Sprite");
        Permanent entered = findPermanent(player1, "Chainweb Aracnir");
        assertThat(entered.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void cannotTargetNonFlyingCreature() {
        ChainwebAracnir aracnir = new ChainwebAracnir();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(aracnir));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature with flying");
    }

    @Test
    void escapingExilesFourCardsEntersWithThreeCountersAndDealsDamage() {
        ChainwebAracnir aracnir = new ChainwebAracnir();
        List<Card> otherCards = List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CloudSprite());
        harness.setGraveyard(player1, List.of(aracnir, otherCards.get(0), otherCards.get(1),
                otherCards.get(2), otherCards.get(3)));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playFlashbackSpell(gd, player1, 0, null, target.getId(), List.of(), List.of(1, 2, 3, 4), null);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(otherCards);

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Cloud Sprite");
        Permanent escaped = findPermanent(player1, "Chainweb Aracnir");
        assertThat(escaped.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void escapeRequiresFourOtherCardsInGraveyard() {
        harness.setGraveyard(player1, List.of(
                new ChainwebAracnir(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(1, 2, 3)))
                .isInstanceOf(IllegalStateException.class);
    }
}
