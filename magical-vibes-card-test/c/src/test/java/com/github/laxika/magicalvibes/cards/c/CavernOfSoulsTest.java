package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AbundantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WanderingWolf;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CavernOfSouls.class, GrizzlyBears.class, Cancel.class, WanderingWolf.class, AbundantGrowth.class})
class CavernOfSoulsTest extends BaseCardTest {

    private Permanent addCavern(CardSubtype chosenSubtype) {
        Permanent cavern = harness.addToBattlefieldAndReturn(player1, new CavernOfSouls());
        cavern.setChosenSubtype(chosenSubtype);
        return cavern;
    }

    @Test
    @DisplayName("First ability adds colorless mana")
    void tappingForColorlessMana() {
        Permanent cavern = addCavern(CardSubtype.BEAR);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(cavern.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Second ability prompts for a color and adds chosen-type creature mana")
    void secondAbilityAddsRestrictedMana() {
        addCavern(CardSubtype.MERFOLK);

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "BLUE");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.BLUE)).isEqualTo(0);
        assertThat(pool.getSubtypeCreatureManaForColor(Set.of(CardSubtype.MERFOLK), ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Mana can't be spent on a creature spell of a different type")
    void manaCannotCastCreatureOfDifferentType() {
        addCavern(CardSubtype.VAMPIRE);

        gd.playerManaPools.get(player1.getId())
                .addSubtypeCreatureMana(CardSubtype.VAMPIRE, ManaColor.GREEN, 1, true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new WanderingWolf()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A creature spell of the chosen type paid for with Cavern mana can't be countered")
    void spellPaidWithCavernManaCannotBeCountered() {
        addCavern(CardSubtype.BEAR);
        addCavern(CardSubtype.BEAR);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "GREEN");
        harness.activateAbility(player1, 1, 1, null, null);
        harness.handleListChoice(player1, "GREEN");

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));

        Cancel cancel = new Cancel();
        harness.setHand(player2, List.of(cancel));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.forceActivePlayer(player1);
        harness.castCreature(player1, 0);
        assertThat(gd.spellsMadeUncounterable).contains(bears.getId());

        harness.castInstant(player2, 0, bears.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Cancel");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The same creature spell paid for with normal mana is countered")
    void spellPaidWithNormalManaIsCountered() {
        addCavern(CardSubtype.BEAR);
        harness.addMana(player1, ManaColor.GREEN, 2);

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));

        Cancel cancel = new Cancel();
        harness.setHand(player2, List.of(cancel));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.forceActivePlayer(player1);
        harness.castCreature(player1, 0);
        assertThat(gd.spellsMadeUncounterable).doesNotContain(bears.getId());

        harness.castInstant(player2, 0, bears.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Playing Cavern chooses a creature type without using the stack")
    void choosesCreatureTypeAsLandEnters() {
        harness.setHand(player1, List.of(new CavernOfSouls()));

        harness.playLand(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.stack).isEmpty();
        harness.handleListChoice(player1, "WOLF");

        Permanent cavern = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(cavern.getChosenSubtype()).isEqualTo(CardSubtype.WOLF);
        assertThat(gd.stack).isEmpty();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "GREEN");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new WanderingWolf()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Wandering Wolf");
    }

    @Test
    @DisplayName("Restricted mana cannot pay for a noncreature spell")
    void restrictedManaCannotCastNoncreatureSpell() {
        Permanent cavern = addCavern(CardSubtype.WOLF);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "GREEN");
        harness.setHand(player1, List.of(new AbundantGrowth()));

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, cavern.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Abundant Growth");
    }

    @Test
    @DisplayName("Cavern mana spent on a generic cost also prevents countering")
    void restrictedManaPaysGenericCostAndPreventsCountering() {
        addCavern(CardSubtype.WOLF);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "BLUE");
        harness.addMana(player1, ManaColor.GREEN, 1);
        WanderingWolf wolf = new WanderingWolf();
        harness.setHand(player1, List.of(wolf));
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.castInstant(player2, 0, wolf.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Wandering Wolf");
        harness.assertInGraveyard(player2, "Cancel");
        assertThat(gd.playerManaPools.get(player1.getId())
                .getSubtypeCreatureManaForColor(Set.of(CardSubtype.WOLF), ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Colorless mana from Cavern does not prevent countering")
    void colorlessAbilityDoesNotPreventCountering() {
        addCavern(CardSubtype.WOLF);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.addMana(player1, ManaColor.GREEN, 1);
        WanderingWolf wolf = new WanderingWolf();
        harness.setHand(player1, List.of(wolf));
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.castInstant(player2, 0, wolf.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Wandering Wolf");
        harness.assertNotOnBattlefield(player1, "Wandering Wolf");
        assertThat(gd.stack).isEmpty();
    }
}
