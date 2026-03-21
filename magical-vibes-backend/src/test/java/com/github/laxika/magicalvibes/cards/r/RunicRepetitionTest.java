package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardFromExileToHandEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RunicRepetitionTest extends BaseCardTest {

    @Test
    @DisplayName("Runic Repetition has correct effect configuration")
    void hasCorrectEffectConfiguration() {
        RunicRepetition card = new RunicRepetition();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(ReturnTargetCardFromExileToHandEffect.class);
    }

    @Test
    @DisplayName("Runic Repetition returns target exiled card with flashback to hand")
    void returnsTargetExiledCardWithFlashbackToHand() {
        Card flashbackCard = new RollingTemblor();
        harness.setExile(player1, List.of(flashbackCard));
        harness.setHand(player1, List.of(new RunicRepetition()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, flashbackCard.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(flashbackCard.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId())).noneMatch(c -> c.getId().equals(flashbackCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(c -> c.getName().equals("Runic Repetition"));
    }

    @Test
    @DisplayName("Runic Repetition cannot target exiled card without flashback")
    void cannotTargetExiledCardWithoutFlashback() {
        Card noFlashbackCard = new HolyDay();
        harness.setExile(player1, List.of(noFlashbackCard));
        harness.setHand(player1, List.of(new RunicRepetition()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, noFlashbackCard.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Runic Repetition cannot target card in opponent's exile")
    void cannotTargetCardInOpponentExile() {
        Card flashbackCard = new RollingTemblor();
        harness.setExile(player2, List.of(flashbackCard));
        harness.setHand(player1, List.of(new RunicRepetition()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, flashbackCard.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("exiled card you own");
    }

    @Test
    @DisplayName("Runic Repetition fizzles if targeted card leaves exile before resolution")
    void fizzlesIfTargetLeavesExileBeforeResolution() {
        Card flashbackCard = new RollingTemblor();
        harness.setExile(player1, List.of(flashbackCard));
        harness.setHand(player1, List.of(new RunicRepetition()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, flashbackCard.getId());
        harness.getGameData().removeFromExile(flashbackCard.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Casting Runic Repetition puts an exile-targeted sorcery spell on the stack")
    void castingPutsExileTargetedSpellOnStack() {
        Card flashbackCard = new RollingTemblor();
        harness.setExile(player1, List.of(flashbackCard));
        harness.setHand(player1, List.of(new RunicRepetition()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, flashbackCard.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(flashbackCard.getId());
        assertThat(entry.getTargetZone()).isEqualTo(Zone.EXILE);
    }

    @Test
    @DisplayName("Runic Repetition requires a target to be cast")
    void requiresTargetToBeCast() {
        harness.setHand(player1, List.of(new RunicRepetition()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
