package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HystericalBlindness.class, GrizzlyBears.class})
class HystericalBlindnessTest extends BaseCardTest {

    @Test
    @DisplayName("Gives -4/-0 to opponent's creatures")
    void debuffsOpponentCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2

        harness.setHand(player1, List.of(new HystericalBlindness()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0);

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.getEffectivePower()).isEqualTo(-2); // 2 - 4 = -2
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not affect controller's own creatures")
    void doesNotAffectOwnCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears()); // 2/2

        harness.setHand(player1, List.of(new HystericalBlindness()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0);

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Effects wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2

        harness.setHand(player1, List.of(new HystericalBlindness()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0);

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.getEffectivePower()).isEqualTo(-2);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Affects every opposing creature and leaves friendly creatures unchanged")
    void affectsAllOpposingCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HystericalBlindness()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0);

        assertThat(findPermanents(player2, "Grizzly Bears")).hasSize(2).allSatisfy(bears -> {
            assertThat(bears.getEffectivePower()).isEqualTo(-2);
            assertThat(bears.getEffectiveToughness()).isEqualTo(2);
        });
        assertThat(findPermanent(player1, "Grizzly Bears").getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures entering after resolution are unaffected")
    void doesNotAffectCreaturesEnteringLater() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent affected = findPermanent(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new HystericalBlindness()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0);
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(affected.getEffectivePower()).isEqualTo(-2);
        assertThat(findPermanents(player2, "Grizzly Bears")).hasSize(2).anySatisfy(bears -> {
            assertThat(bears.getId()).isNotEqualTo(affected.getId());
            assertThat(bears.getEffectivePower()).isEqualTo(2);
            assertThat(bears.getEffectiveToughness()).isEqualTo(2);
        });
    }

    @Test
    @DisplayName("Resolves without opposing creatures or targets")
    void resolvesWithoutOpposingCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new HystericalBlindness()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Hysterical Blindness");
        assertThat(findPermanent(player1, "Grizzly Bears").getEffectivePower()).isEqualTo(2);
        assertThat(findPermanent(player1, "Grizzly Bears").getEffectiveToughness()).isEqualTo(2);
    }
}
