package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheEverflowingWell.class, TheMyriadPools.class, Forest.class, GrizzlyBears.class,
        Shock.class, Spellbook.class, Cancel.class})
class TheEverflowingWellTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by milling two cards and drawing two cards")
    void entersMillsAndDraws() {
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Shock()));
        harness.setHand(player1, List.of(new TheEverflowingWell()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertOnBattlefield(player1, "The Everflowing Well");
    }

    @Test
    @DisplayName("Transforms during upkeep when the controller has eight permanent cards in their graveyard")
    void transformsWithDescendEight() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new Forest(), new Forest(), new Forest(), new Forest()));
        Permanent well = harness.addToBattlefieldAndReturn(player1, new TheEverflowingWell());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(well.getCard()).isInstanceOf(TheMyriadPools.class);
    }

    @Test
    @DisplayName("Copies a permanent spell onto another permanent when its mana pays for that spell")
    void copiesPermanentSpellUsingProducedMana() {
        Permanent pools = harness.addToBattlefieldAndReturn(player1, new TheMyriadPools());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(target.getId())
                .doesNotContain(pools.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isCopyUntilEndOfTurn()).isTrue();
        assertThat(target.getCard().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    void nonpermanentCardsDoNotCountForDescend() {
        harness.setGraveyard(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), new Shock()));
        Permanent well = harness.addToBattlefieldAndReturn(player1, new TheEverflowingWell());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(well.getCard()).isInstanceOf(TheEverflowingWell.class);
    }

    @Test
    void descendConditionIsCheckedAgainOnResolution() {
        harness.setGraveyard(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), new Forest()));
        Permanent well = harness.addToBattlefieldAndReturn(player1, new TheEverflowingWell());

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.setGraveyard(player1, List.of());
        resolveAllTriggers();

        assertThat(well.getCard()).isInstanceOf(TheEverflowingWell.class);
    }

    @Test
    void doesNotTransformDuringOpponentsUpkeep() {
        harness.setGraveyard(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), new Forest()));
        Permanent well = harness.addToBattlefieldAndReturn(player1, new TheEverflowingWell());

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(well.getCard()).isInstanceOf(TheEverflowingWell.class);
    }

    @Test
    void permanentSpellWithoutPoolsManaDoesNotTrigger() {
        harness.addToBattlefield(player1, new TheMyriadPools());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(target.getCard()).isInstanceOf(Spellbook.class);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void instantUsingPoolsManaDoesNotTrigger() {
        harness.addToBattlefield(player1, new TheMyriadPools());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.setHand(player1, List.of(new Cancel()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, gd.stack.getFirst().getCard().getId());
        resolveAllTriggers();

        assertThat(target.getCard()).isInstanceOf(Spellbook.class);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void copyStillResolvesAfterTriggeringSpellIsCountered() {
        harness.addToBattlefield(player1, new TheMyriadPools());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.activateAbility(player1, 0, 0, null, null);
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(target.getCard().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    void copyExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new TheMyriadPools());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();
        assertThat(target.getCard().getName()).isEqualTo("Grizzly Bears");

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(target.getCard()).isInstanceOf(Spellbook.class);
    }
}
