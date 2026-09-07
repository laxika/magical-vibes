package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(StarlitSanctum.class)
@DisplayName("Starlit Sanctum")
class StarlitSanctumTest extends BaseCardTest {

    @Test
    @DisplayName("Adds one colorless mana")
    void addsColorlessMana() {
        harness.addToBattlefield(player1, new StarlitSanctum());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Gains life equal to the sacrificed Cleric's toughness")
    void gainsLifeEqualToSacrificedClericToughness() {
        harness.addToBattlefield(player1, new StarlitSanctum());
        harness.addToBattlefield(player1, createCleric("Starlit Cleric", 2, 5));
        harness.addMana(player1, ManaColor.WHITE, 1);
        prepareAbility();

        harness.setLife(player1, 10);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
        harness.assertInGraveyard(player1, "Starlit Cleric");
    }

    @Test
    @DisplayName("Makes a target player lose life equal to the sacrificed Cleric's power")
    void targetPlayerLosesLifeEqualToSacrificedClericPower() {
        harness.addToBattlefield(player1, new StarlitSanctum());
        harness.addToBattlefield(player1, createCleric("Starlit Cleric", 4, 2));
        harness.addMana(player1, ManaColor.BLACK, 1);
        prepareAbility();

        harness.setLife(player2, 20);
        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        harness.assertInGraveyard(player1, "Starlit Cleric");
    }

    @Test
    @DisplayName("Cannot pay a Cleric sacrifice cost with a non-Cleric creature")
    void cannotSacrificeNonClericCreature() {
        harness.addToBattlefield(player1, new StarlitSanctum());
        harness.addToBattlefield(player1, createCreature("Starlit Soldier", 2, 2));
        harness.addMana(player1, ManaColor.WHITE, 1);
        prepareAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareAbility() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private Card createCleric(String name, int power, int toughness) {
        Card card = createCreature(name, power, toughness);
        card.setSubtypes(List.of(CardSubtype.CLERIC));
        return card;
    }

    private Card createCreature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(CardColor.WHITE);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }
}
