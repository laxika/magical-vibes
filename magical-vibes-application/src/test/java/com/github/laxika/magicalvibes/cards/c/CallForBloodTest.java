package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BileUrchin;
import com.github.laxika.magicalvibes.cards.f.FrostOgre;
import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.cards.g.GoblinCohort;
import com.github.laxika.magicalvibes.cards.t.TendoIceBridge;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({CallForBlood.class, BileUrchin.class, FrostOgre.class, GnarledMass.class,
        GoblinCohort.class, TendoIceBridge.class})
class CallForBloodTest extends BaseCardTest {

    private void prepare() {
        harness.setHand(player1, List.of(new CallForBlood()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    @Test
    @DisplayName("Sacrificing a 2-power creature gives the target -2/-2")
    void sacrificeTwoPowerCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GoblinCohort()); // 2/2
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GnarledMass()); // 3/3

        prepare();
        harness.castInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Goblin Cohort");
        assertThat(target.getPowerModifier()).isEqualTo(-2);
        assertThat(target.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    @DisplayName("Only the targeted creature is weakened")
    void onlyTargetIsWeakened() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new BileUrchin()); // 1/1
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GnarledMass()); // 3/3
        Permanent bystander = harness.addToBattlefieldAndReturn(player2, new FrostOgre()); // 5/3

        prepare();
        harness.castInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(-1);
        assertThat(target.getToughnessModifier()).isEqualTo(-1);
        assertThat(bystander.getPowerModifier()).isZero();
        assertThat(bystander.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Toughness dropped to 0 destroys the target")
    void lethalDebuffDestroysTarget() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new FrostOgre()); // 5/3
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoblinCohort()); // 2/2

        prepare();
        harness.castInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Goblin Cohort");
        harness.assertInGraveyard(player2, "Goblin Cohort");
    }

    @Test
    @DisplayName("Sacrificed creature's power includes +1/+1 counters")
    void sacrificedPowerIncludesCounters() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GoblinCohort()); // 2/2
        sacrifice.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3); // 5/5
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FrostOgre()); // 5/3

        prepare();
        harness.castInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Frost Ogre");
    }

    @Test
    @DisplayName("The sacrifice is paid before the spell resolves")
    void sacrificeIsPaidBeforeResolution() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GoblinCohort());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FrostOgre());

        prepare();
        harness.castInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());

        harness.assertNotOnBattlefield(player1, "Goblin Cohort");
        harness.assertInGraveyard(player1, "Goblin Cohort");
        assertThat(gd.stack).hasSize(1);
        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The target debuff wears off at cleanup")
    void debuffExpiresAtCleanup() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new BileUrchin());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FrostOgre());

        prepare();
        harness.castInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(-1);
        assertThat(target.getToughnessModifier()).isEqualTo(-1);

        harness.passUntil(TurnStep.CLEANUP);

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new BileUrchin());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new TendoIceBridge());

        prepare();
        assertThatThrownBy(() -> harness.castInstantWithSacrifice(
                player1, 0, land.getId(), sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Cannot cast without a creature to sacrifice")
    void cannotCastWithoutCreatureToSacrifice() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GnarledMass());

        prepare();
        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, target.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's creature")
    void cannotSacrificeOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoblinCohort());

        prepare();
        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, target.getId(), target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
    }
}
