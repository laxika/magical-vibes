package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AlertShuInfantry;
import com.github.laxika.magicalvibes.cards.a.AwakenedSkyclave;
import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FireAmbush.class, AlertShuInfantry.class, ChandraNalaar.class,
        InvasionOfZendikar.class, AwakenedSkyclave.class})
class FireAmbushTest extends BaseCardTest {

    @Test
    @DisplayName("Fire Ambush deals 3 damage to target player")
    void deals3DamageToPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new FireAmbush()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Fire Ambush deals 3 damage to target creature, destroying a 2/2")
    void deals3DamageToCreatureDestroysIt() {
        harness.addToBattlefield(player2, new AlertShuInfantry());
        harness.setHand(player1, List.of(new FireAmbush()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Alert Shu Infantry");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Alert Shu Infantry");
        harness.assertInGraveyard(player2, "Alert Shu Infantry");
    }

    @Test
    @CardUsed(ChandraNalaar.class)
    @DisplayName("Fire Ambush deals 3 damage to target planeswalker")
    void deals3DamageToPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 6);
        harness.setHand(player1, List.of(new FireAmbush()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("Fire Ambush goes to graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        harness.setHand(player1, List.of(new FireAmbush()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Fire Ambush");
    }

    @Test
    @CardUsed({InvasionOfZendikar.class, AwakenedSkyclave.class})
    @DisplayName("Fire Ambush removes three defense counters from a battle")
    void deals3DamageToBattle() {
        Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfZendikar());
        battle.setCounterCount(CounterType.DEFENSE, 5);
        harness.setHand(player1, List.of(new FireAmbush()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, battle.getId());

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Invasion of Zendikar");
    }

    @Test
    @DisplayName("Fire Ambush can target its controller")
    void canDamageItsController() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new FireAmbush()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Fire Ambush does not deal damage when its only target leaves")
    void doesNotDealDamageWhenTargetLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlertShuInfantry());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new AlertShuInfantry());
        harness.setHand(player1, List.of(new FireAmbush()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, target.getId());
        GameData gd = harness.getGameData();
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());

        harness.passBothPriorities();

        assertThat(other.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Fire Ambush");
    }
}
