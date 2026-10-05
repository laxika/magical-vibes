package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GnatMiser;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MoonbowIllusionist.class, MirenTheMoaningWell.class, GnatMiser.class})
class MoonbowIllusionistTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a land as a cost and changes the target land's basic type")
    void returnsLandAndChangesTargetType() {
        harness.addToBattlefield(player1, new MoonbowIllusionist());
        harness.addToBattlefield(player1, new MirenTheMoaningWell());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MirenTheMoaningWell());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(player1, "Moonbow Illusionist"), 0, target.getId());

        harness.assertInHand(player1, "Miren, the Moaning Well");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ISLAND");

        assertThat(gqs.effectiveBasicLandTypes(gd, target)).containsExactly(CardSubtype.ISLAND);
    }

    @Test
    @DisplayName("Cannot activate without a land to return")
    void cannotActivateWithoutLand() {
        harness.addToBattlefield(player1, new MoonbowIllusionist());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MirenTheMoaningWell());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, "Moonbow Illusionist"), 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-land permanent")
    void cannotTargetNonLand() {
        harness.addToBattlefield(player1, new MoonbowIllusionist());
        harness.addToBattlefield(player1, new MirenTheMoaningWell());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GnatMiser());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, "Moonbow Illusionist"), 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @Test
    @DisplayName("The type change wears off at end of turn")
    void typeChangeWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new MoonbowIllusionist());
        harness.addToBattlefield(player1, new MirenTheMoaningWell());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MirenTheMoaningWell());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(player1, "Moonbow Illusionist"), 0, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ISLAND");

        assertThat(gqs.effectiveBasicLandTypes(gd, target)).containsExactly(CardSubtype.ISLAND);
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.effectiveBasicLandTypes(gd, target)).isEmpty();
        assertThat(gqs.hasLostPrintedAbilities(gd, target)).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = CardSubtype.class, names = {"PLAINS", "ISLAND", "SWAMP", "MOUNTAIN", "FOREST"})
    @DisplayName("Each basic land type replaces printed abilities and supplies its mana ability")
    void chosenTypeReplacesAbilitiesAndProducesMana(CardSubtype type) {
        harness.addToBattlefield(player1, new MoonbowIllusionist());
        harness.addToBattlefield(player1, new MirenTheMoaningWell());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MirenTheMoaningWell());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(player1, "Moonbow Illusionist"), 0, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, type.name());

        assertThat(gqs.effectiveLandTypes(gd, target)).containsExactly(type);
        assertThat(gqs.hasLostPrintedAbilities(gd, target)).isTrue();
        harness.tapPermanent(player2, battlefieldIndex(player2, "Miren, the Moaning Well"));

        ManaColor expectedColor = switch (type) {
            case PLAINS -> ManaColor.WHITE;
            case ISLAND -> ManaColor.BLUE;
            case SWAMP -> ManaColor.BLACK;
            case MOUNTAIN -> ManaColor.RED;
            case FOREST -> ManaColor.GREEN;
            default -> throw new IllegalArgumentException("Not a basic land type");
        };
        assertThat(gd.playerManaPools.get(player2.getId()).get(expectedColor)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Returning the targeted land pays the cost but leaves no legal target on resolution")
    void canReturnTheTargetedLandAsCost() {
        harness.addToBattlefield(player1, new MoonbowIllusionist());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MirenTheMoaningWell());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(player1, "Moonbow Illusionist"), 0, target.getId());

        harness.assertInHand(player1, "Miren, the Moaning Well");
        harness.assertNotOnBattlefield(player1, "Miren, the Moaning Well");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private int battlefieldIndex(Player owner, String name) {
        return gd.playerBattlefields.get(owner.getId()).indexOf(findPermanent(owner, name));
    }
}
