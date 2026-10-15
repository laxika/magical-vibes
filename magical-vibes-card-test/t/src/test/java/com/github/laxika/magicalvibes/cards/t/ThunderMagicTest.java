package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AncientAdamantoise;
import com.github.laxika.magicalvibes.cards.h.HillGigas;
import com.github.laxika.magicalvibes.cards.i.IronGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.q.QutrubForayer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThunderMagic.class, QutrubForayer.class, HillGigas.class, IronGiant.class,
        Island.class, AncientAdamantoise.class})
class ThunderMagicTest extends BaseCardTest {

    @Test
    @DisplayName("Thunder deals 2 damage to the target creature")
    void thunderDealsTwoDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new QutrubForayer());

        cast(0, target, 1);

        harness.assertNotOnBattlefield(player2, "Qutrub Forayer");
    }

    @Test
    @DisplayName("Thundara deals 4 damage to the target creature")
    void thundaraDealsFourDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGigas());

        cast(1, target, 4);

        harness.assertNotOnBattlefield(player2, "Hill Gigas");
    }

    @Test
    @DisplayName("Thundaga deals 8 damage to the target creature and pays its red tiered cost")
    void thundagaDealsEightDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IronGiant());

        cast(2, target, 7);

        harness.assertNotOnBattlefield(player2, "Iron Giant");
    }

    @Test
    @DisplayName("Thunder Magic cannot target a land")
    void cannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new ThunderMagic()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @CsvSource({"0, 1, 2", "1, 4, 4"})
    void lowerTiersMarkExactlyTheirDamage(int mode, int totalMana, int damage) {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IronGiant());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new HillGigas());

        cast(mode, target, totalMana);

        harness.assertOnBattlefield(player2, "Iron Giant");
        assertThat(target.getMarkedDamage()).isEqualTo(damage);
        assertThat(other.getMarkedDamage()).isZero();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertInGraveyard(player1, "Thunder Magic");
    }

    @ParameterizedTest
    @CsvSource({"1, 1, 2", "2, 2, 4"})
    void higherTiersRequireTheirFullAdditionalGenericCost(int mode, int red, int colorless) {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new QutrubForayer());
        harness.setHand(player1, List.of(new ThunderMagic()));
        harness.addMana(player1, ManaColor.RED, red);
        harness.addMana(player1, ManaColor.COLORLESS, colorless);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, mode, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Thunder Magic");
        assertThat(gd.stack).isEmpty();
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    void thundagaMarksExactlyEightDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AncientAdamantoise());

        cast(2, target, 7);

        harness.assertOnBattlefield(player1, "Ancient Adamantoise");
        assertThat(target.getMarkedDamage()).isEqualTo(8);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertInGraveyard(player1, "Thunder Magic");
    }

    @Test
    void thundagaRequiresTwoRedMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IronGiant());
        harness.setHand(player1, List.of(new ThunderMagic()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Thunder Magic");
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void cannotTargetPlayerInAnyTier(int mode) {
        harness.setHand(player1, List.of(new ThunderMagic()));
        harness.addMana(player1, ManaColor.RED, 7);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, mode, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Thunder Magic");
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    void canTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new QutrubForayer());

        cast(0, target, 1);

        harness.assertNotOnBattlefield(player1, "Qutrub Forayer");
        harness.assertInGraveyard(player1, "Qutrub Forayer");
    }

    @Test
    void doesNotDamageAnotherCreatureWhenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new QutrubForayer());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new IronGiant());
        harness.setHand(player1, List.of(new ThunderMagic()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castInstant(player1, 0, 2, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Iron Giant");
        assertThat(other.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Thunder Magic");
        harness.assertInGraveyard(player2, "Qutrub Forayer");
        assertThat(gd.stack).isEmpty();
    }

    private void cast(int mode, Permanent target, int totalMana) {
        harness.setHand(player1, List.of(new ThunderMagic()));
        harness.addMana(player1, ManaColor.RED, mode == 2 ? 2 : 1);
        harness.addMana(player1, ManaColor.COLORLESS, totalMana - (mode == 2 ? 2 : 1));
        harness.castInstant(player1, 0, mode, target.getId());
        harness.passBothPriorities();
    }
}
