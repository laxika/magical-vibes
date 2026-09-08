package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.b.Breathstealer;
import com.github.laxika.magicalvibes.cards.f.FeralShadow;
import com.github.laxika.magicalvibes.cards.s.SpiritOfTheNight;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UrborgPanther.class, FeralShadow.class, Breathstealer.class, SpiritOfTheNight.class})
class UrborgPantherTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing the Panther destroys the creature blocking it")
    void sacrificeDestroysBlocker() {
        addCreatureReady(player1, new UrborgPanther());
        Permanent blocker = addCreatureReady(player2, new FeralShadow());

        blockPantherWith(0);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, blocker.getId());

        // The sacrifice is a cost — the Panther is already gone while the ability is on the stack.
        harness.assertInGraveyard(player1, "Urborg Panther");

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Feral Shadow");
    }

    @Test
    @DisplayName("A creature that isn't blocking the Panther is an illegal target")
    void nonBlockingCreatureCannotBeTargeted() {
        addCreatureReady(player1, new UrborgPanther());
        addCreatureReady(player2, new FeralShadow());
        Permanent bystander = addCreatureReady(player2, new FeralShadow());

        blockPantherWith(0);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bystander.getId()))
              .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The tutor requires the named permanents to still be creatures")
    void tutorRequiresNamedCreaturesToRemainCreatures() {
        addCreatureReady(player1, new UrborgPanther());
        FeralShadow shadowCard = new FeralShadow();
        shadowCard.setType(CardType.LAND);
        harness.addToBattlefield(player1, shadowCard);
        harness.addToBattlefield(player1, new Breathstealer());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
    }

    @Test
    @DisplayName("The tutor ability can't be activated without both named creatures")
    void tutorRequiresBothNamedCreatures() {
        addCreatureReady(player1, new UrborgPanther());
        harness.addToBattlefield(player1, new FeralShadow());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
    }

    @Test
    @DisplayName("Sacrificing the trio searches out Spirit of the Night onto the battlefield")
    void tutorPutsSpiritOfTheNightOntoBattlefield() {
        addCreatureReady(player1, new UrborgPanther());
        UUID shadowId = harness.addToBattlefieldAndReturn(player1, new FeralShadow()).getId();
        UUID breathstealerId = harness.addToBattlefieldAndReturn(player1, new Breathstealer()).getId();

        harness.setLibrary(player1, List.of(new SpiritOfTheNight(), new FeralShadow()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handlePermanentChosen(player1, shadowId);
        harness.handlePermanentChosen(player1, breathstealerId);

        harness.assertInGraveyard(player1, "Feral Shadow");
        harness.assertInGraveyard(player1, "Breathstealer");
        harness.assertInGraveyard(player1, "Urborg Panther");

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).allMatch(c -> c.getName().equals("Spirit of the Night"));

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertOnBattlefield(player1, "Spirit of the Night");
    }

    @Test
    @DisplayName("The tutor leaves the battlefield unchanged when Spirit of the Night is not found")
    void tutorDoesNothingWhenSpiritOfTheNightIsNotInLibrary() {
        addCreatureReady(player1, new UrborgPanther());
        UUID shadowId = harness.addToBattlefieldAndReturn(player1, new FeralShadow()).getId();
        UUID breathstealerId = harness.addToBattlefieldAndReturn(player1, new Breathstealer()).getId();

        harness.setLibrary(player1, List.of(new FeralShadow()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handlePermanentChosen(player1, shadowId);
        harness.handlePermanentChosen(player1, breathstealerId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        harness.assertNotOnBattlefield(player1, "Spirit of the Night");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Feral Shadow");
    }

    /** Attacks with the Panther and blocks it with player2's creature at {@code blockerIndex}. */
    private void blockPantherWith(int blockerIndex) {
        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, 0)));
        resolveAllTriggers();
    }
}
