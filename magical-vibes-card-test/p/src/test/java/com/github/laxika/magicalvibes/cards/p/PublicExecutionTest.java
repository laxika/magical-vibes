package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
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

@CardUsed({PublicExecution.class, GrizzlyBears.class, HillGiant.class, PrimalHuntbeast.class})
class PublicExecutionTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys the targeted creature and shrinks that player's other creatures")
    void destroysTargetAndShrinksOthers() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new PublicExecution()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
    }

    @Test
    @DisplayName("Creatures the caster controls are unaffected")
    void doesNotShrinkCastersCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent ownGiant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new PublicExecution()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, targetId);

        assertThat(gqs.getEffectivePower(gd, ownGiant)).isEqualTo(3);
    }

    @Test
    @DisplayName("The -2/-0 wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new PublicExecution()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, targetId);
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a creature the caster controls")
    void cannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new PublicExecution()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A regenerating target is excluded from the power reduction")
    void regeneratingTargetIsExcluded() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setRegenerationShield(1);
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new PublicExecution()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(target.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
    }

    @Test
    @DisplayName("The power reduction affects other creatures with hexproof")
    void shrinksOtherHexproofCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent huntbeast = harness.addToBattlefieldAndReturn(player2, new PrimalHuntbeast());
        harness.setHand(player1, List.of(new PublicExecution()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, huntbeast)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, huntbeast)).isEqualTo(3);
    }

    @Test
    @DisplayName("Creatures entering after resolution are unaffected")
    void laterCreaturesAreUnaffected() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new PublicExecution()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castAndResolveInstant(player1, 0, target.getId());
        Permanent laterGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, laterGiant)).isEqualTo(3);
    }

    @Test
    @DisplayName("An absent target prevents the power reduction")
    void absentTargetPreventsAllEffects() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new PublicExecution()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
        harness.assertInGraveyard(player1, "Public Execution");
    }
}
