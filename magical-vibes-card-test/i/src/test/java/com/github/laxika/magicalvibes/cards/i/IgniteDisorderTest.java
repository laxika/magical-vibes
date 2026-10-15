package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.z.ZephyrSprite;
import com.github.laxika.magicalvibes.cards.d.Deathlace;
import com.github.laxika.magicalvibes.cards.l.LightningElemental;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IgniteDisorder.class, SoulWarden.class, EliteVanguard.class, ZephyrSprite.class,
        LightningElemental.class, WindDrake.class, Deathlace.class})
class IgniteDisorderTest extends BaseCardTest {

    @Test
    void deals3DamageToSingleWhiteCreature() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new IgniteDisorder()));
        harness.addMana(player1, ManaColor.RED, 2);

        Permanent target = harness.addToBattlefieldAndReturn(player2, new SoulWarden());

        harness.castInstant(player1, 0, Map.of(target.getId(), 3));
        harness.passBothPriorities();

        // SoulWarden is 1/1, 3 damage kills it
        harness.assertNotOnBattlefield(player2, target.getCard().getName());
        harness.assertInGraveyard(player2, "Soul Warden");
    }

    @Test
    void divides2And1DamageAmongTwoWhiteCreatures() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new IgniteDisorder()));
        harness.addMana(player1, ManaColor.RED, 2);

        Permanent target1 = harness.addToBattlefieldAndReturn(player2, new SoulWarden());
        Permanent target2 = harness.addToBattlefieldAndReturn(player2, new EliteVanguard());

        harness.castInstant(player1, 0, Map.of(target1.getId(), 2, target2.getId(), 1));
        harness.passBothPriorities();

        // Both have 1 toughness, both die
        harness.assertNotOnBattlefield(player2, target1.getCard().getName());
        harness.assertNotOnBattlefield(player2, target2.getCard().getName());
    }

    @Test
    void divides1DamageAmongThreeCreatures() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new IgniteDisorder()));
        harness.addMana(player1, ManaColor.RED, 2);

        Permanent white1 = harness.addToBattlefieldAndReturn(player2, new SoulWarden());
        Permanent white2 = harness.addToBattlefieldAndReturn(player2, new EliteVanguard());
        Permanent blue1 = harness.addToBattlefieldAndReturn(player2, new ZephyrSprite());

        harness.castInstant(player1, 0, Map.of(
                white1.getId(), 1,
                white2.getId(), 1,
                blue1.getId(), 1
        ));
        harness.passBothPriorities();

        // All three have 1 toughness, all die
        harness.assertNotOnBattlefield(player2, white1.getCard().getName());
        harness.assertNotOnBattlefield(player2, white2.getCard().getName());
        harness.assertNotOnBattlefield(player2, blue1.getCard().getName());
    }

    @Test
    void canTargetBlueCreature() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new IgniteDisorder()));
        harness.addMana(player1, ManaColor.RED, 2);

        Permanent blue = harness.addToBattlefieldAndReturn(player2, new ZephyrSprite());

        harness.castInstant(player1, 0, Map.of(blue.getId(), 3));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, blue.getCard().getName());
    }

    @Test
    void skipsTargetThatGainsHexproofBeforeResolution() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new IgniteDisorder()));
        harness.addMana(player1, ManaColor.RED, 2);

        Permanent protectedTarget = harness.addToBattlefieldAndReturn(player2, new EliteVanguard());
        Permanent legalTarget = harness.addToBattlefieldAndReturn(player2, new SoulWarden());

        harness.castInstant(player1, 0, Map.of(protectedTarget.getId(), 2, legalTarget.getId(), 1));
        protectedTarget.getGrantedKeywords().add(Keyword.HEXPROOF);
        harness.passBothPriorities();

        assertThat(protectedTarget.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, protectedTarget.getCard().getName());
        harness.assertNotOnBattlefield(player2, legalTarget.getCard().getName());
    }

    @Test
    void cannotTargetRedCreature() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new IgniteDisorder()));
        harness.addMana(player1, ManaColor.RED, 2);

        Permanent red = harness.addToBattlefieldAndReturn(player2, new LightningElemental());

        assertThatThrownBy(() ->
                harness.castInstant(player1, 0, Map.of(red.getId(), 3))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void damageAssignmentsMustSumTo3() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new IgniteDisorder()));
        harness.addMana(player1, ManaColor.RED, 2);

        Permanent target = harness.addToBattlefieldAndReturn(player2, new SoulWarden());

        // Only assigning 2 damage — should fail
        assertThatThrownBy(() ->
                harness.castInstant(player1, 0, Map.of(target.getId(), 2))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotRedistributeDamageFromIllegalTarget() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new IgniteDisorder()));
        harness.addMana(player1, ManaColor.RED, 2);
        Permanent protectedTarget = harness.addToBattlefieldAndReturn(player2, new EliteVanguard());
        Permanent remainingTarget = harness.addToBattlefieldAndReturn(player2, new WindDrake());

        harness.castInstant(player1, 0, Map.of(protectedTarget.getId(), 2, remainingTarget.getId(), 1));
        protectedTarget.getGrantedKeywords().add(Keyword.HEXPROOF);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Elite Vanguard");
        harness.assertOnBattlefield(player2, "Wind Drake");
        assertThat(protectedTarget.getMarkedDamage()).isZero();
        assertThat(remainingTarget.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void canTargetControllersCreature() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new IgniteDisorder()));
        harness.addMana(player1, ManaColor.RED, 2);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SoulWarden());

        harness.castInstant(player1, 0, Map.of(target.getId(), 3));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Soul Warden");
        harness.assertInGraveyard(player1, "Soul Warden");
    }

    @Test
    void cannotAssignZeroDamageToATarget() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new IgniteDisorder()));
        harness.addMana(player1, ManaColor.RED, 2);
        Permanent first = harness.addToBattlefieldAndReturn(player2, new SoulWarden());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new EliteVanguard());

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                Map.of(first.getId(), 3, second.getId(), 0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotCastWithoutTargets() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new IgniteDisorder()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, Map.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetPlayer() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new IgniteDisorder()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, Map.of(player2.getId(), 3)))
                .isInstanceOf(IllegalStateException.class);
    }
    @Test
    void skipsCreatureThatBecomesBlackBeforeResolution() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new IgniteDisorder(), new Deathlace()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        Permanent changedTarget = harness.addToBattlefieldAndReturn(player2, new EliteVanguard());
        Permanent legalTarget = harness.addToBattlefieldAndReturn(player2, new WindDrake());

        harness.castInstant(player1, 0, Map.of(changedTarget.getId(), 2, legalTarget.getId(), 1));
        harness.castAndResolveInstant(player1, 0, changedTarget.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Elite Vanguard");
        assertThat(changedTarget.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Wind Drake");
        assertThat(legalTarget.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void dealsNoDamageWhenOnlyTargetBecomesBlackBeforeResolution() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new IgniteDisorder(), new Deathlace()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WindDrake());

        harness.castInstant(player1, 0, Map.of(target.getId(), 3));
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Wind Drake");
        assertThat(target.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Ignite Disorder");
    }
}
