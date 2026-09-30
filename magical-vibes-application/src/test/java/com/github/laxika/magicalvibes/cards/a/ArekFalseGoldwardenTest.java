package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArekFalseGoldwarden.class, GrizzlyBears.class})
class ArekFalseGoldwardenTest extends BaseCardTest {

    @Test
    void anotherCreatureYouControlIntensifiesArek() {
        Permanent arek = addCreatureReady(player1, new ArekFalseGoldwarden());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getCardIntensity(arek.getCard().getId())).isEqualTo(1);
    }

    @Test
    void sacrificeAbilityDrainsOpponentByArekIntensity() {
        Permanent arek = addCreatureReady(player1, new ArekFalseGoldwarden());
        gd.intensifyCard(arek.getCard(), 3);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player1, "Arek, False Goldwarden");
    }

    @Test
    void sacrificeAbilityCannotTargetItsController() {
        addCreatureReady(player1, new ArekFalseGoldwarden());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }
}
