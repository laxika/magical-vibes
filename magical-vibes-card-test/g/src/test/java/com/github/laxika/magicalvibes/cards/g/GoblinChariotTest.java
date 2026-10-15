package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.v.ViridianLongbow;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinChariot.class, ViridianLongbow.class})
class GoblinChariotTest extends BaseCardTest {

    @Test
    @DisplayName("Haste lets Goblin Chariot attack the turn it enters")
    void hasteAllowsAttackingTheTurnItEnters() {
        harness.castFromHand(player1, new GoblinChariot(), "{2}{R}");
        harness.passBothPriorities();

        declareAttackers(List.of(0));

        Permanent chariot = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(chariot.isAttackedThisTurn()).isTrue();
        assertThat(chariot.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Haste lets Goblin Chariot be tapped for an ability the turn it enters")
    void hasteAllowsPayingATapCostTheTurnItEnters() {
        harness.castFromHand(player1, new GoblinChariot(), "{2}{R}");
        harness.passBothPriorities();

        Permanent chariot = findPermanent(player1, "Goblin Chariot");
        Permanent longbow = harness.addToBattlefieldAndReturn(player1, new ViridianLongbow());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 1, null, chariot.getId());
        harness.passBothPriorities();
        assertThat(longbow.getAttachedTo()).isEqualTo(chariot.getId());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(chariot.isTapped()).isTrue();
        harness.assertLife(player2, 19);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
