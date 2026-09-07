package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CouriersBriefcase.class, GrizzlyBears.class})
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
    }

    @Test
    @DisplayName("Paying all five colors and sacrificing it draws three cards")
    void sacrificesForThreeCards() {
        castBriefcase();
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        for (ManaColor color : List.of(ManaColor.WHITE, ManaColor.BLUE, ManaColor.BLACK, ManaColor.RED, ManaColor.GREEN)) {
            harness.addMana(player1, color, 1);
        }
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerHands.get(player1.getId())).hasSize(handSizeBefore + 3);
        harness.assertInGraveyard(player1, "Courier's Briefcase");
    }

    private void castBriefcase() {
        harness.setHand(player1, List.of(new CouriersBriefcase()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
