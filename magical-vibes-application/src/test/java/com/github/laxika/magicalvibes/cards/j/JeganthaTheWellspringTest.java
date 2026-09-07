package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JeganthaTheWellspring.class, GrizzlyBears.class, LlanowarElves.class})
class JeganthaTheWellspringTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Jegantha adds one mana of each color with the generic-cost restriction")
    void tappingAddsRestrictedManaOfEachColor() {
        activateManaAbility();

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.getColoredCostOnlyMana(ManaColor.WHITE)).isEqualTo(1);
        assertThat(pool.getColoredCostOnlyMana(ManaColor.BLUE)).isEqualTo(1);
        assertThat(pool.getColoredCostOnlyMana(ManaColor.BLACK)).isEqualTo(1);
        assertThat(pool.getColoredCostOnlyMana(ManaColor.RED)).isEqualTo(1);
        assertThat(pool.getColoredCostOnlyMana(ManaColor.GREEN)).isEqualTo(1);
        assertThat(pool.getTotal()).isZero();
    }

    @Test
    @DisplayName("Restricted mana pays a colored cost")
    void restrictedManaPaysColoredCost() {
        activateManaAbility();
        harness.setHand(player1, List.of(new LlanowarElves()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        assertThat(pool().getColoredCostOnlyMana(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Restricted mana cannot pay a generic cost")
    void restrictedManaCannotPayGenericCost() {
        activateManaAbility();
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(pool().getColoredCostOnlyManaTotal()).isEqualTo(5);
    }

    @Test
    @DisplayName("Restricted mana pays a colored hybrid alternative but not a generic cost")
    void restrictedManaPaysHybridAlternative() {
        activateManaAbility();
        Card hybridCreature = new Card();
        hybridCreature.setName("Test Hybrid Creature");
        hybridCreature.setType(CardType.CREATURE);
        hybridCreature.setColor(CardColor.GREEN);
        hybridCreature.setManaCost("{1}{2/G}");
        hybridCreature.setPower(1);
        hybridCreature.setToughness(1);
        harness.setHand(player1, List.of(hybridCreature));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(hybridCreature.getId()));
    }

    private void activateManaAbility() {
        Permanent jegantha = harness.addToBattlefieldAndReturn(player1, new JeganthaTheWellspring());
        jegantha.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, null);
    }

    private ManaPool pool() {
        return gd.playerManaPools.get(player1.getId());
    }
}
