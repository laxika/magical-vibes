package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.e.ElspethKnightErrant;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.r.RhoxWarMonk;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SoulsFire.class, HillGiant.class, GrizzlyBears.class, GiantGrowth.class,
        Unsummon.class, ElspethKnightErrant.class, InvasionOfZendikar.class,
        Ornithopter.class, RhoxWarMonk.class})
class SoulsFireTest extends BaseCardTest {

    @Test
    @DisplayName("Chosen creature deals damage equal to its power to a target creature, killing it")
    void creatureKillsTargetCreature() {
        // Hill Giant (3/3) deals 3 damage to Grizzly Bears (2/2)
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SoulsFire()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID giantId = harness.getPermanentId(player1, "Hill Giant");
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, List.of(giantId, bearsId));

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Chosen creature deals damage equal to its power to a target player")
    void creatureDamagesTargetPlayer() {
        // Hill Giant (3/3) deals 3 damage to the opponent
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new SoulsFire()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID giantId = harness.getPermanentId(player1, "Hill Giant");
        harness.castAndResolveInstant(player1, 0, List.of(giantId, player2.getId()));

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Cannot choose an opponent's creature as the damage source")
    void cannotChooseOpponentCreatureAsSource() {
        harness.addToBattlefield(player1, new HillGiant()); // gives a legal source so the spell is castable
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SoulsFire()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID opponentGiantId = harness.getPermanentId(player2, "Hill Giant");
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(opponentGiantId, targetId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    void creatureCanDealDamageToItself() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new SoulsFire()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, List.of(giant.getId(), giant.getId()));

        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertNotOnBattlefield(player1, "Hill Giant");
    }

    @Test
    void creatureCanDamageItsController() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new SoulsFire()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, List.of(giant.getId(), player1.getId()));

        harness.assertLife(player1, 17);
    }

    @Test
    void usesPowerWhenTheSpellResolves() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new SoulsFire(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, List.of(giant.getId(), player2.getId()));
        harness.castAndResolveInstant(player1, 0, giant.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 14);
    }

    @Test
    void noDamageWhenSourceLeavesBeforeResolution() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new SoulsFire(), new Unsummon()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, List.of(giant.getId(), player2.getId()));
        harness.castAndResolveInstant(player1, 0, giant.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertInHand(player1, "Hill Giant");
        harness.assertInGraveyard(player1, "Soul's Fire");
    }

    @Test
    void noDamageWhenVictimLeavesBeforeResolution() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SoulsFire(), new Unsummon()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, List.of(giant.getId(), bears.getId()));
        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Hill Giant");
        assertThat(giant.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Soul's Fire");
    }

    @Test
    void creatureDamagesPlaneswalker() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent elspeth = harness.addToBattlefieldAndReturn(player2, new ElspethKnightErrant());
        elspeth.setCounterCount(CounterType.LOYALTY, 4);
        harness.setHand(player1, List.of(new SoulsFire()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, List.of(giant.getId(), elspeth.getId()));

        assertThat(elspeth.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Elspeth, Knight-Errant");
    }

    @Test
    void creatureCanDamageBattle() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfZendikar());
        battle.setCounterCount(CounterType.DEFENSE, 3);
        harness.setHand(player1, List.of(new SoulsFire()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, List.of(bears.getId(), battle.getId()));

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(1);
    }

    @Test
    void damageUsesTheCreaturesLifelink() {
        Permanent monk = harness.addToBattlefieldAndReturn(player1, new RhoxWarMonk());
        harness.setHand(player1, List.of(new SoulsFire()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, List.of(monk.getId(), player2.getId()));

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
    }

    @Test
    void zeroPowerCreatureDealsNoDamage() {
        Permanent ornithopter = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.setHand(player1, List.of(new SoulsFire()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, List.of(ornithopter.getId(), player2.getId()));

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Soul's Fire");
    }
}
