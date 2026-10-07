package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThopterArrest.class, GrizzlyBears.class, Naturalize.class, PropheticPrism.class})
class ThopterArrestTest extends BaseCardTest {

    private void setUpCast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ThopterArrest()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void castAndResolve(UUID targetId) {
        setUpCast();
        harness.castEnchantment(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB exiles an opponent's creature")
    void etbExilesOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        castAndResolve(targetId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("ETB exiles an opponent's artifact")
    void etbExilesOpponentArtifact() {
        harness.addToBattlefield(player2, new PropheticPrism());
        UUID targetId = harness.getPermanentId(player2, "Prophetic Prism");

        castAndResolve(targetId);

        harness.assertNotOnBattlefield(player2, "Prophetic Prism");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Prophetic Prism"));
    }

    @Test
    @DisplayName("Exiled permanent returns when Thopter Arrest leaves the battlefield")
    void exiledPermanentReturnsWhenSourceLeaves() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        castAndResolve(targetId);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        UUID sourceId = harness.getPermanentId(player1, "Thopter Arrest");
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, sourceId);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
    }

    @Test
    @DisplayName("Cannot target an artifact or creature controlled by the caster")
    void cannotTargetOwnPermanent() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        setUpCast();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or creature an opponent controls");
    }

    @Test
    @DisplayName("Cannot target an artifact controlled by the caster")
    void cannotTargetOwnArtifact() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new PropheticPrism()).getId();
        setUpCast();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or creature an opponent controls");
    }

    @Test
    @DisplayName("Cannot target an opponent's nonartifact enchantment")
    void cannotTargetNonartifactEnchantment() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new ThopterArrest()).getId();
        setUpCast();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or creature an opponent controls");
    }

    @Test
    @DisplayName("Target is not exiled if Thopter Arrest leaves before its ETB resolves")
    void sourceLeavingBeforeTriggerResolvesPreventsExile() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        setUpCast();
        harness.castEnchantment(player1, 0, targetId);
        harness.passBothPriorities();

        UUID sourceId = harness.getPermanentId(player1, "Thopter Arrest");
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, sourceId);
        harness.assertNotOnBattlefield(player1, "Thopter Arrest");
        harness.passBothPriorities();

        assertThat(harness.getPermanentId(player2, "Grizzly Bears")).isEqualTo(targetId);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("An artifact that leaves before the ETB resolves is not exiled from its graveyard")
    void targetLeavingBeforeTriggerResolvesIsNotExiled() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new PropheticPrism()).getId();
        setUpCast();
        harness.castEnchantment(player1, 0, targetId);
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player2, 0, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Prophetic Prism");
        harness.assertOnBattlefield(player1, "Thopter Arrest");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A returning artifact is a new permanent and triggers its ETB ability")
    void returningArtifactTriggersItsEnterAbility() {
        UUID originalId = harness.addToBattlefieldAndReturn(player2, new PropheticPrism()).getId();
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        castAndResolve(originalId);
        harness.assertNotOnBattlefield(player2, "Prophetic Prism");
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        UUID sourceId = harness.getPermanentId(player1, "Thopter Arrest");
        harness.castAndResolveInstant(player2, 0, sourceId);

        assertThat(harness.getPermanentId(player2, "Prophetic Prism")).isNotEqualTo(originalId);
        harness.assertNotInHand(player2, "Grizzly Bears");
        harness.passBothPriorities();
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }
}
