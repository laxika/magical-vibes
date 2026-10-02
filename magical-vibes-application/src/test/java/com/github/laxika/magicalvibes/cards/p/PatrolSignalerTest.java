package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(PatrolSignaler.class)
class PatrolSignalerTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {1}{W} and untapping creates a 1/1 Kithkin Soldier token")
    void createsTokenAndUntapsSource() {
        Permanent signaler = addTapped(player1, new PatrolSignaler());
        harness.addMana(player1, ManaColor.WHITE, 2);

        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Kithkin Soldier"))
                .singleElement()
                .satisfies(t -> {
                    assertThat(t.getCard().isToken()).isTrue();
                    assertThat(t.getCard().hasType(CardType.CREATURE)).isTrue();
                    assertThat(t.getCard().getColor()).isEqualTo(CardColor.WHITE);
                    assertThat(t.getCard().getSubtypes())
                            .containsExactlyInAnyOrder(CardSubtype.KITHKIN, CardSubtype.SOLDIER);
                    assertThat(t.getCard().getPower()).isEqualTo(1);
                    assertThat(t.getCard().getToughness()).isEqualTo(1);
                });
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        // Paying {Q} untapped the source.
        assertThat(signaler.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate while the source is untapped ({Q} requires it to be tapped)")
    void cannotActivateWhileUntapped() {
        addCreatureReady(player1, new PatrolSignaler());
        harness.addMana(player1, ManaColor.WHITE, 2);

        enterMainWithPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not tapped");
    }

    @Test
    @DisplayName("Cannot activate while summoning sick, even if the source is tapped")
    void cannotActivateWhileSummoningSick() {
        Permanent signaler = harness.addToBattlefieldAndReturn(player1, new PatrolSignaler());
        signaler.tap();
        harness.addMana(player1, ManaColor.WHITE, 2);

        enterMainWithPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(signaler.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(2);
    }

    private Permanent addTapped(Player player, Card card) {
        Permanent perm = addCreatureReady(player, card);
        perm.tap();
        return perm;
    }

    private void enterMainWithPriority(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
