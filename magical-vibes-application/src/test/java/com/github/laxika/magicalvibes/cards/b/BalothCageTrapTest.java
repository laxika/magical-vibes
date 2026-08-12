package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BalothCageTrapTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 4/4 green Beast creature token")
    void createsBeastToken() {
        harness.setHand(player1, List.of(new BalothCageTrap()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        Permanent beast = findPermanent(player1, "Beast");
        assertThat(beast.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(beast.getCard().getSubtypes()).contains(CardSubtype.BEAST);
        assertThat(beast.getEffectivePower()).isEqualTo(4);
        assertThat(beast.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Can be cast for {1}{G} after an opponent artifact entered this turn")
    void castsForAlternateCostAfterOpponentArtifactEntered() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        gd.permanentsEnteredBattlefieldThisTurn.put(player2.getId(), List.of(artifact.getCard()));
        harness.setHand(player1, List.of(new BalothCageTrap()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Beast")).isEqualTo(1);
    }

    @Test
    @DisplayName("The alternate cost requires an opponent artifact to have entered this turn")
    void alternateCostRequiresOpponentArtifact() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.permanentsEnteredBattlefieldThisTurn.put(player2.getId(), List.of(creature.getCard()));
        harness.setHand(player1, List.of(new BalothCageTrap()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }
}
