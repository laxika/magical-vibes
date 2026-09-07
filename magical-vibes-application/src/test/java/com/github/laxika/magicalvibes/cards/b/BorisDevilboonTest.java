package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(BorisDevilboon.class)
class BorisDevilboonTest extends BaseCardTest {

    @Test
    @DisplayName("{2}{B}{R}, {T}: creates a 1/1 black-and-red Minor Demon token")
    void createsMinorDemonToken() {
        Permanent boris = addReadyBoris();
        addCostMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Minor Demon");
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.BLACK, CardColor.RED);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.DEMON);
        assertThat(boris.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate while Boris Devilboon is tapped")
    void cannotActivateWhileTapped() {
        addReadyBoris();
        addCostMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        addCostMana(player1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyBoris() {
        Permanent boris = harness.addToBattlefieldAndReturn(player1, new BorisDevilboon());
        boris.setSummoningSick(false);
        return boris;
    }

    private void addCostMana(Player player) {
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }
}
