package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MistriseVillage.class, Cancel.class, GrizzlyBears.class, Mountain.class, Forest.class})
class MistriseVillageTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped without a Mountain or Forest")
    void entersTappedWithoutQualifyingLand() {
        playVillage();

        assertThat(findPermanent(player1, "Mistrise Village").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when you control a Mountain")
    void entersUntappedWithMountain() {
        harness.addToBattlefield(player1, new Mountain());

        playVillage();

        assertThat(findPermanent(player1, "Mistrise Village").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapping adds one blue mana")
    void tappingProducesBlueMana() {
        addVillageReady();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The second ability protects the next spell from being countered")
    void protectsNextSpellFromCounter() {
        addVillageReady();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        Cancel cancel = new Cancel();
        harness.setHand(player2, List.of(cancel));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.forceActivePlayer(player1);
        harness.castCreature(player1, 0);
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    @DisplayName("The protection is consumed by the next spell")
    void protectionIsConsumedByNextSpell() {
        addVillageReady();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        GrizzlyBears firstBears = new GrizzlyBears();
        GrizzlyBears secondBears = new GrizzlyBears();
        harness.setHand(player1, List.of(firstBears, secondBears));
        harness.addMana(player1, ManaColor.GREEN, 4);

        Cancel firstCancel = new Cancel();
        Cancel secondCancel = new Cancel();
        harness.setHand(player2, List.of(firstCancel, secondCancel));
        harness.addMana(player2, ManaColor.BLUE, 6);

        harness.forceActivePlayer(player1);
        harness.castCreature(player1, 0);
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, firstBears.getId());
        harness.passBothPriorities();

        harness.castCreature(player1, 0);
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, secondBears.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A tapped Forest still allows the Village to enter untapped")
    void entersUntappedWithTappedForest() {
        harness.addToBattlefieldAndReturn(player1, new Forest()).tap();

        playVillage();

        assertThat(findPermanent(player1, "Mistrise Village").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Opposing Mountains and Forests do not allow the Village to enter untapped")
    void opposingLandsDoNotQualify() {
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Forest());

        playVillage();

        assertThat(findPermanent(player1, "Mistrise Village").isTapped()).isTrue();
    }

    @Test
    @DisplayName("The protection ability uses the stack and pays blue mana and tapping immediately")
    void protectionAbilityUsesStackAndPaysCosts() {
        Permanent village = addVillageReady();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(village.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Two resolved activations both protect the same next spell")
    void multipleActivationsDoNotProtectMultipleSpells() {
        addVillageReady();
        addVillageReady();
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();

        GrizzlyBears firstBears = new GrizzlyBears();
        GrizzlyBears secondBears = new GrizzlyBears();
        harness.setHand(player1, List.of(firstBears, secondBears));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setHand(player2, List.of(new Cancel(), new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, firstBears.getId());
        harness.passBothPriorities();

        harness.castCreature(player1, 0);
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, secondBears.getId());

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(secondBears);
    }

    @Test
    @DisplayName("Unused protection expires at the end of the turn")
    void unusedProtectionExpires() {
        addVillageReady();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.setLibrary(player2, List.of(new Mountain(), new Forest()));
        harness.setLibrary(player1, List.of(new Mountain(), new Forest()));
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0);
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The next instant is protected too, even when it is cast on the opponent's turn")
    void protectsInstantOnOpponentsTurn() {
        addVillageReady();
        harness.forceActivePlayer(player2);
        harness.ensurePriority(player1);
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        GrizzlyBears bears = new GrizzlyBears();
        Cancel protectedCancel = new Cancel();
        harness.setHand(player2, List.of(bears, new Cancel()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.setHand(player1, List.of(protectedCancel));
        harness.ensurePriority(player2);
        harness.castCreature(player2, 0);
        harness.ensurePriority(player1);
        harness.castInstant(player1, 0, bears.getId());
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, protectedCancel.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Cancel");
    }

    private void playVillage() {
        harness.setHand(player1, List.of(new MistriseVillage()));
        harness.playLand(player1, 0);
    }

    private Permanent addVillageReady() {
        return harness.addToBattlefieldAndReturn(player1, new MistriseVillage());
    }
}
