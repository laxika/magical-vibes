package com.github.laxika.magicalvibes.cards.c;

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

@CardUsed({CommonCrook.class, WrathOfGod.class})
class CommonCrookTest extends BaseCardTest {

    @Test
    @DisplayName("When Common Crook dies, it creates a Treasure token")
    void deathCreatesTreasureToken() {
        harness.addToBattlefield(player1, new CommonCrook());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Common Crook");
        Permanent treasure = findPermanent(player1, "Treasure");
        assertThat(treasure.getCard().isToken()).isTrue();
        assertThat(treasure.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(treasure.getCard().getSubtypes()).contains(CardSubtype.TREASURE);
    }

    @Test
    @DisplayName("Nonlethal damage does not create a Treasure")
    void nonlethalDamageDoesNotCreateTreasure() {
        Permanent crook = harness.addToBattlefieldAndReturn(player1, new CommonCrook());
        crook.setMarkedDamage(1);

        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Common Crook");
        harness.assertNotOnBattlefield(player1, "Treasure");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each Crook dying from lethal damage creates one Treasure for its controller")
    void simultaneousDeathsCreateOneTreasurePerCrook() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new CommonCrook());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new CommonCrook());
        first.setMarkedDamage(2);
        second.setMarkedDamage(2);

        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player2, "Common Crook");
        harness.assertNotOnBattlefield(player2, "Treasure");
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        assertThat(findPermanents(player2, "Treasure")).hasSize(1);
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Treasure")).hasSize(2);
        harness.assertNotOnBattlefield(player1, "Treasure");
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("The Treasure enters untapped and can immediately be sacrificed for any color")
    void treasureCanImmediatelyProduceAnyColor(ManaColor color) {
        Permanent crook = harness.addToBattlefieldAndReturn(player1, new CommonCrook());
        crook.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanent(player1, "Treasure").isTapped()).isFalse();

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, color.name());

        harness.assertNotOnBattlefield(player1, "Treasure");
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
