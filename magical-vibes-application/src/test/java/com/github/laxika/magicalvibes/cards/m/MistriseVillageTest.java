package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();
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
        harness.castInstant(player2, 0, firstBears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.castCreature(player1, 0);
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, secondBears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private void playVillage() {
        harness.setHand(player1, List.of(new MistriseVillage()));
        harness.playLand(player1, 0);
    }

    private Permanent addVillageReady() {
        return harness.addToBattlefieldAndReturn(player1, new MistriseVillage());
    }
}
