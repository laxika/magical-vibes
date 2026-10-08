package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.w.WatchersOfTheDead;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ViolentImpact.class, FountainOfYouth.class, GrizzlyBears.class, Mountain.class,
        WatchersOfTheDead.class})
class ViolentImpactTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving destroys target land")
    void resolvingDestroysTargetLand() {
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new ViolentImpact()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID targetId = harness.getPermanentId(player2, "Mountain");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertInGraveyard(player2, "Mountain");
    }

    @Test
    @DisplayName("Resolving destroys target artifact")
    void resolvingDestroysTargetArtifact() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new ViolentImpact()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertInGraveyard(player2, "Fountain of Youth");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ViolentImpact()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cycling discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new ViolentImpact()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Violent Impact");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can destroy your own land with exactly three generic and one red mana")
    void destroysOwnLandWithExactMana() {
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new ViolentImpact()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player1, "Mountain"));

        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertInGraveyard(player1, "Mountain");
        harness.assertInGraveyard(player1, "Violent Impact");
    }

    @Test
    @DisplayName("Four generic mana cannot pay the spell's red mana requirement")
    void cannotCastWithoutRedMana() {
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new ViolentImpact()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID targetId = harness.getPermanentId(player2, "Mountain");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Violent Impact");
        harness.assertOnBattlefield(player2, "Mountain");
    }

    @Test
    @DisplayName("Cycling pays a discard immediately and draws only when the ability resolves")
    void cyclingDiscardsAsCostAndNeedsNoRedManaOrTarget() {
        harness.setHand(player1, List.of(new ViolentImpact()));
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertNotInHand(player1, "Violent Impact");
        harness.assertInGraveyard(player1, "Violent Impact");
        harness.assertNotInHand(player1, "Mountain");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Mountain");
        harness.assertInGraveyard(player1, "Violent Impact");
    }

    @Test
    @DisplayName("Cycling cannot be activated with only one mana and does not discard")
    void cyclingRequiresTwoMana() {
        harness.setHand(player1, List.of(new ViolentImpact()));
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Violent Impact");
        harness.assertNotInGraveyard(player1, "Violent Impact");
        harness.assertNotInHand(player1, "Mountain");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An artifact creature is a legal target")
    void destroysArtifactCreature() {
        harness.addToBattlefield(player2, new WatchersOfTheDead());
        harness.setHand(player1, List.of(new ViolentImpact()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player2, "Watchers of the Dead"));

        harness.assertNotOnBattlefield(player2, "Watchers of the Dead");
        harness.assertInGraveyard(player2, "Watchers of the Dead");
    }

    @Test
    @DisplayName("A target exiled in response makes the spell fail to resolve")
    void targetExiledInResponse() {
        harness.addToBattlefield(player2, new WatchersOfTheDead());
        harness.setHand(player1, List.of(new ViolentImpact()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Watchers of the Dead"));
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Watchers of the Dead");
        harness.assertNotInGraveyard(player2, "Watchers of the Dead");
        assertThat(gd.exiledCards)
                .anyMatch(entry -> entry.card().getName().equals("Watchers of the Dead"));
        harness.assertInGraveyard(player1, "Violent Impact");
        assertThat(gd.stack).isEmpty();
    }
}
