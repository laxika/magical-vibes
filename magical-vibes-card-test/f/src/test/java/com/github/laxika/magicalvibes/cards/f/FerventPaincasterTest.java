package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.m.MaraudingBoneslasher;
import com.github.laxika.magicalvibes.cards.n.NicolBolasGodPharaoh;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FerventPaincaster.class, MaraudingBoneslasher.class, NicolBolasGodPharaoh.class})
class FerventPaincasterTest extends BaseCardTest {


    @Test
    @DisplayName("First ability deals 1 damage to target player and does not exert")
    void firstAbilityDealsDamageToPlayer() {
        harness.setLife(player2, 20);
        Permanent paincaster = addReadyPaincaster(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(paincaster.isTapped()).isTrue();
        assertThat(paincaster.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("First ability can't target a creature")
    void firstAbilityCannotTargetCreature() {
        addReadyPaincaster(player1);
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new MaraudingBoneslasher()).getId();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Second ability deals 1 damage to target creature, destroying a creature with 1 toughness")
    void secondAbilityDealsDamageToCreature() {
        addReadyPaincaster(player1);
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new FerventPaincaster()).getId();

        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Fervent Paincaster");
        harness.assertInGraveyard(player2, "Fervent Paincaster");
    }

    @Test
    @DisplayName("Exerting keeps the creature from untapping next untap step")
    void secondAbilityExertsSelf() {
        Permanent paincaster = addReadyPaincaster(player1);
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new MaraudingBoneslasher()).getId();

        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        assertThat(paincaster.isTapped()).isTrue();
        assertThat(paincaster.getSkipUntapCount()).isEqualTo(1);

        harness.performUntapStep(player1);
        assertThat(paincaster.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(paincaster.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Second ability can't target a player")
    void secondAbilityCannotTargetPlayer() {
        addReadyPaincaster(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void firstAbilityDamagesPlaneswalkerWithoutExerting() {
        Permanent paincaster = addReadyPaincaster(player1);
        Permanent bolas = harness.addToBattlefieldAndReturn(player2, new NicolBolasGodPharaoh());
        bolas.setCounterCount(CounterType.LOYALTY, 7);

        harness.activateAbility(player1, 0, null, bolas.getId());
        harness.passBothPriorities();

        assertThat(bolas.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        harness.performUntapStep(player1);
        assertThat(paincaster.isTapped()).isFalse();
    }

    @Test
    void exertIsPaidBeforeAbilityResolves() {
        Permanent paincaster = addReadyPaincaster(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MaraudingBoneslasher());

        harness.activateAbility(player1, 0, 1, null, target.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(paincaster.isTapped()).isTrue();
        assertThat(paincaster.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    void invalidTargetDoesNotRefundExertCost() {
        Permanent paincaster = addReadyPaincaster(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FerventPaincaster());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        target.setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.assertNotOnBattlefield(player2, "Fervent Paincaster");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.performUntapStep(player1);
        assertThat(paincaster.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(paincaster.isTapped()).isFalse();
    }

    @Test
    void exertDoesNotPreventUntappingForANewController() {
        Permanent paincaster = addReadyPaincaster(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MaraudingBoneslasher());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(paincaster);
        gd.playerBattlefields.get(player2.getId()).add(paincaster);

        harness.performUntapStep(player2);
        assertThat(paincaster.isTapped()).isFalse();
    }

    @Test
    void neitherAbilityCanBeActivatedWithSummoningSickness() {
        harness.addToBattlefield(player1, new FerventPaincaster());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MaraudingBoneslasher());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyPaincaster(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new FerventPaincaster());
        perm.setSummoningSick(false);
        return perm;
    }
}
