package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
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

@CardUsed({Skycrash.class, GrizzlyBears.class, Ornithopter.class})
class SkycrashTest extends BaseCardTest {

    private void prepareSkycrash() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Skycrash()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Destroys target artifact")
    void destroysArtifact() {
        harness.addToBattlefield(player2, new Ornithopter());
        UUID targetId = harness.getPermanentId(player2, "Ornithopter");
        prepareSkycrash();
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertInGraveyard(player2, "Ornithopter");
    }

    @Test
    @DisplayName("Cannot target a nonartifact creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Skycrash()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cycling discards Skycrash and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new Skycrash()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Skycrash");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can destroy an artifact controlled by the caster")
    void destroysOwnArtifact() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new Ornithopter()).getId();
        prepareSkycrash();
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Ornithopter");
        harness.assertInGraveyard(player1, "Ornithopter");
    }

    @Test
    @DisplayName("Cycling discards immediately but draws only on resolution")
    void cyclingPaysDiscardBeforeDrawing() {
        harness.setHand(player1, List.of(new Skycrash()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateHandAbility(player1, 0, null);

        harness.assertNotInHand(player1, "Skycrash");
        harness.assertInGraveyard(player1, "Skycrash");
        harness.assertNotInHand(player1, "Grizzly Bears");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Colorless mana cannot pay the red cycling cost")
    void cyclingRequiresRedMana() {
        harness.setHand(player1, List.of(new Skycrash()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Skycrash");
        harness.assertNotInHand(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }
}
