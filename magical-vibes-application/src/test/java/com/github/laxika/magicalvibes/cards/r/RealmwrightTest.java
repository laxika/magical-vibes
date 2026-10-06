package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SimicGuildgate;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Realmwright.class, Forest.class, SimicGuildgate.class})
class RealmwrightTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Realmwright prompts for a basic land type")
    void resolvingPromptsForBasicLandType() {
        harness.setHand(player1, List.of(new Realmwright()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "ISLAND");

        assertThat(findPermanent(player1, "Realmwright").getChosenSubtype()).isEqualTo(CardSubtype.ISLAND);
    }

    @Test
    @DisplayName("Chosen type is added to own lands and grants its mana ability")
    void chosenTypeAddsManaAbilityToOwnLands() {
        Permanent realmwright = harness.addToBattlefieldAndReturn(player1, new Realmwright());
        realmwright.setChosenSubtype(CardSubtype.ISLAND);
        Permanent firstForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent secondForest = harness.addToBattlefieldAndReturn(player1, new Forest());

        assertThat(gqs.effectiveBasicLandTypes(gd, firstForest))
                .contains(CardSubtype.FOREST, CardSubtype.ISLAND);

        harness.activateAbility(player1, 1, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);

        gs.tapPermanent(gd, player1, 2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);

        assertThat(secondForest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Realmwright does not affect an opponent's lands")
    void opponentLandsAreUnaffected() {
        Permanent realmwright = harness.addToBattlefieldAndReturn(player1, new Realmwright());
        realmwright.setChosenSubtype(CardSubtype.ISLAND);
        Permanent opponentForest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThat(gqs.effectiveBasicLandTypes(gd, opponentForest))
                .containsExactly(CardSubtype.FOREST);
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = CardSubtype.class, names = {"PLAINS", "ISLAND", "SWAMP", "MOUNTAIN", "FOREST"})
    @DisplayName("Each basic land type can be chosen and grants the corresponding mana ability")
    void eachBasicLandTypeGrantsItsManaAbility(CardSubtype subtype) {
        harness.setHand(player1, List.of(new Realmwright()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, subtype.name());

        Permanent gate = harness.addToBattlefieldAndReturn(player1, new SimicGuildgate());
        assertThat(gqs.effectiveBasicLandTypes(gd, gate)).containsExactly(subtype);
        harness.activateAbility(player1, 1, 1, null, null);

        ManaColor color = switch (subtype) {
            case PLAINS -> ManaColor.WHITE;
            case ISLAND -> ManaColor.BLUE;
            case SWAMP -> ManaColor.BLACK;
            case MOUNTAIN -> ManaColor.RED;
            case FOREST -> ManaColor.GREEN;
            default -> throw new IllegalArgumentException("Not a basic land type");
        };
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gate.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Nonbasic lands retain their Gate type and original mana ability")
    void nonbasicLandRetainsOriginalTypesAndAbilities() {
        Permanent realmwright = harness.addToBattlefieldAndReturn(player1, new Realmwright());
        realmwright.setChosenSubtype(CardSubtype.MOUNTAIN);
        Permanent gate = harness.addToBattlefieldAndReturn(player1, new SimicGuildgate());

        assertThat(gqs.effectiveLandTypes(gd, gate)).contains(CardSubtype.GATE, CardSubtype.MOUNTAIN);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each Realmwright contributes its type only while on the battlefield")
    void multipleRealmwrightsContributeIndependently() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Realmwright());
        first.setChosenSubtype(CardSubtype.ISLAND);
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Realmwright());
        second.setChosenSubtype(CardSubtype.MOUNTAIN);
        Permanent gate = harness.addToBattlefieldAndReturn(player1, new SimicGuildgate());

        assertThat(gqs.effectiveBasicLandTypes(gd, gate))
                .containsExactlyInAnyOrder(CardSubtype.ISLAND, CardSubtype.MOUNTAIN);
        first.addMarkedDamage(null, 1);
        harness.runStateBasedActions();
        assertThat(gqs.effectiveBasicLandTypes(gd, gate)).containsExactly(CardSubtype.MOUNTAIN);
        harness.activateAbility(player1, 1, 1, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);

        second.addMarkedDamage(null, 1);
        harness.runStateBasedActions();
        assertThat(gqs.effectiveBasicLandTypes(gd, gate)).isEmpty();
        gate.setTapped(false);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Realmwright also requires a choice when entering without being cast")
    void enteringWithoutCastingRequiresChoice() {
        Permanent gate = harness.addToBattlefieldAndReturn(player1, new SimicGuildgate());
        harness.enterBattlefieldAndReturn(player1, new Realmwright());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "SWAMP");

        assertThat(gqs.effectiveBasicLandTypes(gd, gate)).containsExactly(CardSubtype.SWAMP);
        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }
}
