package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.r.RayOfFrost;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({PortableHole.class, Forest.class, GrizzlyBears.class, HillGiant.class, Naturalize.class, RayOfFrost.class})
class PortableHoleTest extends BaseCardTest {

    private void castAndResolve(UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new PortableHole()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castArtifact(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB exiles an opposing nonland permanent with mana value 2 or less")
    void etbExilesMatchingPermanent() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAndResolve(bears.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Exiled permanent returns when Portable Hole leaves the battlefield")
    void exiledPermanentReturnsWhenPortableHoleLeaves() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castAndResolve(harness.getPermanentId(player2, "Grizzly Bears"));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        UUID portableHoleId = harness.getPermanentId(player1, "Portable Hole");

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, portableHoleId);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new PortableHole()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland permanent");
    }

    @Test
    @DisplayName("Cannot target an opposing permanent with mana value greater than 2")
    void cannotTargetPermanentWithHighManaValue() {
        Permanent hillGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new PortableHole()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, hillGiant.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana value 2 or less");
    }

    @Test
    @DisplayName("Cannot target a permanent controlled by the caster")
    void cannotTargetOwnPermanent() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new PortableHole()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent controls");
    }

    @Test
    @DisplayName("Can exile an opposing noncreature artifact")
    void exilesNoncreatureArtifact() {
        Permanent opposingHole = harness.addToBattlefieldAndReturn(player2, new PortableHole());

        castAndResolve(opposingHole.getId());

        harness.assertNotOnBattlefield(player2, "Portable Hole");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(opposingHole.getCard().getId()));
    }

    @Test
    @DisplayName("Does not exile the target if Portable Hole leaves before its trigger resolves")
    void sourceLeavesBeforeTriggerResolves() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PortableHole()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castArtifact(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Portable Hole"));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Portable Hole");
    }

    @Test
    @DisplayName("The owner chooses a legal attachment when an exiled Aura returns")
    void returningAuraChoosesAttachment() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new RayOfFrost());
        aura.setAttachedTo(bears.getId());
        castAndResolve(aura.getId());

        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Portable Hole"));

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player2, bears.getId());
        harness.assertOnBattlefield(player2, "Ray of Frost");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Ray of Frost")
                        && bears.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("An exiled Aura stays in exile when it has no legal attachment on return")
    void returningAuraWithoutLegalAttachmentStaysExiled() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new RayOfFrost());
        aura.setAttachedTo(bears.getId());
        castAndResolve(aura.getId());
        UUID firstHoleId = harness.getPermanentId(player1, "Portable Hole");
        castAndResolve(bears.getId());

        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player2, 0, firstHoleId);

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(aura.getCard().getId()));
        harness.assertNotOnBattlefield(player2, "Ray of Frost");
        harness.assertNotInGraveyard(player2, "Ray of Frost");
    }
}
