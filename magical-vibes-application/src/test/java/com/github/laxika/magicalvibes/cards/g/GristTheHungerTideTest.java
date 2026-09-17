package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AntQueen;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GristTheHungerTide.class, GrizzlyBears.class, AntQueen.class})
class GristTheHungerTideTest extends BaseCardTest {

    @Test
    @DisplayName("Grist is a 1/1 Insect creature outside the battlefield only")
    void becomesCreatureOutsideBattlefield() {
        Card outside = new GristTheHungerTide();
        harness.setHand(player1, List.of(outside));

        assertThat(gqs.cardHasType(outside, CardType.CREATURE, gd, player1.getId())).isTrue();
        assertThat(gqs.cardHasSubtype(outside, CardSubtype.INSECT, gd, player1.getId())).isTrue();

        Permanent onBattlefield = addReadyGrist(player1, 3);
        assertThat(gqs.isCreature(gd, onBattlefield)).isFalse();
        assertThat(gqs.cardHasSubtype(onBattlefield.getCard(), CardSubtype.INSECT, gd, player1.getId()))
                .isFalse();
    }

    @Test
    @DisplayName("+1 creates an Insect, mills, and repeats after milling an Insect")
    void plusOneRepeatsAfterMillingInsect() {
        Permanent grist = addReadyGrist(player1, 3);
        harness.setLibrary(player1, List.of(new AntQueen(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(
                permanent -> permanent.getCard().isToken()).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Ant Queen", "Grizzly Bears");
        assertThat(grist.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("-2 sacrifices a creature and destroys a target creature")
    void minusTwoSacrificesAndDestroys() {
        Permanent grist = addReadyGrist(player1, 3);
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(p -> p.getCard().getName())
                .containsExactly("Grist, the Hunger Tide");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(grist.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("-5 makes each opponent lose life for creature cards in the controller's graveyard")
    void minusFiveCountsCreatureCardsInControllerGraveyard() {
        Permanent grist = addReadyGrist(player1, 5);
        gd.playerGraveyards.get(player1.getId()).addAll(List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(grist.getCounterCount(CounterType.LOYALTY)).isZero();
    }

    private Permanent addReadyGrist(Player player, int loyalty) {
        Permanent permanent = new Permanent(new GristTheHungerTide());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}
