package com.github.laxika.magicalvibes.cards.f;

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

@CardUsed({FlamekinGildweaver.class})
class FlamekinGildweaverTest extends BaseCardTest {

    @Test
    @DisplayName("When Flamekin Gildweaver enters, it creates one Treasure token")
    void etbCreatesOneTreasureToken() {
        harness.setHand(player1, List.of(new FlamekinGildweaver()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        List<Permanent> treasures = findPermanents(player1, "Treasure");
        assertThat(treasures).hasSize(1);
        assertThat(treasures.getFirst().getCard().isToken()).isTrue();
        assertThat(treasures.getFirst().getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(treasures.getFirst().getCard().getSubtypes()).contains(CardSubtype.TREASURE);
    }

    @Test
    void treasureIsCreatedOnlyWhenEnterTriggerResolves() {
        harness.setHand(player1, List.of(new FlamekinGildweaver()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Flamekin Gildweaver");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void enteringWithoutBeingCastCreatesTreasureForItsController() {
        harness.enterBattlefieldAndReturn(player2, new FlamekinGildweaver());
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Treasure")).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void treasureCanImmediatelyBeSacrificedForColoredMana() {
        harness.setHand(player1, List.of(new FlamekinGildweaver()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent treasure = findPermanent(player1, "Treasure");
        assertThat(treasure.isTapped()).isFalse();
        int treasureIndex = gd.playerBattlefields.get(player1.getId()).indexOf(treasure);
        harness.activateAbility(player1, treasureIndex, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
