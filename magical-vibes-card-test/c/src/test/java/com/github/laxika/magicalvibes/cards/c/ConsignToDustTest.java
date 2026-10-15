package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ConsignToDust.class, FountainOfYouth.class, GrizzlyBears.class, Pacifism.class})
class ConsignToDustTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys any number of target artifacts and enchantments")
    void destroysMixedTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Pacifism());
        aura.setAttachedTo(creature.getId());

        castConsignToDust(List.of(artifact.getId(), aura.getId()), 2, 4);

        harness.assertNotOnBattlefield(player1, "Fountain of Youth");
        harness.assertNotOnBattlefield(player2, "Pacifism");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Strive requires {2}{G} for each additional target")
    void striveAddsCostForEachAdditionalTarget() {
        Permanent firstArtifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        Permanent secondArtifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        harness.setHand(player1, List.of(new ConsignToDust()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, List.of(firstArtifact.getId(), secondArtifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target no permanents")
    void canChooseNoTargets() {
        harness.setHand(player1, List.of(new ConsignToDust()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, List.of());
        harness.assertInGraveyard(player1, "Consign to Dust");
    }

    @Test
    @DisplayName("Can target only artifacts and enchantments")
    void targetsMustBeArtifactsOrEnchantments() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ConsignToDust()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID creatureId = creature.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Targets must be artifacts and/or enchantments");
    }

    @Test
    @DisplayName("A single target costs only the printed mana cost")
    void singleTargetNeedsNoStrivePayment() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        castConsignToDust(List.of(artifact.getId()), 1, 2);

        harness.assertInGraveyard(player2, "Fountain of Youth");
        harness.assertInGraveyard(player1, "Consign to Dust");
    }

    @Test
    @DisplayName("Three targets require two strive payments")
    void threeTargetsRequireTwoStrivePayments() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new ConsignToDust()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        List<UUID> targets = List.of(first.getId(), second.getId(), third.getId());

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targets))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, targets);

        harness.assertNotOnBattlefield(player1, "Fountain of Youth");
        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertInGraveyard(player1, "Consign to Dust");
    }

    @Test
    @DisplayName("Still destroys the remaining target when another target leaves")
    void resolvesWithOneRemainingLegalTarget() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new ConsignToDust()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castInstant(player1, 0, List.of(first.getId(), second.getId()));

        gd.playerBattlefields.get(player1.getId()).remove(first);
        gd.playerGraveyards.get(player1.getId()).add(first.getCard());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Fountain of Youth");
        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertInGraveyard(player1, "Consign to Dust");
    }

    @Test
    @DisplayName("The same permanent cannot be chosen twice")
    void rejectsDuplicateTargets() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new ConsignToDust()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(artifact.getId(), artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castConsignToDust(List<UUID> targetIds, int greenMana, int colorlessMana) {
        harness.setHand(player1, List.of(new ConsignToDust()));
        harness.addMana(player1, ManaColor.GREEN, greenMana);
        harness.addMana(player1, ManaColor.COLORLESS, colorlessMana);
        harness.castAndResolveInstant(player1, 0, targetIds);
    }
}
