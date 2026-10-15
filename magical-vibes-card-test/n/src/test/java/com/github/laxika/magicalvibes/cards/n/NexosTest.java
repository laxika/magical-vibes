package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Fireball;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.Wasteland;
import com.github.laxika.magicalvibes.cards.t.TyrantGuard;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Nexos.class, Forest.class, Wasteland.class, Fireball.class, GrizzlyBears.class, TyrantGuard.class})
class NexosTest extends BaseCardTest {

    @Test
    void basicLandGainsTwoXCostOnlyColorlessManaAbility() {
        harness.addToBattlefield(player1, new Nexos());
        harness.addToBattlefield(player1, new Forest());

        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getXCostOnlyColorless()).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void xCostOnlyManaPaysXSpell() {
        harness.addToBattlefield(player1, new Nexos());
        harness.addToBattlefield(player1, new Forest());
        harness.activateAbility(player1, 1, 0, null, null);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Fireball()));

        harness.castSorcery(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerManaPools.get(player1.getId()).getXCostOnlyColorless()).isZero();
    }

    @Test
    void xCostOnlyManaCannotPayNonXSpell() {
        harness.addToBattlefield(player1, new Nexos());
        harness.addToBattlefield(player1, new Forest());
        harness.activateAbility(player1, 1, 0, null, null);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getXCostOnlyColorless()).isEqualTo(2);
    }

    @Test
    void nonBasicLandDoesNotGainNexosAbility() {
        harness.addToBattlefield(player1, new Nexos());
        harness.addToBattlefield(player1, new Wasteland());

        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getXCostOnlyColorless()).isZero();
    }

    @Test
    void restrictedManaPaysFixedGenericCostWhenXIsZero() {
        harness.addToBattlefield(player1, new Nexos());
        harness.addToBattlefield(player1, new Forest());
        harness.activateAbility(player1, 1, 0, null, null);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new TyrantGuard()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getXCostOnlyColorless()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof TyrantGuard);
    }

    @Test
    void opponentsBasicLandDoesNotGainRestrictedManaAbility() {
        harness.addToBattlefield(player1, new Nexos());
        harness.addToBattlefield(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player2.getId()).getXCostOnlyColorless()).isZero();
        harness.tapPermanent(player2, 0);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void basicLandRetainsItsNormalManaAbility() {
        harness.addToBattlefield(player1, new Nexos());
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getXCostOnlyColorless()).isZero();
    }

    @Test
    void multipleNexosDoNotMultiplyManaFromOneActivation() {
        harness.addToBattlefield(player1, new Nexos());
        harness.addToBattlefield(player1, new Nexos());
        harness.addToBattlefield(player1, new Forest());

        harness.activateAbility(player1, 2, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getXCostOnlyColorless()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1, 2, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getXCostOnlyColorless()).isEqualTo(2);
    }

    @Test
    void basicLandLosesGrantedAbilityWhenNexosLeavesBattlefield() {
        var nexos = harness.addToBattlefieldAndReturn(player1, new Nexos());
        harness.addToBattlefield(player1, new Forest());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player1, List.of(new Fireball()));

        harness.castSorcery(player1, 0, 2, nexos.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof Nexos);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.tapPermanent(player1, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getXCostOnlyColorless()).isZero();
    }
}
