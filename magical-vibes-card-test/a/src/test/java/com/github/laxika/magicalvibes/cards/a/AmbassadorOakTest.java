package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.v.VioletPall;
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

    @Test
    @CardUsed(VioletPall.class)
    @DisplayName("The Elf Warrior token is created even if Ambassador Oak dies before its trigger resolves")
    void createsTokenAfterSourceDies() {
        harness.castFromHand(player1, new AmbassadorOak(), "{3}{G}");
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new VioletPall()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Ambassador Oak"));

        harness.assertInGraveyard(player1, "Ambassador Oak");
        harness.assertNotOnBattlefield(player1, "Ambassador Oak");
        harness.assertNotOnBattlefield(player1, "Elf Warrior");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Elf Warrior")).hasSize(1);
        harness.assertNotOnBattlefield(player2, "Elf Warrior");
        assertThat(gd.stack).isEmpty();
    }
}
