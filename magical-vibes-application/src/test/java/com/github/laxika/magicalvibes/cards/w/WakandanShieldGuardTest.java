package com.github.laxika.magicalvibes.cards.w;

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

@CardUsed(WakandanShieldGuard.class)
class WakandanShieldGuardTest extends BaseCardTest {

    @Test
    @DisplayName("When Wakandan Shield Guard enters, it creates a 1/1 white Soldier token")
    void etbCreatesSoldierToken() {
        harness.setHand(player1, List.of(new WakandanShieldGuard()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent soldier = findPermanent(player1, "Soldier");
        assertThat(soldier.getCard().getPower()).isEqualTo(1);
        assertThat(soldier.getCard().getToughness()).isEqualTo(1);
        assertThat(soldier.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(soldier.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(soldier.getCard().getSubtypes()).contains(CardSubtype.SOLDIER);
        assertThat(soldier.getCard().isToken()).isTrue();
    }
}
