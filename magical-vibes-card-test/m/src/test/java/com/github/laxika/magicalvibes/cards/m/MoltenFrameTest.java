package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MoltenFrame.class, Ornithopter.class, GrizzlyBears.class, FountainOfYouth.class})
class MoltenFrameTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Molten Frame destroys target artifact creature")
    void destroysArtifactCreature() {
        harness.addToBattlefield(player2, new Ornithopter());
        harness.setHand(player1, List.of(new MoltenFrame()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Ornithopter");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertInGraveyard(player2, "Ornithopter");
    }

    @Test
    @DisplayName("Cannot target a nonartifact creature with Molten Frame")
    void cannotTargetNonArtifactCreature() {
        harness.addToBattlefield(player2, new Ornithopter()); // legal target so the spell is castable
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MoltenFrame()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, bearId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature artifact with Molten Frame")
    void cannotTargetNonCreatureArtifact() {
        harness.addToBattlefield(player2, new Ornithopter()); // legal target so the spell is castable
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new MoltenFrame()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID artifactId = harness.getPermanentId(player2, "Fountain of Youth");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifactId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cycling discards Molten Frame and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new MoltenFrame()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Molten Frame");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Molten Frame can destroy an artifact creature you control")
    void destroysOwnArtifactCreature() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.setHand(player1, List.of(new MoltenFrame()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Ornithopter"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ornithopter");
        harness.assertInGraveyard(player1, "Ornithopter");
        harness.assertInGraveyard(player1, "Molten Frame");
    }

    @Test
    @DisplayName("Cycling discards immediately but draws only when the ability resolves")
    void cyclingPaysDiscardBeforeDrawing() {
        harness.setHand(player1, List.of(new MoltenFrame()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertNotInHand(player1, "Molten Frame");
        harness.assertInGraveyard(player1, "Molten Frame");
        harness.assertNotInHand(player1, "Grizzly Bears");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cycling cannot be activated with less than two mana")
    void cyclingRequiresTwoMana() {
        harness.setHand(player1, List.of(new MoltenFrame()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Molten Frame");
        harness.assertNotInGraveyard(player1, "Molten Frame");
        harness.assertNotInHand(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }
}
