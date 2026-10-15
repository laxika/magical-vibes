package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LeylineOfSanctity;
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
    @DisplayName("Does not deal damage for a token of the chosen color")
    void excludesMatchingColorTokensFromDamageCount() {
        castAndChooseBlue();
        harness.addToBattlefield(player2, new AirElemental());
        AirElemental token = new AirElemental();
        token.setToken(true);
        harness.addToBattlefield(player2, token);
        harness.setLife(player2, 10);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(9);
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

    @Test
    @DisplayName("Destroys itself when its controller declines to sacrifice two Islands")
    void destroysItselfWhenSacrificeIsDeclined() {
        castAndChooseBlue();
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Psychic Allergy");
        harness.assertInGraveyard(player1, "Psychic Allergy");
    }

    @Test
    @DisplayName("Counts matching enchantments as well as creatures, but not its controller's permanents or colorless Islands")
    void countsAllOpponentPermanentTypesOnly() {
        castAndChooseBlue();
        harness.addToBattlefield(player1, new AirElemental());
        harness.addToBattlefield(player2, new PsychicAllergy());
        harness.addToBattlefield(player2, new AirElemental());
        harness.addToBattlefield(player2, new Island());
        harness.setLife(player2, 10);

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 8);
        harness.assertOnBattlefield(player1, "Psychic Allergy");
    }

    @Test
    @DisplayName("Counts matching permanents when the damage trigger resolves")
    void countsPermanentsAtResolution() {
        castAndChooseBlue();
        harness.addToBattlefield(player2, new AirElemental());
        harness.setLife(player2, 10);

        advanceToUpkeep(player2);
        harness.addToBattlefield(player2, new AirElemental());
        harness.passBothPriorities();

        harness.assertLife(player2, 8);
    }

    @Test
    @DisplayName("Choosing green counts green permanents instead of blue permanents")
    void canChooseAnotherColor() {
        harness.setHand(player1, List.of(new PsychicAllergy()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardColor.GREEN.name());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new AirElemental());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setLife(player2, 10);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player2, 9);
    }

    @Test
    @DisplayName("Deals no damage when the opponent controls no matching permanents")
    void dealsNoDamageWithoutMatchingPermanents() {
        castAndChooseBlue();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Island());
        harness.setLife(player2, 10);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player2, 10);
    }

    @Test
    @DisplayName("A single Island cannot pay the upkeep cost and is not sacrificed")
    void doesNotPartiallyPayUpkeepCost() {
        castAndChooseBlue();
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Island());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Psychic Allergy");
        harness.assertNotOnBattlefield(player1, "Psychic Allergy");
        harness.assertOnBattlefield(player1, "Island");
        harness.assertOnBattlefield(player2, "Island");
    }

    @Test
    @DisplayName("The controller chooses exactly two Islands when three are available")
    void choosesTwoIslandsFromThree() {
        castAndChooseBlue();
        var first = harness.addToBattlefieldAndReturn(player1, new Island());
        var second = harness.addToBattlefieldAndReturn(player1, new Island());
        var third = harness.addToBattlefieldAndReturn(player1, new Island());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), third.getId()));

        harness.assertOnBattlefield(player1, "Psychic Allergy");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Island"))
                .extracting(permanent -> permanent.getId())
                .containsExactly(second.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Island"))
                .hasSize(2);
    }

    @Test
    @CardUsed(LeylineOfSanctity.class)
    @DisplayName("Opponent hexproof does not stop the nontargeting upkeep damage")
    void dealsDamageToOpponentWithHexproof() {
        castAndChooseBlue();
        harness.addToBattlefield(player2, new LeylineOfSanctity());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setLife(player2, 10);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player2, 9);
    }
}
