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

    private Permanent addEstate() {
        Permanent estate = harness.addToBattlefieldAndReturn(player1, new VoldarenEstate());
        estate.setSummoningSick(false);
        return estate;
    }
}
