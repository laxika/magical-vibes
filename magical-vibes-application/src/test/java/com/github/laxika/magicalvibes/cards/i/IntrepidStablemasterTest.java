package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IntrepidStablemaster.class})
class IntrepidStablemasterTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability adds one green mana")
    void addsGreenMana() {
        Permanent stablemaster = addStablemaster();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(stablemaster.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("The second ability adds two mana of the chosen color for Mount or Vehicle spells")
    void addsMountOrVehicleSpellOnlyMana() {
        addStablemaster();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "RED");

        Card vehicle = testVehicle();
        harness.setHand(player1, List.of(vehicle));
        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("The restricted mana cannot cast a non-Mount, non-Vehicle spell")
    void restrictedManaCannotCastOtherSpells() {
        addStablemaster();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "RED");

        Card creature = testCreature();
        harness.setHand(player1, List.of(creature));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addStablemaster() {
        Permanent stablemaster = harness.addToBattlefieldAndReturn(player1, new IntrepidStablemaster());
        stablemaster.setSummoningSick(false);
        return stablemaster;
    }

    private Card testVehicle() {
        Card vehicle = new Card();
        vehicle.setName("Test Vehicle");
        vehicle.setType(CardType.ARTIFACT);
        vehicle.setSubtypes(List.of(CardSubtype.VEHICLE));
        vehicle.setManaCost("{2}");
        return vehicle;
    }

    private Card testCreature() {
        Card creature = new Card();
        creature.setName("Test Creature");
        creature.setType(CardType.CREATURE);
        creature.setManaCost("{2}");
        creature.setPower(2);
        creature.setToughness(2);
        return creature;
    }
}
