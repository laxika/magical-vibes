package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AjaniGoldmane;
import com.github.laxika.magicalvibes.cards.t.Tarfire;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KithkinHealer.class, KithkinGreatheart.class, Tarfire.class, AjaniGoldmane.class})
class KithkinHealerTest extends BaseCardTest {

    private void addHealerReady() {
        addCreatureReady(player1, new KithkinHealer());
    }

    @Test
    @DisplayName("Adds 1 prevention shield to target creature")
    void preventsOnCreature() {
        addHealerReady();
        harness.addToBattlefield(player2, new KithkinGreatheart());

        UUID targetId = harness.getPermanentId(player2, "Kithkin Greatheart");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent greatheart = findPermanent(player2, "Kithkin Greatheart");
        assertThat(greatheart.getDamagePreventionShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Adds 1 prevention shield to target player")
    void preventsOnPlayer() {
        addHealerReady();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness")
    void respectsSummoningSickness() {
        harness.addToBattlefield(player1, new KithkinHealer());
        harness.addToBattlefield(player2, new KithkinGreatheart());

        UUID targetId = harness.getPermanentId(player2, "Kithkin Greatheart");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenTapped() {
        addHealerReady();
        harness.addToBattlefield(player2, new KithkinGreatheart());

        Permanent healer = findPermanent(player1, "Kithkin Healer");
        healer.tap();

        UUID targetId = harness.getPermanentId(player2, "Kithkin Greatheart");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Prevents only the next 1 damage to a targeted player")
    void preventsOnlyNextDamageToPlayer() {
        addHealerReady();
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Tarfire(), new Tarfire()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("The prevention shield expires at the end of the turn")
    void preventionExpiresAtEndOfTurn() {
        addHealerReady();
        Permanent target = addCreatureReady(player2, new KithkinGreatheart());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.getDamagePreventionShield()).isEqualTo(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(target.getDamagePreventionShield()).isZero();
    }

    @Test
    @DisplayName("Prevents one damage to itself and pays the tap cost")
    void preventsDamageToItself() {
        addHealerReady();
        Permanent healer = findPermanent(player1, "Kithkin Healer");
        harness.setHand(player1, List.of(new Tarfire()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, healer.getId());
        assertThat(healer.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, healer.getId());

        harness.assertOnBattlefield(player1, "Kithkin Healer");
        assertThat(healer.getDamagePreventionShield()).isZero();
    }

    @Test
    @DisplayName("Two healers prevent two damage to a creature, then their shields are consumed")
    void overlappingShieldsPreventCreatureDamage() {
        addHealerReady();
        addHealerReady();
        Permanent target = addCreatureReady(player2, new KithkinGreatheart());
        harness.setHand(player1, List.of(new Tarfire(), new Tarfire()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Kithkin Greatheart");

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Kithkin Greatheart");
        harness.assertInGraveyard(player2, "Kithkin Greatheart");
    }

    @Test
    @DisplayName("Unused player prevention expires before damage on the next turn")
    void playerPreventionExpiresAtEndOfTurn() {
        addHealerReady();
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new Tarfire()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player2.getId());

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Prevents one damage to a targeted planeswalker")
    void preventsDamageToPlaneswalker() {
        addHealerReady();
        Permanent ajani = harness.addToBattlefieldAndReturn(player2, new AjaniGoldmane());
        ajani.setCounterCount(CounterType.LOYALTY, 4);
        harness.setHand(player1, List.of(new Tarfire()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, ajani.getId());
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, ajani.getId());

        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }
}
