package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChandraNalaar.class, GrizzlyBears.class, MagmaticSinkhole.class, Mountain.class})
class MagmaticSinkholeTest extends BaseCardTest {

    @Test
    @DisplayName("Delve pays the generic cost and deals 5 damage to a target creature")
    void delvesAndDealsDamageToCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        List<GrizzlyBears> graveyard = List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears());
        harness.setGraveyard(player1, List.copyOf(graveyard));
        harness.setHand(player1, List.of(new MagmaticSinkhole()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstantWithMultipleGraveyardExile(
                player1, 0, harness.getPermanentId(player2, "Grizzly Bears"), List.of(0, 1, 2, 3, 4));

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(graveyard);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Deals 5 damage to a target planeswalker")
    void dealsDamageToPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 6);
        harness.setHand(player1, List.of(new MagmaticSinkhole()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castAndResolveInstant(player1, 0, planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new MagmaticSinkhole()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, harness.getPermanentId(player2, "Mountain")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canPayWithPartialDelveAndMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        Mountain exiled = new Mountain();
        Mountain remaining = new Mountain();
        harness.setGraveyard(player1, List.of(exiled, remaining));
        harness.setHand(player1, List.of(new MagmaticSinkhole()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstantWithMultipleGraveyardExile(player1, 0, target.getId(), List.of(0));

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(exiled);
        harness.passBothPriorities();
        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    void delveCannotPayTheRedManaRequirement() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(
                new Mountain(), new Mountain(), new Mountain(), new Mountain(), new Mountain()));
        harness.setHand(player1, List.of(new MagmaticSinkhole()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstantWithMultipleGraveyardExile(
                player1, 0, harness.getPermanentId(player2, "Grizzly Bears"), List.of(0, 1, 2, 3, 4)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(5);
    }

    @Test
    void cannotExileMoreThanTheGenericCost() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(
                new Mountain(), new Mountain(), new Mountain(),
                new Mountain(), new Mountain(), new Mountain()));
        harness.setHand(player1, List.of(new MagmaticSinkhole()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstantWithMultipleGraveyardExile(
                player1, 0, harness.getPermanentId(player2, "Grizzly Bears"), List.of(0, 1, 2, 3, 4, 5)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(6);
    }

    @Test
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new MagmaticSinkhole()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
