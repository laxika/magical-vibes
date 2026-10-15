package com.github.laxika.magicalvibes.cards.b;

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

@CardUsed({BannersRaised.class, GrizzlyBears.class})
class BannersRaisedTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving gives all creatures you control +1/+0")
    void boostsAllOwnCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BannersRaised()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0);

        for (Permanent p : gd.playerBattlefields.get(player1.getId())) {
            assertThat(p.getEffectivePower()).isEqualTo(3);
            assertThat(p.getEffectiveToughness()).isEqualTo(2);
        }
        harness.assertInGraveyard(player1, "Banners Raised");
    }

    @Test
    @DisplayName("Does not boost opponent's creatures")
    void doesNotBoostOpponentCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BannersRaised()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getEffectivePower()).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost wears off at cleanup")
    void boostWearsOff() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BannersRaised()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Resolves with an empty battlefield")
    void resolvesWithEmptyBattlefield() {
        harness.setHand(player1, List.of(new BannersRaised()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Creatures entering after resolution do not receive the boost")
    void doesNotBoostCreaturesEnteringLater() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BannersRaised()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0);
        Permanent later = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(original.getEffectivePower()).isEqualTo(3);
        assertThat(original.getEffectiveToughness()).isEqualTo(2);
        assertThat(later.getEffectivePower()).isEqualTo(2);
        assertThat(later.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures entering while the spell is on the stack receive the boost")
    void boostsCreaturesPresentAtResolution() {
        harness.setHand(player1, List.of(new BannersRaised()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(3);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
    }
}
