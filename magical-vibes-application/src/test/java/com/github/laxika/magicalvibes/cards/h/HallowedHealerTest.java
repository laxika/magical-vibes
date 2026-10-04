package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DuskImp;
import com.github.laxika.magicalvibes.cards.f.Firebolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HallowedHealer.class, DuskImp.class, Firebolt.class})
class HallowedHealerTest extends BaseCardTest {

    @Test
    @DisplayName("The basic ability prevents 2 damage to a target player")
    void preventsTwoDamageToPlayer() {
        addCreatureReady(player1, new HallowedHealer());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(2);
    }

    @Test
    @DisplayName("The threshold ability prevents 4 damage to a target creature")
    void thresholdPreventsFourDamageToCreature() {
        addCreatureReady(player1, new HallowedHealer());
        harness.setGraveyard(player1, List.of(
                new DuskImp(), new DuskImp(), new DuskImp(), new DuskImp(),
                new DuskImp(), new DuskImp(), new DuskImp()
        ));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DuskImp());
        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getDamagePreventionShield()).isEqualTo(4);
    }

    @Test
    @DisplayName("The threshold ability cannot be activated with fewer than seven graveyard cards")
    void thresholdRequiresSevenGraveyardCards() {
        addCreatureReady(player1, new HallowedHealer());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("seven or more cards");
    }

    @Test
    @DisplayName("The basic ability prevents only the next 2 damage to a target player")
    void basicAbilityPreventsOnlyNextTwoDamageToPlayer() {
        addCreatureReady(player1, new HallowedHealer());
        addCreatureReady(player1, new DuskImp());
        addCreatureReady(player1, new DuskImp());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        declareAttackers(player1, List.of(1, 2));
        resolveCombat();

        harness.assertLife(player2, 18);
        assertThat(gd.playerDamagePreventionShields).doesNotContainKey(player2.getId());
    }

    @Test
    @DisplayName("The threshold ability prevents combat damage to a target creature")
    void thresholdAbilityPreventsCombatDamageToCreature() {
        addCreatureReady(player1, new HallowedHealer());
        harness.setGraveyard(player1, List.of(
                new DuskImp(), new DuskImp(), new DuskImp(), new DuskImp(),
                new DuskImp(), new DuskImp(), new DuskImp()
        ));
        addCreatureReady(player1, new DuskImp());
        Permanent target = addCreatureReady(player2, new DuskImp());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(player1, List.of(1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(target.getDamagePreventionShield()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Threshold counts cards only in the activating player's graveyard")
    void thresholdRequiresCardsInActivatingPlayersGraveyard() {
        addCreatureReady(player1, new HallowedHealer());
        harness.setGraveyard(player2, List.of(
                new DuskImp(), new DuskImp(), new DuskImp(), new DuskImp(),
                new DuskImp(), new DuskImp(), new DuskImp()
        ));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("seven or more cards");
    }

    @Test
    @DisplayName("The ability cannot be activated while Hallowed Healer is tapped")
    void cannotActivateWhenTapped() {
        Permanent healer = addCreatureReady(player1, new HallowedHealer());
        healer.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    void thresholdCannotBeActivatedWithExactlySixCards() {
        Permanent healer = addCreatureReady(player1, new HallowedHealer());
        harness.setGraveyard(player1, List.of(
                new DuskImp(), new DuskImp(), new DuskImp(),
                new DuskImp(), new DuskImp(), new DuskImp()
        ));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("seven or more cards");
        assertThat(healer.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void thresholdStillResolvesAfterGraveyardFallsBelowSevenCards() {
        addCreatureReady(player1, new HallowedHealer());
        addCreatureReady(player1, new DuskImp());
        addCreatureReady(player1, new DuskImp());
        harness.setGraveyard(player1, List.of(
                new DuskImp(), new DuskImp(), new DuskImp(), new DuskImp(),
                new DuskImp(), new DuskImp(), new DuskImp()
        ));

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        declareAttackers(player1, List.of(1, 2));
        resolveCombat();

        harness.assertLife(player2, 20);
        assertThat(gd.playerDamagePreventionShields).doesNotContainKey(player2.getId());
    }

    @Test
    void basicAbilityStillPreventsOnlyTwoWithThreshold() {
        addCreatureReady(player1, new HallowedHealer());
        addCreatureReady(player1, new DuskImp());
        addCreatureReady(player1, new DuskImp());
        harness.setGraveyard(player1, List.of(
                new DuskImp(), new DuskImp(), new DuskImp(), new DuskImp(),
                new DuskImp(), new DuskImp(), new DuskImp()
        ));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        declareAttackers(player1, List.of(1, 2));
        resolveCombat();

        harness.assertLife(player2, 18);
    }

    @Test
    void neitherAbilityCanBeActivatedWithSummoningSickness() {
        harness.addToBattlefield(player1, new HallowedHealer());
        harness.setGraveyard(player1, List.of(
                new DuskImp(), new DuskImp(), new DuskImp(), new DuskImp(),
                new DuskImp(), new DuskImp(), new DuskImp()
        ));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    void basicAbilityPreventsNoncombatDamageToItself() {
        Permanent healer = addCreatureReady(player1, new HallowedHealer());
        harness.setHand(player1, List.of(new Firebolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, healer.getId());
        harness.passBothPriorities();
        harness.castAndResolveSorcery(player1, 0, healer.getId());

        harness.assertOnBattlefield(player1, "Hallowed Healer");
        assertThat(healer.getMarkedDamage()).isZero();
        assertThat(healer.getDamagePreventionShield()).isZero();
    }
}
