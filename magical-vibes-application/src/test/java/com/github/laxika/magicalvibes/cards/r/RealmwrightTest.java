package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
}
