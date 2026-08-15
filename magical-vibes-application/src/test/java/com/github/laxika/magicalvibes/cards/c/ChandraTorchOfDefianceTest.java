package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChandraTorchOfDefianceTest extends BaseCardTest {

    @Test
    @DisplayName("First +1 deals 2 damage when the exiled card is a land")
    void firstPlusOneDealsDamageForLand() {
        Permanent chandra = addReadyChandra(player1, 4);
        Card land = new Plains();
        gd.playerDecks.get(player1.getId()).addFirst(land);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(land);
    }

    @Test
    @DisplayName("First +1 casts a nonland exiled card for its normal cost")
    void firstPlusOneCastsExiledCard() {
        Permanent chandra = addReadyChandra(player1, 4);
        Card shock = new Shock();
        gd.playerDecks.get(player1.getId()).addFirst(shock);
        harness.addMana(player1, ManaColor.RED, 1);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(shock);
    }

    @Test
    @DisplayName("Second +1 adds two red mana")
    void secondPlusOneAddsTwoRedMana() {
        Permanent chandra = addReadyChandra(player1, 4);
        int manaBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.RED);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(manaBefore + 2);
    }

    @Test
    @DisplayName("Minus three deals four damage to a target creature")
    void minusThreeDamagesCreature() {
        Permanent chandra = addReadyChandra(player1, 4);
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bear = findPermanent(player2, "Grizzly Bears");

        harness.activateAbility(player1, 0, 2, null, bear.getId());
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(bear.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Minus seven creates an emblem that deals five damage on a spell cast")
    void minusSevenCreatesSpellCastDamageEmblem() {
        Permanent chandra = addReadyChandra(player1, 7);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, 3, null, null);
        harness.passBothPriorities();
        assertThat(gd.emblems).hasSize(1);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 7);
    }

    private Permanent addReadyChandra(Player player, int loyalty) {
        Permanent permanent = new Permanent(new ChandraTorchOfDefiance());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}
