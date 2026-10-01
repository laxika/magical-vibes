package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(AmbassadorOak.class)
class AmbassadorOakTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Ambassador Oak puts its ETB token trigger on the stack")
    void resolvingPutsEtbOnStack() {
        harness.castFromHand(player1, new AmbassadorOak(), "{3}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ambassador Oak");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("ETB trigger creates a 1/1 green Elf Warrior token")
    void etbCreatesElfWarriorToken() {
        harness.castFromHand(player1, new AmbassadorOak(), "{3}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        Permanent token = findPermanent(player1, "Elf Warrior");
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB trigger creates a green Elf Warrior creature token")
    void etbTokenHasCorrectProperties() {
        harness.castFromHand(player1, new AmbassadorOak(), "{3}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Elf Warrior");
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getSubtypes())
                .containsExactly(CardSubtype.ELF, CardSubtype.WARRIOR);
        assertThat(token.getCard().isToken()).isTrue();
    }
}
