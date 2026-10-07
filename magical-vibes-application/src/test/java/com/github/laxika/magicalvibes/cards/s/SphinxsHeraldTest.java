package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SphinxsHerald.class, SuntailHawk.class, FugitiveWizard.class, ScatheZombies.class,
        SphinxSovereign.class})
class SphinxsHeraldTest extends BaseCardTest {

    private Permanent setUpHerald() {
        Permanent herald = harness.addToBattlefieldAndReturn(player1, new SphinxsHerald());
        herald.setSummoningSick(false);
        harness.addMana(player1, ManaColor.BLUE, 3);
        return herald;
    }

    @Test
    @DisplayName("Cannot activate without a creature of each required color")
    void cannotActivateWithoutEachColor() {
        setUpHerald();
        harness.addToBattlefield(player1, new SuntailHawk());   // white
        harness.addToBattlefield(player1, new FugitiveWizard()); // blue
        // No black creature.

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
    }

    @Test
    @DisplayName("White sacrifice prompt only offers white creatures")
    void whitePromptOffersOnlyWhiteCreatures() {
        setUpHerald();
        UUID hawkId = harness.addToBattlefieldAndReturn(player1, new SuntailHawk()).getId();
        harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());
        harness.addToBattlefieldAndReturn(player1, new ScatheZombies());

        harness.activateAbility(player1, 0, null, null);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactly(hawkId);
    }

    @Test
    @DisplayName("Paying the cost sacrifices one white, one blue, and one black creature")
    void payingSacrificesOneOfEachColor() {
        setUpHerald();
        UUID hawkId = harness.addToBattlefieldAndReturn(player1, new SuntailHawk()).getId();
        UUID wizardId = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard()).getId();
        harness.addToBattlefieldAndReturn(player1, new ScatheZombies());

        harness.activateAbility(player1, 0, null, null);
        // White pick, then blue pick; the sole remaining black creature is paid automatically.
        harness.handlePermanentChosen(player1, hawkId);
        harness.handlePermanentChosen(player1, wizardId);

        harness.assertInGraveyard(player1, "Suntail Hawk");
        harness.assertInGraveyard(player1, "Fugitive Wizard");
        harness.assertInGraveyard(player1, "Scathe Zombies");
        // The Herald itself was not sacrificed and the ability is on the stack.
        harness.assertOnBattlefield(player1, "Sphinx's Herald");
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Resolving searches for Sphinx Sovereign by name and puts it onto the battlefield")
    void resolvingPutsSphinxSovereignOntoBattlefield() {
        setUpHerald();
        UUID hawkId = harness.addToBattlefieldAndReturn(player1, new SuntailHawk()).getId();
        UUID wizardId = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard()).getId();
        harness.addToBattlefieldAndReturn(player1, new ScatheZombies());

        harness.setLibrary(player1, List.of(new SphinxSovereign(), new ScatheZombies()));

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, hawkId);
        harness.handlePermanentChosen(player1, wizardId);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).allMatch(c -> c.getName().equals("Sphinx Sovereign"));

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Sphinx Sovereign");
    }

    @Test
    @DisplayName("The tapped Herald can itself pay the blue sacrifice")
    void canSacrificeHeraldForBlue() {
        Permanent herald = setUpHerald();
        UUID whiteId = harness.addToBattlefieldAndReturn(player1, new SphinxSovereign()).getId();
        harness.addToBattlefield(player1, new SphinxSovereign());
        harness.setLibrary(player1, List.of(new SphinxSovereign()));

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, whiteId);
        harness.handlePermanentChosen(player1, herald.getId());

        harness.assertInGraveyard(player1, "Sphinx's Herald");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.assertOnBattlefield(player1, "Sphinx Sovereign");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A multicolored creature cannot pay more than one sacrifice")
    void needsThreeDistinctCreatures() {
        setUpHerald();
        harness.addToBattlefield(player1, new SphinxSovereign());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
        harness.assertOnBattlefield(player1, "Sphinx Sovereign");
        harness.assertOnBattlefield(player1, "Sphinx's Herald");
    }

    @Test
    @DisplayName("The search can decline to find an available Sphinx Sovereign")
    void canFailToFind() {
        Permanent herald = setUpHerald();
        UUID whiteId = harness.addToBattlefieldAndReturn(player1, new SphinxSovereign()).getId();
        harness.addToBattlefield(player1, new SphinxSovereign());
        SphinxSovereign sovereign = new SphinxSovereign();
        harness.setLibrary(player1, List.of(sovereign));

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, whiteId);
        harness.handlePermanentChosen(player1, herald.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertNotOnBattlefield(player1, "Sphinx Sovereign");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(sovereign);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An empty library does not undo the sacrifices")
    void emptyLibraryStillPaysCosts() {
        Permanent herald = setUpHerald();
        UUID whiteId = harness.addToBattlefieldAndReturn(player1, new SphinxSovereign()).getId();
        harness.addToBattlefield(player1, new SphinxSovereign());
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, whiteId);
        harness.handlePermanentChosen(player1, herald.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        harness.assertNotOnBattlefield(player1, "Sphinx Sovereign");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

}
