package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GleefulSabotageTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving destroys target artifact")
    void resolvesAndDestroysArtifact() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new GleefulSabotage()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Fountain of Youth"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Fountain of Youth"));
    }

    @Test
    @DisplayName("Resolving destroys target enchantment")
    void resolvesAndDestroysEnchantment() {
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.setHand(player1, List.of(new GleefulSabotage()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Angelic Chorus"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Angelic Chorus"));
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        Permanent creature = addCreatureReady(player2, new ScatheZombies());
        harness.setHand(player1, List.of(new GleefulSabotage()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Conspire taps two color-sharing creatures and queues a copy of the spell")
    void conspireTapsCreaturesAndQueuesCopy() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new GleefulSabotage()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        Permanent bears1 = addCreatureReady(player1, new GrizzlyBears());
        Permanent bears2 = addCreatureReady(player1, new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        harness.castWithConspire(player1, 0, targetId, List.of(bears1.getId(), bears2.getId()));

        GameData gd = harness.getGameData();
        assertThat(bears1.isTapped()).isTrue();
        assertThat(bears2.isTapped()).isTrue();

        // The spell plus one conspire copy trigger are on the stack.
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.SORCERY_SPELL);
        assertThat(gd.stack).anyMatch(e -> e.getEffectsToResolve().stream()
                .anyMatch(fx -> fx instanceof CopyControllerCastSpellEffect));
    }

    @Test
    @DisplayName("Conspire is rejected when a chosen creature does not share a color with the spell")
    void conspireRejectsNonGreenCreature() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new GleefulSabotage()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent zombie = addCreatureReady(player1, new ScatheZombies()); // black, does not share green

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        assertThatThrownBy(() -> harness.castWithConspire(player1, 0, targetId,
                List.of(bears.getId(), zombie.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
