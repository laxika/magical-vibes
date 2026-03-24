package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentAndControllerLosesLifeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpreadingRotTest extends BaseCardTest {

    @Test
    @DisplayName("Spreading Rot has correct effect structure")
    void hasCorrectEffectStructure() {
        SpreadingRot card = new SpreadingRot();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(DestroyTargetPermanentAndControllerLosesLifeEffect.class);

        DestroyTargetPermanentAndControllerLosesLifeEffect effect =
                (DestroyTargetPermanentAndControllerLosesLifeEffect) card.getEffects(EffectSlot.SPELL).get(0);
        assertThat(effect.lifeLoss()).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting Spreading Rot puts it on the stack with target")
    void castingPutsOnStack() {
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new SpreadingRot()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        UUID targetId = harness.getPermanentId(player2, "Mountain");
        harness.castSorcery(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Spreading Rot");
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Resolving destroys target land and its controller loses 2 life")
    void destroysLandAndControllerLosesLife() {
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new SpreadingRot()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        UUID targetId = harness.getPermanentId(player2, "Mountain");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Mountain"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Mountain"));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Caster's life total is unchanged")
    void casterLifeUnchanged() {
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new SpreadingRot()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        UUID targetId = harness.getPermanentId(player2, "Mountain");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Spreading Rot goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new SpreadingRot()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        UUID targetId = harness.getPermanentId(player2, "Mountain");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Spreading Rot"));
    }

    @Test
    @DisplayName("Fizzles when target land is removed before resolution")
    void fizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new SpreadingRot()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        UUID targetId = harness.getPermanentId(player2, "Mountain");
        harness.castSorcery(player1, 0, targetId);
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Cannot target a creature with Spreading Rot")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SpreadingRot()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }
}
