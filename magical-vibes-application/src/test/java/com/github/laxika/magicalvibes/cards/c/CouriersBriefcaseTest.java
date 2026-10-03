package com.github.laxika.magicalvibes.cards.c;

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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CouriersBriefcase.class})
class CouriersBriefcaseTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a green and white Citizen token")
    void entersWithCitizenToken() {
        castBriefcase();

        Permanent citizen = findPermanent(player1, "Citizen");
        assertThat(citizen.getCard().getPower()).isEqualTo(1);
        assertThat(citizen.getCard().getToughness()).isEqualTo(1);
        assertThat(citizen.getCard().getColors())
                .containsExactlyInAnyOrder(CardColor.GREEN, CardColor.WHITE);
        assertThat(citizen.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(citizen.getCard().getSubtypes()).containsExactly(CardSubtype.CITIZEN);
        assertThat(countPermanents(player1, "Citizen")).isEqualTo(1);
        assertThat(countPermanents(player2, "Citizen")).isZero();
    }

    @Test
    @DisplayName("Tapping and sacrificing it adds one mana of the chosen color")
    void sacrificesForAnyColorMana() {
        castBriefcase();
        Permanent briefcase = findPermanent(player1, "Courier's Briefcase");

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(briefcase);
        harness.assertInGraveyard(player1, "Courier's Briefcase");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Paying all five colors and sacrificing it draws three cards")
    void sacrificesForThreeCards() {
        castBriefcase();
        harness.setLibrary(player1, List.of(new CouriersBriefcase(), new CouriersBriefcase(), new CouriersBriefcase()));
        for (ManaColor color : List.of(ManaColor.WHITE, ManaColor.BLUE, ManaColor.BLACK, ManaColor.RED, ManaColor.GREEN)) {
            harness.addMana(player1, color, 1);
        }
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.assertInGraveyard(player1, "Courier's Briefcase");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 3);
        harness.assertInGraveyard(player1, "Courier's Briefcase");
    }

    @Test
    @DisplayName("Citizen trigger resolves even after the Briefcase is sacrificed for mana")
    void citizenTriggerSurvivesSacrifice() {
        harness.castFromHand(player1, new CouriersBriefcase(), "{1}{G}");
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Citizen")).isZero();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.WHITE.name());
        harness.assertInGraveyard(player1, "Courier's Briefcase");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);

        resolveAllTriggers();

        assertThat(countPermanents(player1, "Citizen")).isEqualTo(1);
    }

    @Test
    @DisplayName("Neither ability can be activated while the Briefcase is tapped")
    void tappedBriefcaseCannotActivate() {
        castBriefcase();
        Permanent briefcase = findPermanent(player1, "Courier's Briefcase");
        briefcase.tap();
        for (ManaColor color : List.of(ManaColor.WHITE, ManaColor.BLUE, ManaColor.BLACK, ManaColor.RED, ManaColor.GREEN)) {
            harness.addMana(player1, color, 1);
        }

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(briefcase);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Generic mana cannot replace a missing color in the draw ability's cost")
    void drawAbilityRequiresEveryColor() {
        castBriefcase();
        Permanent briefcase = findPermanent(player1, "Courier's Briefcase");
        for (ManaColor color : List.of(ManaColor.WHITE, ManaColor.BLUE, ManaColor.BLACK, ManaColor.RED, ManaColor.COLORLESS)) {
            harness.addMana(player1, color, 1);
        }

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(briefcase);
        assertThat(briefcase.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void castBriefcase() {
        harness.castFromHand(player1, new CouriersBriefcase(), "{1}{G}");
        resolveAllTriggers();
    }
}
