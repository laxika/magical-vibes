package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.Expel;
import com.github.laxika.magicalvibes.cards.k.KasminaEnigmaSage;
import com.github.laxika.magicalvibes.cards.s.StrixhavenStadium;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrackleWithPower.class, CampusGuide.class, Expel.class,
        KasminaEnigmaSage.class, StrixhavenStadium.class})
class CrackleWithPowerTest extends BaseCardTest {

    @Test
    @DisplayName("Deals five times X damage to each target")
    void dealsFiveTimesXDamageToEachTarget() {
        harness.setLife(player1, 30);
        harness.setLife(player2, 30);
        harness.setHand(player1, List.of(new CrackleWithPower()));
        harness.addMana(player1, ManaColor.RED, 8);

        harness.castSorcery(player1, 0, 2, List.of(player1.getId(), player2.getId()));
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("May choose fewer than X targets")
    void mayChooseFewerThanXTargets() {
        harness.setLife(player2, 30);
        harness.setHand(player1, List.of(new CrackleWithPower()));
        harness.addMana(player1, ManaColor.RED, 11);

        harness.castSorcery(player1, 0, 3, List.of(player2.getId()));
        harness.passBothPriorities();

        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("Rejects more than X targets")
    void rejectsMoreThanXTargets() {
        harness.setHand(player1, List.of(new CrackleWithPower()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1,
                List.of(player1.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void mayCastWithXZeroAndNoTargets() {
        harness.setHand(player1, List.of(new CrackleWithPower()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Crackle with Power");
    }

    @Test
    void mayChooseNoTargetsWithPositiveX() {
        harness.setHand(player1, List.of(new CrackleWithPower()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, 1);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Crackle with Power");
    }

    @Test
    void cannotChooseATargetWithXZero() {
        harness.setHand(player1, List.of(new CrackleWithPower()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void mustPayForAllThreeXSymbols() {
        harness.setHand(player1, List.of(new CrackleWithPower()));
        harness.addMana(player1, ManaColor.RED, 7);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2, List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotChooseTheSameTargetTwice() {
        harness.setHand(player1, List.of(new CrackleWithPower()));
        harness.addMana(player1, ManaColor.RED, 8);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2,
                List.of(player2.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void dealsFullDamageToCreaturePlaneswalkerAndPlayer() {
        var creature = harness.addToBattlefieldAndReturn(player2, new CampusGuide());
        var planeswalker = harness.enterBattlefieldAndReturn(player2, new KasminaEnigmaSage());
        harness.setLife(player2, 30);
        harness.setHand(player1, List.of(new CrackleWithPower()));
        harness.addMana(player1, ManaColor.RED, 11);

        harness.castSorcery(player1, 0, 3,
                List.of(creature.getId(), planeswalker.getId(), player2.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Campus Guide");
        harness.assertInGraveyard(player2, "Kasmina, Enigma Sage");
        harness.assertLife(player2, 15);
    }

    @Test
    void cannotTargetANoncreatureArtifact() {
        var artifact = harness.addToBattlefieldAndReturn(player2, new StrixhavenStadium());
        harness.setHand(player1, List.of(new CrackleWithPower()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void stillDealsFullDamageWhenAnotherTargetLeavesTheBattlefield() {
        var creature = harness.addToBattlefieldAndReturn(player2, new CampusGuide());
        creature.setTapped(true);
        harness.setLife(player2, 30);
        harness.setHand(player1, List.of(new CrackleWithPower()));
        harness.setHand(player2, List.of(new Expel()));
        harness.addMana(player1, ManaColor.RED, 8);
        harness.addMana(player2, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, 2, List.of(creature.getId(), player2.getId()));
        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertNotOnBattlefield(player2, "Campus Guide");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Campus Guide"));
        harness.assertInGraveyard(player1, "Crackle with Power");
    }

    @Test
    void doesNotResolveWhenItsOnlyTargetLeavesTheBattlefield() {
        var creature = harness.addToBattlefieldAndReturn(player2, new CampusGuide());
        creature.setTapped(true);
        harness.setHand(player1, List.of(new CrackleWithPower()));
        harness.setHand(player2, List.of(new Expel()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.addMana(player2, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, 1, List.of(creature.getId()));
        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Campus Guide"));
        harness.assertInGraveyard(player1, "Crackle with Power");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void mayChooseMoreThanOneHundredTargetsWhenXIsLargeEnough() {
        List<UUID> targets = IntStream.range(0, 101)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player2, new CampusGuide()).getId())
                .toList();
        harness.setHand(player1, List.of(new CrackleWithPower()));
        harness.addMana(player1, ManaColor.RED, 305);

        harness.castSorcery(player1, 0, 101, targets);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Campus Guide");
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(101);
        harness.assertInGraveyard(player1, "Crackle with Power");
    }
}
