package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CrushTest extends BaseCardTest {

    @Test
    @DisplayName("Crush has correct effects")
    void hasCorrectProperties() {
        Crush card = new Crush();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(DestroyTargetPermanentEffect.class);
    }

    @Test
    @DisplayName("Casting Crush puts it on the stack with target")
    void castingPutsOnStack() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new Crush()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        harness.castInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Crush");
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Resolving Crush destroys target noncreature artifact")
    void destroysNoncreatureArtifact() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new Crush()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Fountain of Youth"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Fountain of Youth"));
    }

    @Test
    @DisplayName("Cannot target an artifact creature with Crush")
    void cannotTargetArtifactCreature() {
        harness.addToBattlefield(player2, new CoreProwler());
        harness.setHand(player1, List.of(new Crush()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID creatureId = harness.getPermanentId(player2, "Core Prowler");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Crush fizzles when target is removed before resolution")
    void fizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new Crush()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        harness.castInstant(player1, 0, targetId);
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Crush"));
    }

    @Test
    @DisplayName("Crush goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new Crush()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Crush"));
    }
}
