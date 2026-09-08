package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VampiresKiss.class, GrizzlyBears.class})
class VampiresKissTest extends BaseCardTest {

    @Test
    @DisplayName("Target opponent loses 2 life, controller gains 2 life, and two Blood tokens are created")
    void drainsOpponentAndCreatesTwoBloodTokens() {
        harness.setHand(player1, List.of(new VampiresKiss()));
        addMana();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 22);
        assertThat(findPermanents(player1, "Blood")).hasSize(2)
                .allSatisfy(blood -> {
                    assertThat(blood.getCard().isToken()).isTrue();
                    assertThat(blood.getCard().getType()).isEqualTo(CardType.ARTIFACT);
                    assertThat(blood.getCard().getSubtypes()).contains(CardSubtype.BLOOD);
                });
    }

    @Test
    @DisplayName("Can target yourself")
    void canTargetYourself() {
        harness.setHand(player1, List.of(new VampiresKiss()));
        addMana();

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(findPermanents(player1, "Blood")).hasSize(2);
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new VampiresKiss()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
