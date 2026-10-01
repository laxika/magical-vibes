package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AvenRiftwatcher;
import com.github.laxika.magicalvibes.cards.d.DuneriderOutlaw;
import com.github.laxika.magicalvibes.cards.m.MagusOfTheLibrary;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.u.UrborgTombOfYawgmoth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VoidstoneGargoyle.class, AvenRiftwatcher.class, DuneriderOutlaw.class,
        ProdigalPyromancer.class, MagusOfTheLibrary.class, UrborgTombOfYawgmoth.class})
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
        harness.setHand(player1, List.of(new VoidstoneGargoyle(), new AvenRiftwatcher()));
        addWhiteMana(5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Aven Riftwatcher");

        assertThat(findPermanent(player1, "Voidstone Gargoyle").getChosenName())
                .isEqualTo("Aven Riftwatcher");
    }

    @Test
    @DisplayName("The as-enters choice excludes land names")
    void cardNameChoiceExcludesLandNames() {
        harness.setHand(player1, List.of(
                new VoidstoneGargoyle(), new AvenRiftwatcher(), new UrborgTombOfYawgmoth()));
        addWhiteMana(5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.prompt()).isEqualTo("Choose a nonland card name.");
        assertThat(choice.options())
                .contains("Aven Riftwatcher")
                .doesNotContain("Urborg, Tomb of Yawgmoth");
    }

    @Test
    @DisplayName("Any qualifying nonland card name can be chosen")
    void cardNameChoiceIncludesNamesNotPresentInGame() {
        harness.setHand(player1, List.of(new VoidstoneGargoyle()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());
        addWhiteMana(5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).contains("Dunerider Outlaw");
    }

    @Test
    @DisplayName("No player can cast a spell with the chosen name")
    void noPlayerCanCastChosenName() {
        addReadyGargoyle(player1, "Aven Riftwatcher");

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new AvenRiftwatcher()));
        harness.addMana(player2, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new AvenRiftwatcher()));
        addWhiteMana(3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Spells with a different name can still be cast")
    void spellsWithDifferentNamesCanStillBeCast() {
        addReadyGargoyle(player1, "Aven Riftwatcher");

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DuneriderOutlaw()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Activated abilities of the named source cannot be activated")
    void blocksActivatedAbilitiesOfChosenName() {
        addReadyGargoyle(player1, "Prodigal Pyromancer");
        addCreatureReady(player2, new ProdigalPyromancer());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Mana abilities of the named source cannot be activated")
    void blocksManaAbilitiesOfChosenName() {
        addReadyGargoyle(player1, "Magus of the Library");
        addCreatureReady(player2, new MagusOfTheLibrary());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Activated abilities of a differently named source can still be activated")
    void allowsActivatedAbilitiesOfDifferentName() {
        addReadyGargoyle(player1, "Aven Riftwatcher");
        addCreatureReady(player2, new ProdigalPyromancer());

        harness.activateAbility(player2, 0, null, player1.getId());

        assertThat(gd.stack).hasSize(1);
    }

    private void addWhiteMana(int amount) {
        harness.addMana(player1, ManaColor.WHITE, amount);
    }

    private Permanent addReadyGargoyle(Player player, String chosenName) {
        Permanent perm = addCreatureReady(player, new VoidstoneGargoyle());
        perm.setChosenName(chosenName);
        return perm;
    }
}
