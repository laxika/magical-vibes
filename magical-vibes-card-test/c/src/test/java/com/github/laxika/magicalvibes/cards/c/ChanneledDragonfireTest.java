package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.UginEyeOfTheStorms;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChanneledDragonfire.class, GrizzlyBears.class, UginEyeOfTheStorms.class})
class ChanneledDragonfireTest extends BaseCardTest {

    @Test
    void dealsTwoDamageToTargetPlayer() {
        harness.setHand(player1, List.of(new ChanneledDragonfire()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void dealsTwoDamageToTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ChanneledDragonfire()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, bears.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void dealsTwoDamageToTargetPlaneswalker() {
        Permanent ugin = harness.addToBattlefieldAndReturn(player2, new UginEyeOfTheStorms());
        ugin.setCounterCount(CounterType.LOYALTY, 7);
        harness.setHand(player1, List.of(new ChanneledDragonfire()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, ugin.getId());

        assertThat(ugin.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        harness.assertLife(player2, 20);
    }

    @Test
    void harmonizeCastsFromGraveyardAndExilesTheSpell() {
        ChanneledDragonfire spell = new ChanneledDragonfire();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castAndResolveFlashback(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertNotInGraveyard(player1, "Channeled Dragonfire");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    void harmonizeReducesGenericCostByTappedCreaturePower() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        ChanneledDragonfire spell = new ChanneledDragonfire();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashbackWithTapCost(player1, 0, player2.getId(), List.of(creature.getId()));
        assertThat(creature.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    void normalCastGoesToGraveyardAndCanThenBeHarmonized() {
        ChanneledDragonfire spell = new ChanneledDragonfire();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertInGraveyard(player1, "Channeled Dragonfire");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(spell);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castAndResolveFlashback(player1, 0, player2.getId());

        harness.assertLife(player2, 16);
        harness.assertNotInGraveyard(player1, "Channeled Dragonfire");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    void harmonizeCannotTapAnOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new ChanneledDragonfire()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castFlashbackWithTapCost(
                player1, 0, player2.getId(), List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(creature.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Channeled Dragonfire");
    }

    @Test
    void harmonizeCanTapASummoningSickCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setSummoningSick(true);
        harness.setGraveyard(player1, List.of(new ChanneledDragonfire()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashbackWithTapCost(player1, 0, player2.getId(), List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        harness.assertLife(player2, 18);
        harness.assertNotInGraveyard(player1, "Channeled Dragonfire");
    }

    @Test
    void harmonizeCannotTapAnAlreadyTappedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.tap();
        harness.setGraveyard(player1, List.of(new ChanneledDragonfire()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castFlashbackWithTapCost(
                player1, 0, player2.getId(), List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Channeled Dragonfire");
    }

    @Test
    void harmonizeCannotTapMoreThanOneCreature() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new ChanneledDragonfire()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castFlashbackWithTapCost(
                player1, 0, player2.getId(), List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Channeled Dragonfire");
    }

    @Test
    void harmonizeStillRequiresBothRedManaAfterPowerReduction() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new ChanneledDragonfire()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castFlashbackWithTapCost(
                player1, 0, player2.getId(), List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(creature.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Channeled Dragonfire");
    }

    @Test
    void harmonizedSpellIsExiledWhenItsOnlyTargetBecomesIllegal() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        ChanneledDragonfire spell = new ChanneledDragonfire();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castFlashback(player1, 0, creature.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, creature));
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Channeled Dragonfire");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        harness.assertLife(player2, 20);
    }
}
