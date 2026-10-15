package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DireFleetHoarder.class, WrathOfGod.class})
class DireFleetHoarderTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Dire Fleet Hoarder puts it on the battlefield")
    void castingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new DireFleetHoarder()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Dire Fleet Hoarder");
    }

    @Test
    @DisplayName("When Dire Fleet Hoarder dies, a Treasure token is created")
    void deathTriggerCreatesTreasureToken() {
        harness.addToBattlefield(player1, new DireFleetHoarder());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities(); // Resolve Wrath — Dire Fleet Hoarder dies

        GameData gd = harness.getGameData();

        // Dire Fleet Hoarder should be in the graveyard
        harness.assertInGraveyard(player1, "Dire Fleet Hoarder");

        // One death trigger should be on the stack
        assertThat(gd.stack).hasSize(1);

        // Resolve the death trigger
        harness.passBothPriorities();

        // A Treasure token should be on the battlefield
        List<Permanent> tokens = findPermanents(player1, "Treasure");
        assertThat(tokens).hasSize(1);
    }

    @Test
    @DisplayName("Death trigger token is a Treasure artifact with correct properties")
    void tokenHasCorrectProperties() {
        harness.addToBattlefield(player1, new DireFleetHoarder());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities(); // Resolve Wrath
        harness.passBothPriorities(); // Resolve death trigger

        Permanent token = findPermanent(player1, "Treasure");

        assertThat(token.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.TREASURE);
        assertThat(token.getCard().isToken()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void treasureCanImmediatelyBeSacrificedForOneManaOfAnyColor(ManaColor color) {
        harness.addToBattlefield(player1, new DireFleetHoarder());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Treasure").isTapped()).isFalse();
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void simultaneousDeathsCreateOneTreasureForEachController() {
        harness.addToBattlefield(player1, new DireFleetHoarder());
        harness.addToBattlefield(player2, new DireFleetHoarder());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Dire Fleet Hoarder");
        harness.assertInGraveyard(player2, "Dire Fleet Hoarder");
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player2, "Treasure")).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }
}
