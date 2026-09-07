package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AuraOfSilence;
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

@CardUsed({ReturnToDust.class, AuraOfSilence.class, GrizzlyBears.class, Ornithopter.class})
class ReturnToDustTest extends BaseCardTest {

    private void castReturnToDust(TurnStep step, List<UUID> targetIds) {
        harness.forceActivePlayer(player1);
        harness.forceStep(step);
        harness.setHand(player1, List.of(new ReturnToDust()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, targetIds);
    }

    @Test
    @DisplayName("Exiles both targets when cast during the controller's main phase")
    void exilesBothTargetsDuringMainPhase() {
        Ornithopter artifact = new Ornithopter();
        AuraOfSilence enchantment = new AuraOfSilence();
        harness.addToBattlefield(player2, artifact);
        harness.addToBattlefield(player2, enchantment);

        UUID artifactId = harness.getPermanentId(player2, "Ornithopter");
        UUID enchantmentId = harness.getPermanentId(player2, "Aura of Silence");
        castReturnToDust(TurnStep.PRECOMBAT_MAIN, List.of(artifactId, enchantmentId));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(artifact, enchantment);
    }

    @Test
    @DisplayName("Exiles only the mandatory target when no second target is chosen")
    void secondTargetCanBeOmitted() {
        Ornithopter artifact = new Ornithopter();
        AuraOfSilence enchantment = new AuraOfSilence();
        harness.addToBattlefield(player2, artifact);
        harness.addToBattlefield(player2, enchantment);

        UUID artifactId = harness.getPermanentId(player2, "Ornithopter");
        castReturnToDust(TurnStep.PRECOMBAT_MAIN, List.of(artifactId));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(artifact).doesNotContain(enchantment);
    }

    @Test
    @DisplayName("Does not exile the optional target outside the controller's main phase")
    void optionalTargetDoesNotResolveOutsideMainPhase() {
        Ornithopter artifact = new Ornithopter();
        AuraOfSilence enchantment = new AuraOfSilence();
        harness.addToBattlefield(player2, artifact);
        harness.addToBattlefield(player2, enchantment);

        UUID artifactId = harness.getPermanentId(player2, "Ornithopter");
        UUID enchantmentId = harness.getPermanentId(player2, "Aura of Silence");
        castReturnToDust(TurnStep.UPKEEP, List.of(artifactId, enchantmentId));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(artifact).doesNotContain(enchantment);
    }

    @Test
    @DisplayName("Rejects a creature as a target")
    void rejectsCreatureTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ReturnToDust()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(creatureId)))
                .isInstanceOf(IllegalStateException.class);
    }
}
