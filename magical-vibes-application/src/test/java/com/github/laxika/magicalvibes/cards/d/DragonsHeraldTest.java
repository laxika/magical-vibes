package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CylianElf;
import com.github.laxika.magicalvibes.cards.b.BloodthornTaunter;
import com.github.laxika.magicalvibes.cards.h.HellkiteOverlord;
import com.github.laxika.magicalvibes.cards.s.SproutingThrinax;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DragonsHerald.class, DregscapeZombie.class, BloodthornTaunter.class,
        CylianElf.class, HellkiteOverlord.class, SproutingThrinax.class})
class DragonsHeraldTest extends BaseCardTest {

    private Permanent setUpHerald() {
        Permanent herald = addCreatureReady(player1, new DragonsHerald());
        harness.addMana(player1, ManaColor.RED, 3);
        return herald;
    }

    @Test
    @DisplayName("Cannot activate without a creature of each required color")
    void cannotActivateWithoutEachColor() {
        setUpHerald();
        harness.addToBattlefield(player1, new DregscapeZombie()); // black
        harness.addToBattlefield(player1, new BloodthornTaunter()); // red
        // No green creature.

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
    }

    @Test
    @DisplayName("Black sacrifice prompt only offers black creatures")
    void blackPromptOffersOnlyBlackCreatures() {
        setUpHerald();
        UUID zombiesId = harness.addToBattlefieldAndReturn(player1, new DregscapeZombie()).getId();
        harness.addToBattlefieldAndReturn(player1, new BloodthornTaunter());
        harness.addToBattlefieldAndReturn(player1, new CylianElf());

        harness.activateAbility(player1, 0, null, null);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactly(zombiesId);
    }

    @Test
    @DisplayName("Paying the cost sacrifices one black, one red, and one green creature")
    void payingSacrificesOneOfEachColor() {
        setUpHerald();
        UUID zombiesId = harness.addToBattlefieldAndReturn(player1, new DregscapeZombie()).getId();
        UUID giantId = harness.addToBattlefieldAndReturn(player1, new BloodthornTaunter()).getId();
        harness.addToBattlefieldAndReturn(player1, new CylianElf());

        harness.activateAbility(player1, 0, null, null);
        // Black pick, then red pick; the sole remaining green creature is paid automatically.
        harness.handlePermanentChosen(player1, zombiesId);
        harness.handlePermanentChosen(player1, giantId);

        harness.assertInGraveyard(player1, "Dregscape Zombie");
        harness.assertInGraveyard(player1, "Bloodthorn Taunter");
        harness.assertInGraveyard(player1, "Cylian Elf");
        // The Herald itself was not sacrificed and the ability is on the stack.
        harness.assertOnBattlefield(player1, "Dragon's Herald");
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Resolving searches for Hellkite Overlord by name and puts it onto the battlefield")
    void resolvingPutsHellkiteOverlordOntoBattlefield() {
        setUpHerald();
        UUID zombiesId = harness.addToBattlefieldAndReturn(player1, new DregscapeZombie()).getId();
        UUID giantId = harness.addToBattlefieldAndReturn(player1, new BloodthornTaunter()).getId();
        harness.addToBattlefieldAndReturn(player1, new CylianElf());

        harness.setLibrary(player1, List.of(new HellkiteOverlord(), new DregscapeZombie()));

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, zombiesId);
        harness.handlePermanentChosen(player1, giantId);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).hasSize(1).allMatch(c -> c.getName().equals("Hellkite Overlord"));

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Hellkite Overlord");
    }

    @Test
    @DisplayName("The Herald can be tapped and sacrificed as the red creature")
    void canSacrificeHeraldAndStillResolveAbility() {
        Permanent herald = setUpHerald();
        UUID blackId = harness.addToBattlefieldAndReturn(player1, new DregscapeZombie()).getId();
        harness.addToBattlefield(player1, new CylianElf());
        harness.setLibrary(player1, List.of(new HellkiteOverlord()));

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, blackId);
        harness.handlePermanentChosen(player1, herald.getId());

        harness.assertInGraveyard(player1, "Dragon's Herald");
        harness.assertInGraveyard(player1, "Dregscape Zombie");
        harness.assertInGraveyard(player1, "Cylian Elf");
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.assertOnBattlefield(player1, "Hellkite Overlord");
        assertThat(findPermanent(player1, "Hellkite Overlord").isTapped()).isFalse();
    }

    @Test
    @DisplayName("A three-colored creature cannot pay multiple sacrifice slots")
    void requiresThreeDistinctCreatures() {
        Permanent herald = setUpHerald();
        harness.addToBattlefield(player1, new SproutingThrinax());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
        assertThat(herald.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Sprouting Thrinax");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's black creature cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsCreatures() {
        setUpHerald();
        harness.addToBattlefield(player2, new DregscapeZombie());
        harness.addToBattlefield(player1, new BloodthornTaunter());
        harness.addToBattlefield(player1, new CylianElf());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
        harness.assertOnBattlefield(player2, "Dregscape Zombie");
    }

    @Test
    @DisplayName("A summoning-sick Herald cannot activate its tap ability")
    void cannotActivateWhileSummoningSick() {
        Permanent herald = setUpHerald();
        herald.setSummoningSick(true);
        harness.addToBattlefield(player1, new DregscapeZombie());
        harness.addToBattlefield(player1, new CylianElf());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(herald.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The controller may fail to find even when Hellkite Overlord is in the library")
    void mayFailToFindMatchingCard() {
        Permanent herald = setUpHerald();
        UUID blackId = harness.addToBattlefieldAndReturn(player1, new DregscapeZombie()).getId();
        harness.addToBattlefield(player1, new CylianElf());
        HellkiteOverlord overlord = new HellkiteOverlord();
        harness.setLibrary(player1, List.of(overlord));

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, blackId);
        harness.handlePermanentChosen(player1, herald.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertNotOnBattlefield(player1, "Hellkite Overlord");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(overlord);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability resolves normally when the library has no Hellkite Overlord")
    void resolvesWithoutMatchingCard() {
        Permanent herald = setUpHerald();
        UUID blackId = harness.addToBattlefieldAndReturn(player1, new DregscapeZombie()).getId();
        harness.addToBattlefield(player1, new CylianElf());
        CylianElf libraryCard = new CylianElf();
        harness.setLibrary(player1, List.of(libraryCard));

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, blackId);
        harness.handlePermanentChosen(player1, herald.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        harness.assertNotOnBattlefield(player1, "Hellkite Overlord");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A multicolored creature can pay the black sacrifice slot")
    void canSacrificeMulticoloredCreatureForOneColor() {
        Permanent herald = setUpHerald();
        UUID blackId = harness.addToBattlefieldAndReturn(player1, new HellkiteOverlord()).getId();
        harness.addToBattlefield(player1, new CylianElf());
        harness.setLibrary(player1, List.of(new HellkiteOverlord()));

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, blackId);
        harness.handlePermanentChosen(player1, herald.getId());

        harness.assertInGraveyard(player1, "Hellkite Overlord");
        harness.assertInGraveyard(player1, "Dragon's Herald");
        harness.assertInGraveyard(player1, "Cylian Elf");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.assertOnBattlefield(player1, "Hellkite Overlord");
    }

    @Test
    @DisplayName("Three generic mana cannot replace the red mana in the activation cost")
    void requiresRedMana() {
        Permanent herald = addCreatureReady(player1, new DragonsHerald());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addToBattlefield(player1, new DregscapeZombie());
        harness.addToBattlefield(player1, new CylianElf());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(herald.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Dregscape Zombie");
        harness.assertOnBattlefield(player1, "Cylian Elf");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An already tapped Herald cannot activate")
    void cannotActivateWhileTapped() {
        Permanent herald = setUpHerald();
        herald.tap();
        harness.addToBattlefield(player1, new DregscapeZombie());
        harness.addToBattlefield(player1, new CylianElf());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Dregscape Zombie");
        harness.assertOnBattlefield(player1, "Cylian Elf");
        assertThat(gd.stack).isEmpty();
    }
}
