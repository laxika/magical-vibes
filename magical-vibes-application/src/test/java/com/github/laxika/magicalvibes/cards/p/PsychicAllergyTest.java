package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PsychicAllergy.class, AirElemental.class, GrizzlyBears.class, Island.class})
class PsychicAllergyTest extends BaseCardTest {

    private void castAndChooseBlue() {
        harness.setHand(player1, List.of(new PsychicAllergy()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardColor.BLUE.name());
    }

    @Test
    @DisplayName("Deals damage for the opponent's nontoken permanents of the chosen color")
    void dealsDamageForChosenColorNontokenPermanents() {
        castAndChooseBlue();
        harness.addToBattlefield(player2, new AirElemental());
        harness.addToBattlefield(player2, new AirElemental());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player2, 10);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(8);
    }

    @Test
    @DisplayName("Destroys itself when its controller cannot sacrifice two Islands")
    void destroysItselfWithoutTwoIslands() {
        castAndChooseBlue();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Psychic Allergy");
        harness.assertInGraveyard(player1, "Psychic Allergy");
    }

    @Test
    @DisplayName("Sacrificing two Islands keeps Psychic Allergy on the battlefield")
    void sacrificingTwoIslandsKeepsItOnBattlefield() {
        castAndChooseBlue();
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Psychic Allergy");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Island"));
    }
}
