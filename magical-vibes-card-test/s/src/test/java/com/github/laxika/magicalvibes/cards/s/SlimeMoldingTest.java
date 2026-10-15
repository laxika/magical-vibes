package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SlimeMolding.class})
class SlimeMoldingTest extends BaseCardTest {

    private List<Permanent> oozes() {
        return findPermanents(player1, "Ooze").stream()
                .filter(p -> p.getCard().isToken())
                .toList();
    }

    @Test
    @DisplayName("X=3 creates a single 3/3 Ooze token")
    void createsXByXOoze() {
        harness.setHand(player1, List.of(new SlimeMolding()));
        harness.addMana(player1, ManaColor.GREEN, 4); // X=3: {3}{G}

        harness.castAndResolveSorcery(player1, 0, 3);

        assertThat(oozes()).hasSize(1);
        Permanent ooze = oozes().getFirst();
        assertThat(ooze.getCard().getPower()).isEqualTo(3);
        assertThat(ooze.getCard().getToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("X=1 creates a 1/1 Ooze token")
    void xOneCreatesOneOneOoze() {
        harness.setHand(player1, List.of(new SlimeMolding()));
        harness.addMana(player1, ManaColor.GREEN, 2); // X=1: {1}{G}

        harness.castAndResolveSorcery(player1, 0, 1);

        assertThat(oozes()).hasSize(1);
        assertThat(oozes().getFirst().getCard().getPower()).isEqualTo(1);
        assertThat(oozes().getFirst().getCard().getToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("X=0 creates a 0/0 Ooze that dies immediately to state-based actions")
    void xZeroTokenDies() {
        harness.setHand(player1, List.of(new SlimeMolding()));
        harness.addMana(player1, ManaColor.GREEN, 1); // X=0: {0}{G}

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(oozes()).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Creates an untapped green Ooze creature under the caster's control")
    void createsGreenOozeCreature() {
        harness.setHand(player1, List.of(new SlimeMolding()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, 2);

        assertThat(oozes()).hasSize(1);
        Permanent ooze = oozes().getFirst();
        assertThat(ooze.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(ooze.getCard().getColors()).containsExactly(CardColor.GREEN);
        assertThat(ooze.getCard().getSubtypes()).containsExactly(CardSubtype.OOZE);
        assertThat(ooze.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Slime Molding");
    }

    @Test
    @DisplayName("Separate casts preserve each token's chosen X")
    void separateCastsKeepIndependentSizes() {
        harness.setHand(player1, List.of(new SlimeMolding(), new SlimeMolding()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castAndResolveSorcery(player1, 0, 1);
        harness.castAndResolveSorcery(player1, 0, 4);

        assertThat(oozes()).hasSize(2);
        assertThat(oozes()).extracting(p -> p.getCard().getPower()).containsExactlyInAnyOrder(1, 4);
        assertThat(oozes()).extracting(p -> p.getCard().getToughness()).containsExactlyInAnyOrder(1, 4);
    }
}
