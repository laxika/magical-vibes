package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PaladinEnVec;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Forest.class, GrizzlyBears.class, PaladinEnVec.class, SwiftDemise.class})
class SwiftDemiseTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage, then destroys each damaged opposing creature")
    void damagesTargetThenDestroysDamagedOpposingCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent previouslyDamaged = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent undamaged = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent ownDamaged = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.permanentsDealtDamageThisTurn.add(previouslyDamaged.getId());
        gd.permanentsDealtDamageThisTurn.add(ownDamaged.getId());

        cast(target);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()))
                .noneMatch(permanent -> permanent.getId().equals(previouslyDamaged.getId()))
                .anyMatch(permanent -> permanent.getId().equals(undamaged.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(ownDamaged.getId()));
    }

    @Test
    @DisplayName("Does not destroy the controller's damaged creatures")
    void doesNotDestroyOwnDamagedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast(target);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new SwiftDemise()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Targeting your own creature still destroys damaged opposing creatures")
    void ownTargetStillAllowsOpposingCreaturesToBeDestroyed() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.permanentsDealtDamageThisTurn.add(opposing.getId());

        cast(target);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposing);
    }

    @Test
    @DisplayName("An illegal sole target prevents the entire spell from resolving")
    void illegalTargetPreventsDestruction() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent previouslyDamaged = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.permanentsDealtDamageThisTurn.add(previouslyDamaged.getId());
        harness.setHand(player1, List.of(new SwiftDemise()));
        addMana();
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(previouslyDamaged);
        harness.assertInGraveyard(player1, "Swift Demise");
    }

    @Test
    @DisplayName("Prevented damage does not qualify an undamaged target for destruction")
    void preventedDamageDoesNotDestroyUndamagedTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setDamagePreventionShield(1);
        Permanent previouslyDamaged = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.permanentsDealtDamageThisTurn.add(previouslyDamaged.getId());

        cast(target);

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(target.getDamagePreventionShield()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(target).doesNotContain(previouslyDamaged);
    }

    @Test
    @DisplayName("Preventing the new damage does not spare a creature damaged earlier this turn")
    void preventedDamageStillDestroysPreviouslyDamagedTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setDamagePreventionShield(1);
        gd.permanentsDealtDamageThisTurn.add(target.getId());

        cast(target);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Protection from black does not stop the untargeted destruction")
    void destroysPreviouslyDamagedCreatureWithProtectionFromBlack() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player2, new PaladinEnVec());
        gd.permanentsDealtDamageThisTurn.add(protectedCreature.getId());

        cast(target);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(protectedCreature);
        harness.assertInGraveyard(player2, "Paladin en-Vec");
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new SwiftDemise()));
        addMana();
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
