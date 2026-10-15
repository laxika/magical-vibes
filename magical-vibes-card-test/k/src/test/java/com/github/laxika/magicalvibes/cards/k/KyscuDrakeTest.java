package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.s.SpittingDrake;
import com.github.laxika.magicalvibes.cards.s.SoulSculptor;
import com.github.laxika.magicalvibes.cards.v.ViashivanDragon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KyscuDrake.class, SpittingDrake.class, ViashivanDragon.class, SoulSculptor.class})
class KyscuDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability gives +0/+1 until end of turn")
    void resolvingAbilityBoostsSelf() {
        Permanent drake = addCreatureReady(player1, new KyscuDrake());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(drake.getPowerModifier()).isEqualTo(0);
        assertThat(drake.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate pump more than once each turn")
    void cannotActivatePumpMoreThanOncePerTurn() {
        addCreatureReady(player1, new KyscuDrake());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Pump resets at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent drake = addCreatureReady(player1, new KyscuDrake());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(drake.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(drake.getPowerModifier()).isEqualTo(0);
        assertThat(drake.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Tutor ability can't be activated without Spitting Drake")
    void tutorRequiresSpittingDrake() {
        addCreatureReady(player1, new KyscuDrake());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
    }

    @Test
    @DisplayName("Tutor ability sacrifices the activating Drake rather than another Kyscu Drake")
    void tutorSacrificesActivatingDrake() {
        Permanent source = addCreatureReady(player1, new KyscuDrake());
        Permanent otherDrake = addCreatureReady(player1, new KyscuDrake());
        UUID spittingId = harness.addToBattlefieldAndReturn(player1, new SpittingDrake()).getId();
        harness.setLibrary(player1, List.of(new ViashivanDragon()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handlePermanentChosen(player1, spittingId);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId)
                .containsExactly(otherDrake.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Kyscu Drake", "Spitting Drake");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(source.getOriginalCard().getId());

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Viashivan Dragon");
    }

    @Test
    @DisplayName("Sacrificing with Spitting Drake searches out Viashivan Dragon onto the battlefield")
    void tutorPutsViashivanDragonOntoBattlefield() {
        addCreatureReady(player1, new KyscuDrake());
        UUID spittingId = harness.addToBattlefieldAndReturn(player1, new SpittingDrake()).getId();

        harness.setLibrary(player1, List.of(new ViashivanDragon()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handlePermanentChosen(player1, spittingId);

        harness.assertInGraveyard(player1, "Kyscu Drake");
        harness.assertInGraveyard(player1, "Spitting Drake");

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).allMatch(c -> c.getName().equals("Viashivan Dragon"));

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Viashivan Dragon");
    }

    @Test
    @DisplayName("Pump limit applies before the first activation resolves")
    void cannotActivatePumpAgainWhileOnStack() {
        addCreatureReady(player1, new KyscuDrake());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("An opponent's Spitting Drake cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsSpittingDrake() {
        addCreatureReady(player1, new KyscuDrake());
        addCreatureReady(player2, new SpittingDrake());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
        harness.assertOnBattlefield(player1, "Kyscu Drake");
        harness.assertOnBattlefield(player2, "Spitting Drake");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The named search may fail to find even when Viashivan Dragon is present")
    void canDeclineToFindDragon() {
        addCreatureReady(player1, new KyscuDrake());
        UUID spittingId = harness.addToBattlefieldAndReturn(player1, new SpittingDrake()).getId();
        harness.setLibrary(player1, List.of(new ViashivanDragon(), new SpittingDrake()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handlePermanentChosen(player1, spittingId);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertNotOnBattlefield(player1, "Viashivan Dragon");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Viashivan Dragon", "Spitting Drake");
        harness.assertInGraveyard(player1, "Kyscu Drake");
        harness.assertInGraveyard(player1, "Spitting Drake");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Spitting Drake must still be a creature to pay the sacrifice cost")
    void cannotSacrificeSpittingDrakeThatBecameEnchantment() {
        addCreatureReady(player1, new KyscuDrake());
        Permanent spitting = addCreatureReady(player1, new SpittingDrake());
        addCreatureReady(player1, new SoulSculptor());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 2, null, spitting.getId());
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, spitting)).isFalse();
        assertThat(gqs.isEnchantment(gd, spitting)).isTrue();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
        harness.assertOnBattlefield(player1, "Kyscu Drake");
        harness.assertOnBattlefield(player1, "Spitting Drake");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Pump can be activated again on the opponent's next turn")
    void pumpLimitResetsOnNextTurn() {
        Permanent drake = addCreatureReady(player1, new KyscuDrake());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(drake.getToughnessModifier()).isEqualTo(0);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(drake.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("The sacrifice cost is paid even when the library is empty")
    void tutorWithEmptyLibraryStillSacrificesBothDrakes() {
        addCreatureReady(player1, new KyscuDrake());
        UUID spittingId = harness.addToBattlefieldAndReturn(player1, new SpittingDrake()).getId();
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handlePermanentChosen(player1, spittingId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Kyscu Drake");
        harness.assertInGraveyard(player1, "Spitting Drake");
        harness.assertNotOnBattlefield(player1, "Viashivan Dragon");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
