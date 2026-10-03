package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.f.FiresOfUndeath;
import com.github.laxika.magicalvibes.cards.h.HeadlessSkaab;
import com.github.laxika.magicalvibes.cards.t.TragicSlip;
import com.github.laxika.magicalvibes.cards.y.YoungWolf;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Counterlash.class, Divination.class, GrizzlyBears.class, HillGiant.class,
        LightningBolt.class, FiresOfUndeath.class, HeadlessSkaab.class, TragicSlip.class, YoungWolf.class})
class CounterlashTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack targeting a spell")
    void castingPutsOnStackTargetingSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Counterlash()));
        harness.addMana(player2, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(2);
        StackEntry counterlashEntry = gd.stack.getLast();
        assertThat(counterlashEntry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(counterlashEntry.getCard().getName()).isEqualTo("Counterlash");
        assertThat(counterlashEntry.getTargetId()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Counters target spell and offers may cast for a creature sharing card type")
    void countersAndOffersMayCastCreature() {
        GrizzlyBears bears = new GrizzlyBears();
        HillGiant hillGiant = new HillGiant();

        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Counterlash(), hillGiant));
        harness.addMana(player2, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        GameData gd = harness.getGameData();

        // Grizzly Bears should be countered
        harness.assertInGraveyard(player1, "Grizzly Bears");

        // Should prompt player2 with may ability to cast Hill Giant
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting may cast puts the creature spell on the stack")
    void acceptingMayCastPutsCreatureOnStack() {
        GrizzlyBears bears = new GrizzlyBears();
        HillGiant hillGiant = new HillGiant();

        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Counterlash(), hillGiant));
        harness.addMana(player2, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());
        harness.handleMayAbilityChosen(player2, true); // Accept casting Hill Giant

        GameData gd = harness.getGameData();

        // Hill Giant should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Hill Giant");
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);

        // Hill Giant removed from hand
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Accepting may cast and resolving puts creature on battlefield")
    void acceptingMayCastAndResolvingPutsCreatureOnBattlefield() {
        GrizzlyBears bears = new GrizzlyBears();
        HillGiant hillGiant = new HillGiant();

        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Counterlash(), hillGiant));
        harness.addMana(player2, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());
        harness.handleMayAbilityChosen(player2, true); // Accept casting Hill Giant
        harness.passBothPriorities(); // Hill Giant resolves

        GameData gd = harness.getGameData();

        // Hill Giant should be on the battlefield
        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Declining may cast does not cast any spell")
    void decliningMayCastDoesNotCast() {
        GrizzlyBears bears = new GrizzlyBears();
        HillGiant hillGiant = new HillGiant();

        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Counterlash(), hillGiant));
        harness.addMana(player2, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());
        harness.handleMayAbilityChosen(player2, false); // Decline

        GameData gd = harness.getGameData();

        // Hill Giant should still be in hand
        harness.assertInHand(player2, "Hill Giant");
        // Stack should be empty (no spells cast)
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("No may cast offered when no cards share a type with countered spell")
    void noMayCastWhenNoMatchingTypes() {
        GrizzlyBears bears = new GrizzlyBears();
        Divination divination = new Divination(); // sorcery — doesn't share type with creature

        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Counterlash(), divination));
        harness.addMana(player2, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        GameData gd = harness.getGameData();

        // Grizzly Bears should be countered
        harness.assertInGraveyard(player1, "Grizzly Bears");

        // No may ability should be offered — stack should be empty and no may prompt
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Can cast a non-targeted sorcery from hand that shares a type")
    void castNonTargetedSorceryFromHand() {
        Divination divination1 = new Divination();
        Divination divination2 = new Divination();

        harness.setHand(player1, List.of(divination1));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.setHand(player2, List.of(new Counterlash(), divination2));
        harness.addMana(player2, ManaColor.BLUE, 6);

        // Cast the sorcery — Divination targets no one
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, divination1.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player2, true); // Accept casting Divination

        // Divination2 should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Divination");
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
    }

    @Test
    @DisplayName("Can cast a targeted instant from hand — prompts for target")
    void castTargetedInstantFromHandPromptsForTarget() {
        LightningBolt bolt1 = new LightningBolt();
        LightningBolt bolt2 = new LightningBolt();

        harness.setHand(player1, List.of(bolt1));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.setHand(player2, List.of(new Counterlash(), bolt2));
        harness.addMana(player2, ManaColor.BLUE, 6);

        // Cast Lightning Bolt targeting player2
        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bolt1.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player2, true); // Accept casting Lightning Bolt

        // Should prompt for target (Lightning Bolt needs a target)
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
    }

    @Test
    @DisplayName("Fizzles if target spell is no longer on the stack")
    void fizzlesIfTargetSpellRemoved() {
        GrizzlyBears bears = new GrizzlyBears();
        HillGiant hillGiant = new HillGiant();

        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Counterlash(), hillGiant));
        harness.addMana(player2, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());

        GameData gd = harness.getGameData();
        // Remove the target spell from the stack (simulating it being countered by something else)
        gd.stack.removeIf(se -> se.getCard().getName().equals("Grizzly Bears"));

        harness.passBothPriorities();

        // Should fizzle — no may ability offered, no casting
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        // Hill Giant should still be in hand
        harness.assertInHand(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Counters the spell without offering may cast if hand is empty")
    void countersWithEmptyHand() {
        GrizzlyBears bears = new GrizzlyBears();

        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Counterlash()));
        harness.addMana(player2, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        GameData gd = harness.getGameData();

        // Grizzly Bears should be countered
        harness.assertInGraveyard(player1, "Grizzly Bears");

        // No may ability — hand is empty
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("With multiple eligible cards, declining first then accepting second works")
    void multipleEligibleCardsDeclineFirstAcceptSecond() {
        GrizzlyBears bears = new GrizzlyBears();
        HillGiant hillGiant = new HillGiant();
        GrizzlyBears bears2 = new GrizzlyBears();

        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Counterlash(), hillGiant, bears2));
        harness.addMana(player2, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        // Decline first eligible card (Hill Giant)
        harness.handleMayAbilityChosen(player2, false);

        // Should offer second eligible card (Grizzly Bears)
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        // Second creature should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(bears2.getId());
    }

    @Test
    @DisplayName("Accepting one card removes remaining may abilities for other eligible cards")
    void acceptingFirstRemovesRemainingMayAbilities() {
        GrizzlyBears bears = new GrizzlyBears();
        HillGiant hillGiant = new HillGiant();
        GrizzlyBears bears2 = new GrizzlyBears();

        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Counterlash(), hillGiant, bears2));
        harness.addMana(player2, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        // Accept first eligible card (Hill Giant)
        harness.handleMayAbilityChosen(player2, true);

        GameData gd = harness.getGameData();

        // Only Hill Giant should be on the stack (not prompted for bears2)
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Hill Giant");

        // Bears2 should still be in hand
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(bears2.getId()));
    }

    @Test
    void decliningDoesNotRevealTheUncastHandCard() {
        YoungWolf wolf = new YoungWolf();
        harness.setHand(player1, List.of(wolf));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player2, List.of(new Counterlash(), new HeadlessSkaab()));
        harness.addMana(player2, ManaColor.BLUE, 6);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, wolf.getId());

        int logSize = gd.gameLog.size();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInHand(player2, "Headless Skaab");
        assertThat(gd.gameLog.subList(logSize, gd.gameLog.size()).stream()
                .map(GameLogEntry::plainText)).noneMatch(log -> log.contains("Headless Skaab"));
    }

    @Test
    void cannotCastCreatureWithoutItsMandatoryAdditionalCost() {
        YoungWolf wolf = new YoungWolf();
        harness.setHand(player1, List.of(wolf));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player2, List.of(new Counterlash(), new HeadlessSkaab()));
        harness.setGraveyard(player2, List.of());
        harness.addMana(player2, ManaColor.BLUE, 6);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, wolf.getId());

        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player2, true);
        }

        harness.assertInHand(player2, "Headless Skaab");
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Headless Skaab");
    }

    @Test
    void spellWithNoLegalTargetsStaysInHand() {
        FiresOfUndeath fires = new FiresOfUndeath();
        harness.setHand(player1, List.of(fires));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setHand(player2, List.of(new Counterlash(), new TragicSlip()));
        harness.addMana(player2, ManaColor.BLUE, 6);
        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, fires.getId());

        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player2, true);
        }

        harness.assertInHand(player2, "Tragic Slip");
        harness.assertNotInGraveyard(player2, "Tragic Slip");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void freeTargetedInstantResolvesWithoutManaRemaining() {
        FiresOfUndeath fires = new FiresOfUndeath();
        harness.setHand(player1, List.of(fires));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setHand(player2, List.of(new Counterlash(), new FiresOfUndeath()));
        harness.addMana(player2, ManaColor.BLUE, 6);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, fires.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Fires of Undeath");
        harness.assertInGraveyard(player2, "Fires of Undeath");
        assertThat(gd.stack).isEmpty();
    }
}
