package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CavernOfSoulsTest extends BaseCardTest {

    private static Card createCreature(String name, String manaCost, CardColor color, CardSubtype... subtypes) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost(manaCost);
        card.setColor(color);
        card.setPower(2);
        card.setToughness(2);
        card.setSubtypes(List.of(subtypes));
        return card;
    }

    private Permanent addCavern(CardSubtype chosenSubtype) {
        harness.addToBattlefield(player1, new CavernOfSouls());
        Permanent cavern = gd.playerBattlefields.get(player1.getId()).getLast();
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
        harness.setHand(player1, List.of(createCreature("Test Elf", "{G}", CardColor.GREEN, CardSubtype.ELF)));

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

        harness.ensurePriority(player2);
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

        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, bears.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }
}
