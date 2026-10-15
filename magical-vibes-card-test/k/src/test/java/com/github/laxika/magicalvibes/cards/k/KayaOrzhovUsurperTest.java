package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KayaOrzhovUsurper.class, Forest.class, GrizzlyBears.class, LlanowarElves.class, Shock.class})
class KayaOrzhovUsurperTest extends BaseCardTest {

    @Test
    @DisplayName("+1 exiles up to two cards from one graveyard and gains life for a creature")
    void plusOneExilesCardsAndGainsLifeForCreature() {
        Permanent kaya = addReadyKaya(3);
        Card creature = new GrizzlyBears();
        Card noncreature = new Shock();
        harness.setGraveyard(player2, List.of(creature, noncreature));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0,
                List.of(creature.getId(), noncreature.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(creature.getId(), noncreature.getId());
        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(kaya.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("+1 does not gain life when no creature was exiled")
    void plusOneDoesNotGainLifeWithoutCreature() {
        addReadyKaya(3);
        Card noncreature = new Shock();
        harness.setGraveyard(player2, List.of(noncreature));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(noncreature.getId()));
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(noncreature);
    }

    @Test
    @DisplayName("-1 exiles a nonland permanent with mana value 1 or less")
    void minusOneExilesSmallNonlandPermanent() {
        Permanent kaya = addReadyKaya(3);
        Permanent elves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());

        harness.activateAbility(player1, 0, 1, null, elves.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(elves.getCard());
        assertThat(kaya.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("-1 rejects a land and a permanent with mana value greater than 1")
    void minusOneRejectsInvalidPermanent() {
        addReadyKaya(3);
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Forest");
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-5 deals damage and gains life equal to the target player's owned exiled cards")
    void minusFiveUsesTargetPlayersOwnedExileCount() {
        Permanent kaya = addReadyKaya(5);
        harness.setExile(player2, List.of(new GrizzlyBears(), new Forest(), new Shock()));

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(kaya.getCounterCount(CounterType.LOYALTY)).isZero();
    }

    @Test
    void plusOneCanChooseZeroTargetsWithEmptyGraveyards() {
        Permanent kaya = addReadyKaya(3);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(kaya.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void plusOneGainsOnlyTwoLifeForTwoCreaturesFromOwnGraveyard() {
        addReadyKaya(3);
        Card first = new GrizzlyBears();
        Card second = new LlanowarElves();
        harness.setGraveyard(player1, List.of(first, second));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0,
                List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
    }

    @Test
    void plusOneRejectsTargetsFromDifferentGraveyards() {
        addReadyKaya(3);
        Card first = new GrizzlyBears();
        Card second = new Shock();
        harness.setGraveyard(player1, List.of(first));
        harness.setGraveyard(player2, List.of(second));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, 0, 0,
                List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void plusOneDoesNotGainLifeForCreatureThatLeavesBeforeResolution() {
        addReadyKaya(3);
        Card creature = new GrizzlyBears();
        Card noncreature = new Shock();
        harness.setGraveyard(player2, List.of(creature, noncreature));
        harness.activateAbilityWithGraveyardTargets(player1, 0, 0,
                List.of(creature.getId(), noncreature.getId()));
        harness.setGraveyard(player2, List.of(noncreature));

        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(noncreature);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    void minusFiveCanTargetControllerAndUsesOnlyTheirExiledCards() {
        addReadyKaya(5);
        harness.setExile(player1, List.of(new Forest(), new Shock()));
        harness.setExile(player2, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, 2, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void minusFiveUsesExileCountAtResolution() {
        addReadyKaya(5);
        harness.setExile(player2, List.of(new Forest()));
        harness.setExile(player1, List.of(new GrizzlyBears(), new LlanowarElves()));
        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.setExile(player2, List.of(new Forest(), new Shock(), new GrizzlyBears()));

        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
    }

    private Permanent addReadyKaya(int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new KayaOrzhovUsurper());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
