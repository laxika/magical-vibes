package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
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
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlightGrenade.class, GrizzlyBears.class, Plains.class, SerraAngel.class})
class BlightGrenadeTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys the target creature and gives all other creatures -3/-3")
    void destroysTargetAndWeakensAllCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent ownAngel = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        Permanent opposingAngel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        castBlightGrenade(target.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, ownAngel)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownAngel)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opposingAngel)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opposingAngel)).isEqualTo(1);
    }

    @Test
    @DisplayName("The -3/-3 wears off at end of turn")
    void weakensUntilEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        castBlightGrenade(target.getId());
        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void rejectsNonCreatureTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new BlightGrenade()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("The reduction kills untargeted creatures on both battlefields")
    void reductionKillsUntargetedCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castBlightGrenade(target.getId());

        harness.assertInGraveyard(player2, "Serra Angel");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("An illegal sole target prevents the entire spell from resolving")
    void illegalTargetPreventsReduction() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        harness.setHand(player1, List.of(new BlightGrenade()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(4);
        harness.assertInGraveyard(player1, "Blight Grenade");
    }

    @Test
    @DisplayName("Creatures entering after resolution are not weakened")
    void laterCreaturesAreUnaffected() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castBlightGrenade(target.getId());

        Permanent laterCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, laterCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, laterCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("A regenerated target also receives the reduction")
    void regeneratedTargetIsWeakened() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        target.setRegenerationShield(1);

        castBlightGrenade(target.getId());

        harness.assertOnBattlefield(player2, "Serra Angel");
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getRegenerationShield()).isZero();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration cannot save a target reduced to zero toughness")
    void regeneratedSmallTargetDiesFromReduction() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setRegenerationShield(2);

        castBlightGrenade(target.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    private void castBlightGrenade(UUID targetId) {
        harness.setHand(player1, List.of(new BlightGrenade()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castAndResolveSorcery(player1, 0, targetId);
    }
}
