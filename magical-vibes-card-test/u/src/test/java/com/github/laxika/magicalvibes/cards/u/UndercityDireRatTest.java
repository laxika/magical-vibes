package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.o.Overkill;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UndercityDireRat.class, WrathOfGod.class, Overkill.class})
class UndercityDireRatTest extends BaseCardTest {

    @Test
    @DisplayName("When Undercity Dire Rat dies, its controller creates a Treasure token")
    void deathTriggerCreatesTreasureToken() {
        harness.addToBattlefield(player1, new UndercityDireRat());

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("The death-trigger Treasure is an artifact token with the Treasure subtype")
    void deathTriggerCreatesTreasureWithCorrectProperties() {
        harness.addToBattlefield(player1, new UndercityDireRat());

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent treasure = findPermanent(player1, "Treasure");

        assertThat(treasure.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(treasure.getCard().getSubtypes()).contains(CardSubtype.TREASURE);
        assertThat(treasure.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Zero toughness triggers Rat Tail, but the Treasure waits for the trigger to resolve")
    void zeroToughnessDeathUsesTheStack() {
        Permanent rat = harness.addToBattlefieldAndReturn(player1, new UndercityDireRat());
        harness.setHand(player1, List.of(new Overkill()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, rat.getId());

        harness.assertInGraveyard(player1, "Undercity Dire Rat");
        harness.assertNotOnBattlefield(player1, "Undercity Dire Rat");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Simultaneous Rat deaths give each controller one Treasure per Rat")
    void simultaneousDeathsCreateTreasuresForEachController() {
        harness.addToBattlefield(player1, new UndercityDireRat());
        harness.addToBattlefield(player1, new UndercityDireRat());
        harness.addToBattlefield(player2, new UndercityDireRat());

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
        assertThat(findPermanents(player2, "Treasure")).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Undercity Dire Rat");
        harness.assertNotOnBattlefield(player2, "Undercity Dire Rat");
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("The Treasure enters untapped and can immediately be sacrificed for any color")
    void treasureCanImmediatelyProduceAnyColor(ManaColor color) {
        Permanent rat = harness.addToBattlefieldAndReturn(player1, new UndercityDireRat());
        harness.setHand(player1, List.of(new Overkill()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, rat.getId());
        resolveAllTriggers();

        Permanent treasure = findPermanent(player1, "Treasure");
        assertThat(treasure.isTapped()).isFalse();
        int manaBefore = gd.playerManaPools.get(player1.getId()).get(color);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(manaBefore + 1);
        assertThat(gd.stack).isEmpty();
    }
}
