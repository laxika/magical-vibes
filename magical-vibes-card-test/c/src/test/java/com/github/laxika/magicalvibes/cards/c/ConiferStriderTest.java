package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SeismicRupture;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ConiferStrider.class, Shock.class, SeismicRupture.class})
class ConiferStriderTest extends BaseCardTest {

    @Test
    @DisplayName("Resolves as a creature on the battlefield")
    void resolvesOntoBattlefield() {
        harness.setHand(player1, List.of(new ConiferStrider()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Conifer Strider");
    }

    @Test
    @DisplayName("An opponent cannot target it with a spell")
    void opponentCannotTargetIt() {
        Permanent strider = harness.addToBattlefieldAndReturn(player1, new ConiferStrider());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, strider.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("Its controller can target it with a spell")
    void controllerCanTargetIt() {
        Permanent strider = harness.addToBattlefieldAndReturn(player1, new ConiferStrider());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, strider.getId());

        harness.assertNotOnBattlefield(player1, "Conifer Strider");
        harness.assertInGraveyard(player1, "Conifer Strider");
    }

    @Test
    @DisplayName("Hexproof does not prevent an opponent's nontargeted damage")
    void opponentCanDealNontargetedDamage() {
        harness.addToBattlefield(player2, new ConiferStrider());
        harness.setHand(player1, List.of(new SeismicRupture()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Conifer Strider");
        harness.assertInGraveyard(player2, "Conifer Strider");
    }
}
