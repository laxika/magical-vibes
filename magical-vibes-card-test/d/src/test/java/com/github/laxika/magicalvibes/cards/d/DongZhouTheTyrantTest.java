package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.ChildOfNight;
import com.github.laxika.magicalvibes.cards.s.ShuDefender;
import com.github.laxika.magicalvibes.cards.w.WallOfVines;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DongZhouTheTyrant.class, ShuDefender.class, WallOfVines.class, ChildOfNight.class})
class DongZhouTheTyrantTest extends BaseCardTest {

    @Test
    @DisplayName("ETB makes an opponent's creature deal its power to that opponent")
    void opponentTakesPowerDamage() {
        harness.addToBattlefield(player2, new ShuDefender());
        harness.setHand(player1, List.of(new DongZhouTheTyrant()));
        harness.addMana(player1, ManaColor.RED, 5);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        UUID targetId = harness.getPermanentId(player2, "Shu Defender");
        harness.castCreature(player1, 0, targetId);
        resolveAllTriggers();

        // Shu Defender is 2/2 -> opponent loses 2, creature is unharmed (damage went to the player).
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
        assertThat(findPermanent(player2, "Shu Defender").getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("A 0-power creature deals no damage to its controller")
    void zeroPowerDealsNoDamage() {
        harness.addToBattlefield(player2, new WallOfVines());
        harness.setHand(player1, List.of(new DongZhouTheTyrant()));
        harness.addMana(player1, ManaColor.RED, 5);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        UUID targetId = harness.getPermanentId(player2, "Wall of Vines");
        harness.castCreature(player1, 0, targetId);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Does nothing if the targeted creature leaves before the ability resolves")
    void doesNothingIfTargetLeavesBeforeResolution() {
        var target = harness.addToBattlefieldAndReturn(player2, new ShuDefender());
        harness.setHand(player1, List.of(new DongZhouTheTyrant()));
        harness.addMana(player1, ManaColor.RED, 5);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();

        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Cannot target your own creature")
    void cannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new ShuDefender());
        harness.setHand(player1, List.of(new DongZhouTheTyrant()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID ownDefender = harness.getPermanentId(player1, "Shu Defender");

        assertThatThrownBy(() -> harness.castCreature(player1, 0, ownDefender))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent controls");
    }

    @Test
    @DisplayName("Damage uses the targeted creature's power at resolution")
    void usesPowerAtResolution() {
        var target = harness.addToBattlefieldAndReturn(player2, new ShuDefender());
        harness.setHand(player1, List.of(new DongZhouTheTyrant()));
        harness.addMana(player1, ManaColor.RED, 5);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        resolveAllTriggers();

        harness.assertLife(player2, lifeBefore - 5);
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The ability does not resolve if its target changes to your control")
    void targetBecomesIllegalWhenYouGainControl() {
        var target = harness.addToBattlefieldAndReturn(player2, new ShuDefender());
        harness.setHand(player1, List.of(new DongZhouTheTyrant()));
        harness.addMana(player1, ManaColor.RED, 5);
        int yourLifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        resolveAllTriggers();

        harness.assertLife(player1, yourLifeBefore);
        harness.assertLife(player2, opponentLifeBefore);
    }

    @Test
    @DisplayName("The trigger resolves even if Dong Zhou leaves the battlefield")
    void triggerResolvesWithoutDongZhou() {
        var target = harness.addToBattlefieldAndReturn(player2, new ShuDefender());
        harness.setHand(player1, List.of(new DongZhouTheTyrant()));
        harness.addMana(player1, ManaColor.RED, 5);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        var dongZhou = findPermanent(player1, "Dong Zhou, the Tyrant");
        gd.playerBattlefields.get(player1.getId()).remove(dongZhou);
        gd.playerGraveyards.get(player1.getId()).add(dongZhou.getCard());
        resolveAllTriggers();

        harness.assertLife(player2, lifeBefore - 2);
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The targeted creature's lifelink gains life for its controller")
    void targetedCreatureIsTheDamageSource() {
        var target = harness.addToBattlefieldAndReturn(player2, new ChildOfNight());
        harness.setLife(player2, 10);
        harness.setHand(player1, List.of(new DongZhouTheTyrant()));
        harness.addMana(player1, ManaColor.RED, 5);
        int yourLifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 10);
        harness.assertLife(player1, yourLifeBefore);
        assertThat(target.getMarkedDamage()).isZero();
    }
}
