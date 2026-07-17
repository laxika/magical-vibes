package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ComaVeilTest extends BaseCardTest {

    // ===== Targeting =====

    @Test
    @DisplayName("Can target a creature with Coma Veil")
    void canTargetCreature() {
        Permanent creature = addCreatureReady(player2);

        harness.setHand(player1, List.of(new ComaVeil()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Can target an artifact with Coma Veil")
    void canTargetArtifact() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        Permanent artifact = findPermanentByName(player2, "Fountain of Youth");

        harness.setHand(player1, List.of(new ComaVeil()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0, artifact.getId());

        assertThat(gd.stack).hasSize(1);
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Resolving Coma Veil attaches it to target creature")
    void resolvingAttachesToCreature() {
        Permanent creature = addCreatureReady(player2);

        harness.setHand(player1, List.of(new ComaVeil()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Coma Veil")
                        && p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));
    }

    // ===== Prevents untapping =====

    @Test
    @DisplayName("Tapped creature with Coma Veil does not untap during controller's untap step")
    void enchantedCreatureDoesNotUntap() {
        Permanent creature = addCreatureReady(player2);
        creature.tap();

        Permanent veilPerm = new Permanent(new ComaVeil());
        veilPerm.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(veilPerm);

        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapped artifact with Coma Veil does not untap during controller's untap step")
    void enchantedArtifactDoesNotUntap() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        Permanent artifact = findPermanentByName(player2, "Fountain of Youth");
        artifact.tap();

        Permanent veilPerm = new Permanent(new ComaVeil());
        veilPerm.setAttachedTo(artifact.getId());
        gd.playerBattlefields.get(player1.getId()).add(veilPerm);

        advanceToNextTurn(player1);

        assertThat(artifact.isTapped()).isTrue();
    }

    // ===== Removal =====

    @Test
    @DisplayName("Creature can untap again after Coma Veil is removed")
    void creatureUntapsAfterVeilRemoved() {
        Permanent creature = addCreatureReady(player2);
        creature.tap();

        Permanent veilPerm = new Permanent(new ComaVeil());
        veilPerm.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(veilPerm);

        gd.playerBattlefields.get(player1.getId()).remove(veilPerm);

        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isFalse();
    }

    // ===== Helpers =====

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // END_STEP -> CLEANUP
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // CLEANUP -> next turn
    }

    private Permanent addCreatureReady(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent findPermanentByName(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst().orElseThrow();
    }
}
