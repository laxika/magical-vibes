package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.c.CloudSprite;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LurrusOfTheDreamDen;
import com.github.laxika.magicalvibes.cards.v.VexingGull;
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

@CardUsed({ChainwebAracnir.class, CloudSprite.class, GrizzlyBears.class,
        LurrusOfTheDreamDen.class, VexingGull.class})
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

    @Test
    void canEnterWithoutAnOpposingFlyingCreature() {
        harness.setHand(player1, List.of(new ChainwebAracnir()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Chainweb Aracnir");
        assertThat(findPermanent(player1, "Chainweb Aracnir")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetOwnFlyingCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new VexingGull());
        harness.setHand(player1, List.of(new ChainwebAracnir()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void escapeCountersArePresentBeforeDamageTriggerResolves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VexingGull());
        harness.setGraveyard(player1, List.of(new ChainwebAracnir(), new ChainwebAracnir(),
                new ChainwebAracnir(), new ChainwebAracnir(), new ChainwebAracnir()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        gs.playFlashbackSpell(gd, player1, 0, null, target.getId(), List.of(), List.of(1, 2, 3, 4), null);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Chainweb Aracnir")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Vexing Gull");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Vexing Gull");
        harness.assertInGraveyard(player2, "Vexing Gull");
    }

    @Test
    void castingThroughLurrusDoesNotGrantEscapeCounters() {
        harness.addToBattlefield(player1, new LurrusOfTheDreamDen());
        harness.setGraveyard(player1, List.of(new ChainwebAracnir()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        prepareMainPhase(player1);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Chainweb Aracnir");
        assertThat(findPermanent(player1, "Chainweb Aracnir")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }
}
