package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.CaptivatingVampire;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VoldarenEstate.class, CaptivatingVampire.class, RagingGoblin.class, VoldarenEpicure.class})
class VoldarenEstateTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability adds one colorless mana")
    void addsColorlessMana() {
        Permanent estate = addEstate();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(estate.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("The second ability pays 1 life and adds Vampire-only mana")
    void addsVampireOnlyMana() {
        addEstate();
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getSubtypeSpellOnlyManaTotal(Set.of(CardSubtype.VAMPIRE))).isEqualTo(1);

        harness.setHand(player1, List.of(new VoldarenEpicure()));
        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId())
                .getSubtypeSpellOnlyManaTotal(Set.of(CardSubtype.VAMPIRE))).isZero();
    }

    @Test
    @DisplayName("The second ability's mana cannot cast a non-Vampire spell")
    void cannotCastNonVampireSpellWithRestrictedMana() {
        addEstate();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "RED");
        harness.setHand(player1, List.of(new RagingGoblin()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The Blood ability costs one less for each Vampire controlled")
    void bloodAbilityCostIsReducedPerVampire() {
        addEstate();
        harness.addToBattlefield(player1, new CaptivatingVampire());
        harness.addToBattlefield(player1, new CaptivatingVampire());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Blood")).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Without Vampires the Blood ability costs five mana and uses the stack")
    void bloodAbilityPaysFullCostAndUsesStack() {
        Permanent estate = addEstate();
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 2, null, null);

        assertThat(estate.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(findPermanents(player1, "Blood")).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Blood")).hasSize(1);
    }

    @Test
    @DisplayName("Opposing Vampires do not reduce the Blood ability's cost")
    void opposingVampiresDoNotReduceCost() {
        Permanent estate = addEstate();
        harness.addToBattlefield(player2, new VoldarenEpicure());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(estate.isTapped()).isFalse();
        assertThat(findPermanents(player1, "Blood")).isEmpty();
    }

    @Test
    @DisplayName("Six Vampires reduce the Blood activation cost to zero")
    void discountCannotReduceCostBelowZero() {
        Permanent estate = addEstate();
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player1, new VoldarenEpicure());
        }

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(estate.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Blood")).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Vampire-only mana cannot pay the Blood activation's generic cost")
    void restrictedManaCannotPayActivationCost() {
        Permanent manaEstate = addEstate();
        Permanent bloodEstate = addEstate();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "RED");

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 2, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(manaEstate.isTapped()).isTrue();
        assertThat(bloodEstate.isTapped()).isFalse();
        assertThat(findPermanents(player1, "Blood")).isEmpty();
    }

    @Test
    @DisplayName("A tapped Estate cannot activate again")
    void cannotActivateTappedEstate() {
        addEstate();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player1, "Blood")).isEmpty();
    }

    private Permanent addEstate() {
        return addCreatureReady(player1, new VoldarenEstate());
    }
}
