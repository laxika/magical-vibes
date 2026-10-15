package com.github.laxika.magicalvibes.cards.b;

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

@CardUsed({BrazenFreebooter.class})
class BrazenFreebooterTest extends BaseCardTest {

    @Test
    @DisplayName("When Brazen Freebooter enters, one Treasure token is created")
    void etbCreatesTreasureToken() {
        harness.setHand(player1, List.of(new BrazenFreebooter()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Brazen Freebooter's ETB Treasure is an artifact token with Treasure subtype")
    void etbTreasureHasExpectedProperties() {
        harness.setHand(player1, List.of(new BrazenFreebooter()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent treasure = findPermanent(player1, "Treasure");

        assertThat(treasure.getCard().isToken()).isTrue();
        assertThat(treasure.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(treasure.getCard().getSubtypes()).contains(CardSubtype.TREASURE);
    }

    @Test
    @DisplayName("Treasure is created only when the enter trigger resolves")
    void treasureWaitsForTriggerResolution() {
        harness.setHand(player1, List.of(new BrazenFreebooter()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Brazen Freebooter");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanent(player1, "Treasure").isTapped()).isFalse();
    }

    @Test
    @DisplayName("An opponent's Freebooter creates Treasure for that opponent")
    void opponentReceivesOwnTreasure() {
        harness.enterBattlefieldAndReturn(player2, new BrazenFreebooter());
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Treasure")).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, mode = EnumSource.Mode.EXCLUDE, names = "COLORLESS")
    @DisplayName("The Treasure can immediately be sacrificed for one mana of any color")
    void treasureProducesOneManaOfChosenColor(ManaColor color) {
        harness.enterBattlefieldAndReturn(player1, new BrazenFreebooter());
        resolveAllTriggers();
        Permanent treasure = findPermanent(player1, "Treasure");
        var manaPool = gd.playerManaPools.get(player1.getId());
        int before = manaPool.get(color);
        int totalBefore = manaPool.getTotalAllMana();
        int treasureIndex = gd.playerBattlefields.get(player1.getId()).indexOf(treasure);

        harness.activateAbility(player1, treasureIndex, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        harness.assertOnBattlefield(player1, "Brazen Freebooter");
        assertThat(manaPool.get(color)).isEqualTo(before + 1);
        assertThat(manaPool.getTotalAllMana()).isEqualTo(totalBefore + 1);
        assertThat(gd.stack).isEmpty();
    }
}
