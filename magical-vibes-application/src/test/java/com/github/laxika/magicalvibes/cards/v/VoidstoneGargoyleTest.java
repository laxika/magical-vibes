package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VoidstoneGargoyle.class, GrizzlyBears.class, HillGiant.class})
class VoidstoneGargoyleTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Voidstone Gargoyle awaits a nonland card name choice")
    void resolvingAwaitsCardNameChoice() {
        harness.setHand(player1, List.of(new VoidstoneGargoyle()));
        addWhiteMana(5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Voidstone Gargoyle");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
    }

    @Test
    @DisplayName("Choosing a card name records it on Voidstone Gargoyle")
    void choosingNameSetsOnPermanent() {
        harness.setHand(player1, List.of(new VoidstoneGargoyle()));
        addWhiteMana(5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Grizzly Bears");

        assertThat(findPermanent(player1, "Voidstone Gargoyle").getChosenName())
                .isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("No player can cast a spell with the chosen name")
    void noPlayerCanCastChosenName() {
        addReadyGargoyle(player1, "Grizzly Bears");

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Spells with a different name can still be cast")
    void spellsWithDifferentNamesCanStillBeCast() {
        addReadyGargoyle(player1, "Grizzly Bears");

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new HillGiant()));
        harness.addMana(player2, ManaColor.RED, 4);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Activated abilities of the named source cannot be activated")
    void blocksActivatedAbilitiesOfChosenName() {
        addReadyGargoyle(player1, "Prodigal Pyromancer");
        addPermanentWithTapAbility(player2, createCreatureWithTapAbility("Prodigal Pyromancer"));

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Mana abilities of the named source cannot be activated")
    void blocksManaAbilitiesOfChosenName() {
        addReadyGargoyle(player1, "Birds of Paradise");
        addPermanentWithTapAbility(player2, createCreatureWithManaAbility("Birds of Paradise"));

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Activated abilities of a differently named source can still be activated")
    void allowsActivatedAbilitiesOfDifferentName() {
        addReadyGargoyle(player1, "Some Other Card");
        addPermanentWithTapAbility(player2, createCreatureWithTapAbility("Prodigal Pyromancer"));

        harness.activateAbility(player2, 0, null, player1.getId());

        assertThat(gd.stack).hasSize(1);
    }

    private void addWhiteMana(int amount) {
        harness.addMana(player1, ManaColor.WHITE, amount);
    }

    private Permanent addReadyGargoyle(Player player, String chosenName) {
        Permanent perm = new Permanent(new VoidstoneGargoyle());
        perm.setChosenName(chosenName);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void addPermanentWithTapAbility(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
    }

    private static Card createCreatureWithTapAbility(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(CardColor.RED);
        card.setPower(1);
        card.setToughness(1);
        card.addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new DealDamageToAnyTargetEffect(1)),
                "{T}: " + name + " deals 1 damage to any target."
        ));
        return card;
    }

    private static Card createCreatureWithManaAbility(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(CardColor.GREEN);
        card.setPower(0);
        card.setToughness(1);
        card.addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new AwardAnyColorManaEffect()),
                "{T}: Add one mana of any color."
        ));
        return card;
    }
}
