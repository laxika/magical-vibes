package com.github.laxika.magicalvibes.cards.q;
import java.util.UUID;

import com.github.laxika.magicalvibes.cards.a.AjaniVengeant;
import com.github.laxika.magicalvibes.cards.d.DruidOfTheAnima;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SphereOfResistance;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QasaliAmbusher.class, Forest.class, Plains.class, DruidOfTheAnima.class})
class QasaliAmbusherTest extends BaseCardTest {

    private Permanent attackController(com.github.laxika.magicalvibes.model.Player attacker,
                                       com.github.laxika.magicalvibes.model.Player defender) {
        Permanent creature = harness.addToBattlefieldAndReturn(attacker, new DruidOfTheAnima());
        creature.setAttacking(true);
        creature.setAttackTarget(defender.getId());
        return creature;
    }

    @Test
    @DisplayName("Casts for free with flash while a creature attacks you and you control a Forest and a Plains")
    void freeFlashCastWhenConditionMet() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Plains());
        attackController(player2, player1);
        harness.setHand(player1, List.of(new QasaliAmbusher()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.castCreatureWithEvoke(player1, 0, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Qasali Ambusher");
        // No mana was spent — it was cast without paying its mana cost.
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot free-cast when no creature is attacking you")
    void rejectedWhenNotAttacked() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Plains());
        harness.setHand(player1, List.of(new QasaliAmbusher()));

        assertThatThrownBy(() -> harness.castCreatureWithEvoke(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
        // The rejected cast rewinds — the card stays in hand.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Qasali Ambusher");
    }

    @Test
    @DisplayName("Cannot free-cast without both a Forest and a Plains")
    void rejectedWithoutRequiredLands() {
        harness.addToBattlefield(player1, new Forest());
        attackController(player2, player1);
        harness.setHand(player1, List.of(new QasaliAmbusher()));

        assertThatThrownBy(() -> harness.castCreatureWithEvoke(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Qasali Ambusher");
    }

    @Test
    void rejectedWithoutForest() {
        harness.addToBattlefield(player1, new Plains());
        attackController(player2, player1);
        harness.setHand(player1, List.of(new QasaliAmbusher()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, (UUID) null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Qasali Ambusher");
    }

    @Test
    void opponentsLandsDoNotSatisfyCondition() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Plains());
        attackController(player2, player1);
        harness.setHand(player1, List.of(new QasaliAmbusher()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, (UUID) null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Qasali Ambusher");
    }

    @Test
    void attackingOpponentDoesNotEnableFreeCast() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Plains());
        attackController(player1, player2);
        harness.setHand(player1, List.of(new QasaliAmbusher()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, (UUID) null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Qasali Ambusher");
    }

    @Test
    @CardUsed({AjaniVengeant.class})
    void attackingYourPlaneswalkerDoesNotEnableFreeCast() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Plains());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new AjaniVengeant());
        attackController(player2, player1).setAttackTarget(planeswalker.getId());
        harness.setHand(player1, List.of(new QasaliAmbusher()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, (UUID) null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Qasali Ambusher");
    }

    @Test
    void tappedLandsStillEnableFreeCast() {
        harness.addToBattlefieldAndReturn(player1, new Forest()).tap();
        harness.addToBattlefieldAndReturn(player1, new Plains()).tap();
        attackController(player2, player1);
        harness.setHand(player1, List.of(new QasaliAmbusher()));

        harness.castWithAlternateCost(player1, 0, (UUID) null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Qasali Ambusher");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void normalManaCostDoesNotRequireAlternateCostCondition() {
        harness.castFromHand(player1, new QasaliAmbusher(), "{1}{G}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Qasali Ambusher");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @CardUsed({SphereOfResistance.class})
    void freeCastStillRequiresManaForCostIncrease() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player2, new SphereOfResistance());
        attackController(player2, player1);
        harness.setHand(player1, List.of(new QasaliAmbusher()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, (UUID) null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Qasali Ambusher");
    }

    @Test
    @CardUsed({SphereOfResistance.class})
    void freeCastPaysCostIncrease() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player2, new SphereOfResistance());
        attackController(player2, player1);
        harness.setHand(player1, List.of(new QasaliAmbusher()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, (UUID) null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Qasali Ambusher");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
