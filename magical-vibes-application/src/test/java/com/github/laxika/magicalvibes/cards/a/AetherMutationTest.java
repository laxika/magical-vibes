package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AetherMutation.class, Forest.class, GrizzlyBears.class, HillGiant.class})
class AetherMutationTest extends BaseCardTest {

    @Test
    void returnsTargetCreatureAndCreatesSaprolingsEqualToItsManaValue() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(target);

        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(countPermanents(player1, "Saproling")).isEqualTo(2);
    }

    @Test
    void createsTheTargetManaValueNumberOfTokensForLargerCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        cast(target);

        harness.assertInHand(player2, "Hill Giant");
        assertThat(countPermanents(player1, "Saproling")).isEqualTo(4);
    }

    @Test
    void cannotTargetNonCreaturePermanents() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new AetherMutation()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new AetherMutation()));
        addMana();
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
