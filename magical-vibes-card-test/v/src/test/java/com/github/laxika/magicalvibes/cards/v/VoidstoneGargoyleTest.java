package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AvenRiftwatcher;
import com.github.laxika.magicalvibes.cards.d.DuneriderOutlaw;
import com.github.laxika.magicalvibes.cards.m.MagusOfTheLibrary;
import com.github.laxika.magicalvibes.cards.o.Ovinize;
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
        ProdigalPyromancer.class, MagusOfTheLibrary.class, UrborgTombOfYawgmoth.class, Ovinize.class})
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

    @Test
    @DisplayName("Submitting a land name is rejected without completing the choice")
    void rejectsLandNameSubmission() {
        harness.setHand(player1, List.of(new VoidstoneGargoyle(), new UrborgTombOfYawgmoth()));
        addWhiteMana(5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleListChoice(player1, "Urborg, Tomb of Yawgmoth"))
                .isInstanceOf(IllegalArgumentException.class);
        harness.assertNotOnBattlefield(player1, "Voidstone Gargoyle");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
    }

    @Test
    @DisplayName("Losing its abilities ends the restriction on nonmana abilities")
    void losingAbilitiesAllowsNamedNonmanaAbility() {
        Permanent gargoyle = addReadyGargoyle(player1, "Prodigal Pyromancer");
        addCreatureReady(player2, new ProdigalPyromancer());
        ovinize(gargoyle);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Losing its abilities ends the restriction on mana abilities")
    void losingAbilitiesAllowsNamedManaAbility() {
        Permanent gargoyle = addReadyGargoyle(player1, "Magus of the Library");
        Permanent magus = addCreatureReady(player2, new MagusOfTheLibrary());
        ovinize(gargoyle);

        harness.activateAbility(player2, 0, null, null);

        assertThat(magus.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Losing its abilities ends the spell restriction")
    void losingAbilitiesAllowsNamedSpell() {
        Permanent gargoyle = addReadyGargoyle(player1, "Aven Riftwatcher");
        ovinize(gargoyle);
        harness.setHand(player1, List.of(new AvenRiftwatcher()));
        addWhiteMana(3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Aven Riftwatcher");
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("The named source's triggered abilities remain active")
    void namedSourceStillTriggers() {
        addReadyGargoyle(player1, "Aven Riftwatcher");

        harness.enterBattlefieldAndReturn(player2, new AvenRiftwatcher());
        harness.passBothPriorities();

        harness.assertLife(player2, 22);
    }

    @Test
    @DisplayName("Leaving the battlefield ends the activated ability restriction")
    void leavingBattlefieldAllowsNamedAbility() {
        Permanent gargoyle = addReadyGargoyle(player1, "Prodigal Pyromancer");
        addCreatureReady(player2, new ProdigalPyromancer());
        gargoyle.setMarkedDamage(3);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Voidstone Gargoyle");

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    private void ovinize(Permanent gargoyle) {
        harness.setHand(player1, List.of(new Ovinize()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, gargoyle.getId());
    }

    @Test
    @DisplayName("The controller's named activated abilities are also restricted")
    void blocksControllersNamedAbility() {
        addReadyGargoyle(player2, "Prodigal Pyromancer");
        addCreatureReady(player2, new ProdigalPyromancer());

        assertThatThrownBy(() -> harness.activateAbility(player2, 1, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Leaving the battlefield ends the spell restriction")
    void leavingBattlefieldAllowsNamedSpell() {
        Permanent gargoyle = addReadyGargoyle(player1, "Dunerider Outlaw");
        gargoyle.setMarkedDamage(3);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Voidstone Gargoyle");
        harness.setHand(player1, List.of(new DuneriderOutlaw()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dunerider Outlaw");
    }

    private Permanent addReadyGargoyle(Player player, String chosenName) {
        Permanent perm = addCreatureReady(player, new VoidstoneGargoyle());
        perm.setChosenName(chosenName);
        return perm;
    }
}
