package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.r.RalZarek;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PunishTheEnemy.class, SerraAngel.class, RalZarek.class, PyrewildShaman.class})
class PunishTheEnemyTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 to the targeted player and 3 to the targeted creature")
    void damagesPlayerAndCreature() {
        Permanent serra = harness.addToBattlefieldAndReturn(player2, new SerraAngel()); // 4/4
        UUID serraId = serra.getId();
        harness.setHand(player1, List.of(new PunishTheEnemy()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, List.of(player2.getId(), serraId));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(serra.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Can target its own controller")
    void canTargetController() {
        Permanent serra = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        harness.setHand(player1, List.of(new PunishTheEnemy()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setLife(player1, 20);

        harness.castAndResolveInstant(player1, 0, List.of(player1.getId(), serra.getId()));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(serra.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Rejects a player as the creature target")
    void rejectsPlayerAsCreatureTarget() {
        harness.setHand(player1, List.of(new PunishTheEnemy()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(player2.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects a creature as the player-or-planeswalker target")
    void rejectsCreatureAsPlayerTarget() {
        Permanent serra = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        harness.setHand(player1, List.of(new PunishTheEnemy()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(serra.getId(), serra.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void damagesPlaneswalkerAndKillsCreature() {
        Permanent ral = harness.addToBattlefieldAndReturn(player2, new RalZarek());
        ral.setCounterCount(CounterType.LOYALTY, 4);
        Permanent shaman = harness.addToBattlefieldAndReturn(player2, new PyrewildShaman());
        harness.setHand(player1, List.of(new PunishTheEnemy()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveInstant(player1, 0, List.of(ral.getId(), shaman.getId()));

        assertThat(ral.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Ral Zarek");
        harness.assertInGraveyard(player2, "Pyrewild Shaman");
        harness.assertNotOnBattlefield(player2, "Pyrewild Shaman");
        harness.assertLife(player2, 20);
    }

    @Test
    void damagesPlayerWhenCreatureTargetLeavesBattlefield() {
        Permanent shaman = harness.addToBattlefieldAndReturn(player2, new PyrewildShaman());
        harness.setHand(player1, List.of(new PunishTheEnemy()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castInstant(player1, 0, List.of(player2.getId(), shaman.getId()));

        gd.playerBattlefields.get(player2.getId()).remove(shaman);
        harness.setGraveyard(player2, List.of(shaman.getCard()));
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player1, "Punish the Enemy");
    }

    @Test
    void damagesCreatureWhenPlaneswalkerTargetLeavesBattlefield() {
        Permanent ral = harness.addToBattlefieldAndReturn(player2, new RalZarek());
        ral.setCounterCount(CounterType.LOYALTY, 4);
        Permanent shaman = harness.addToBattlefieldAndReturn(player2, new PyrewildShaman());
        harness.setHand(player1, List.of(new PunishTheEnemy()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castInstant(player1, 0, List.of(ral.getId(), shaman.getId()));

        gd.playerBattlefields.get(player2.getId()).remove(ral);
        harness.setGraveyard(player2, List.of(ral.getCard()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Pyrewild Shaman");
        harness.assertNotOnBattlefield(player2, "Pyrewild Shaman");
        harness.assertLife(player2, 20);
    }

    @Test
    void doesNotResolveWhenBothTargetsLeaveBattlefield() {
        Permanent ral = harness.addToBattlefieldAndReturn(player2, new RalZarek());
        ral.setCounterCount(CounterType.LOYALTY, 4);
        Permanent shaman = harness.addToBattlefieldAndReturn(player2, new PyrewildShaman());
        harness.setHand(player1, List.of(new PunishTheEnemy()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castInstant(player1, 0, List.of(ral.getId(), shaman.getId()));

        gd.playerBattlefields.get(player2.getId()).remove(ral);
        gd.playerBattlefields.get(player2.getId()).remove(shaman);
        harness.setGraveyard(player2, List.of(ral.getCard(), shaman.getCard()));
        harness.passBothPriorities();

        assertThat(ral.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(shaman.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Punish the Enemy");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void requiresCreatureTargetInAdditionToPlayerTarget() {
        harness.setHand(player1, List.of(new PunishTheEnemy()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
