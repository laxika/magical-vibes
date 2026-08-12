package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PulseOfTheTangleTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 3/3 Beast and returns to hand when an opponent still has more creatures")
    void createsBeastAndReturnsToHand() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        PulseOfTheTangle pulse = cast();

        Permanent beast = findBeastToken();
        assertThat(beast.getEffectivePower()).isEqualTo(3);
        assertThat(beast.getEffectiveToughness()).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).contains(pulse);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(pulse);
    }

    @Test
    @DisplayName("Checks the creature count after creating the token")
    void doesNotReturnWhenTheTokenMakesTheCountsEqual() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        PulseOfTheTangle pulse = cast();

        assertThat(findBeastToken()).isNotNull();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(pulse);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(pulse);
    }

    private PulseOfTheTangle cast() {
        PulseOfTheTangle pulse = new PulseOfTheTangle();
        harness.setHand(player1, List.of(pulse));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        return pulse;
    }

    private Permanent findBeastToken() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.BEAST))
                .findFirst()
                .orElseThrow();
    }
}
