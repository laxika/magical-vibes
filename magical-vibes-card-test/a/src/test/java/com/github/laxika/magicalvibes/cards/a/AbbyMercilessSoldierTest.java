package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AbbyMercilessSoldier.class})
class AbbyMercilessSoldierTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Abby creates one Cordyceps Infected token per mana spent")
    void createsTokensEqualToManaSpent() {
        harness.setHand(player1, List.of(new AbbyMercilessSoldier()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Cordyceps Infected")).hasSize(3);
        Permanent token = findPermanent(player1, "Cordyceps Infected");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getColors()).containsExactly(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.FUNGUS, CardSubtype.ZOMBIE);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
    }

    @Test
    @DisplayName("Abby enters under an opponent's control")
    void entersUnderOpponentsControl() {
        harness.castFromHand(player1, new AbbyMercilessSoldier(), "{1}{R}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Abby, Merciless Soldier");
        harness.assertOnBattlefield(player2, "Abby, Merciless Soldier");
    }

    @Test
    @DisplayName("Abby enters under an opponent's control without a control-transfer trigger")
    void opponentControlsAbbyAsSoonAsSpellResolves() {
        harness.castFromHand(player1, new AbbyMercilessSoldier(), "{1}{R}{G}");

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Abby, Merciless Soldier");
        harness.assertOnBattlefield(player2, "Abby, Merciless Soldier");
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Cordyceps Infected")).hasSize(3);
        assertThat(findPermanents(player2, "Cordyceps Infected")).isEmpty();
    }
}
