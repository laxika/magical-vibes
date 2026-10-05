package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.Colossapede;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SubmergedBoneyard;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OashraCultivator.class, Plains.class, Forest.class, Island.class, Colossapede.class, SubmergedBoneyard.class})
class OashraCultivatorTest extends BaseCardTest {

    @Test
    @DisplayName("Activating sacrifices Oashra Cultivator and puts the ability on the stack")
    void activatingSacrificesSelf() {
        addOashraReady(player1);
        addMana(player1);
        seedLibrary();

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Oashra Cultivator");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Resolving presents only basic lands with destination battlefield tapped")
    void resolvingPresentsBasicLandsToBattlefieldTapped() {
        addOashraReady(player1);
        addMana(player1);
        seedLibrary();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .hasSize(3)
                .allMatch(c -> c.hasType(CardType.LAND) && c.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().destination())
                .isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
    }

    @Test
    @DisplayName("Chosen basic land enters the battlefield tapped")
    void chosenBasicLandEntersTapped() {
        addOashraReady(player1);
        addMana(player1);
        seedLibrary();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().hasType(CardType.LAND) && p.isTapped());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Player may fail to find")
    void canFailToFind() {
        addOashraReady(player1);
        addMana(player1);
        seedLibrary();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().hasType(CardType.LAND));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("No basic lands in library does not prompt for a choice")
    void noBasicLandsNoPrompt() {
        addOashraReady(player1);
        addMana(player1);
        harness.setLibrary(player1, List.of(new Colossapede(), new Colossapede()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(entry -> entry.contains("finds no basic land cards"));
    }

    @Test
    @DisplayName("A summoning-sick Cultivator cannot pay the tap cost")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new OashraCultivator());
        findPermanent(player1, "Oashra Cultivator").setSummoningSick(true);
        addMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Oashra Cultivator");
        harness.assertNotInGraveyard(player1, "Oashra Cultivator");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Cultivator cannot activate")
    void cannotActivateWhileTapped() {
        addOashraReady(player1);
        findPermanent(player1, "Oashra Cultivator").setTapped(true);
        addMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Oashra Cultivator");
        harness.assertNotInGraveyard(player1, "Oashra Cultivator");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Insufficient mana does not sacrifice or tap the Cultivator")
    void cannotActivateWithoutEnoughMana() {
        addOashraReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Oashra Cultivator");
        assertThat(findPermanent(player1, "Oashra Cultivator").isTapped()).isFalse();
        harness.assertNotInGraveyard(player1, "Oashra Cultivator");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An empty library resolves without a choice or a land")
    void emptyLibraryResolves() {
        addOashraReady(player1);
        addMana(player1);
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Oashra Cultivator");
    }

    @Test
    @DisplayName("The search excludes nonbasic lands")
    void excludesNonbasicLands() {
        addOashraReady(player1);
        addMana(player1);
        Forest forest = new Forest();
        SubmergedBoneyard nonbasicLand = new SubmergedBoneyard();
        harness.setLibrary(player1, List.of(nonbasicLand, forest));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(forest);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonbasicLand);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void addOashraReady(Player player) {
        harness.addToBattlefield(player, new OashraCultivator());
        Permanent oashra = findPermanent(player, "Oashra Cultivator");
        oashra.setSummoningSick(false);
    }

    private void addMana(Player player) {
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }

    private void seedLibrary() {
        harness.setLibrary(player1, List.of(
                new Plains(), new Forest(), new Island(), new Colossapede()));
    }
}
