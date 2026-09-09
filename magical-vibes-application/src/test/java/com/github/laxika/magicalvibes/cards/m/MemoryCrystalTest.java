package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forbid;
import com.github.laxika.magicalvibes.cards.s.ShatteringPulse;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MemoryCrystal.class, ShatteringPulse.class, Spellbook.class, Forbid.class})
class MemoryCrystalTest extends BaseCardTest {

    @Test
    void reducesManaBuybackCost() {
        harness.addToBattlefield(player1, new MemoryCrystal());
        harness.addToBattlefield(player1, new Spellbook());
        harness.setHand(player1, List.of(new ShatteringPulse()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player1, "Spellbook");
        harness.castInstantWithBuyback(player1, 0, targetId);

        assertThat(harness.getGameData().stack.getFirst().isBuyback()).isTrue();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Shattering Pulse");
    }

    @Test
    void affectsBuybackCostsOfOpponentsSpells() {
        harness.addToBattlefield(player1, new MemoryCrystal());
        harness.addToBattlefield(player1, new Spellbook());
        harness.setHand(player2, List.of(new ShatteringPulse()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player1, "Spellbook");
        harness.castInstantWithBuyback(player2, 0, targetId);
        harness.passBothPriorities();

        harness.assertInHand(player2, "Shattering Pulse");
    }

    @Test
    void doesNotReduceNormalManaCost() {
        harness.addToBattlefield(player1, new MemoryCrystal());
        harness.addToBattlefield(player1, new Spellbook());
        harness.setHand(player1, List.of(new ShatteringPulse()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player1, "Spellbook");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotReduceDiscardBuybackCost() {
        harness.addToBattlefield(player1, new MemoryCrystal());
        harness.addToBattlefield(player1, new Spellbook());
        harness.setHand(player1, List.of(new ShatteringPulse()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player1, "Spellbook");
        harness.castInstant(player1, 0, targetId);
        UUID spellId = harness.getGameData().stack.getFirst().getCard().getId();

        harness.setHand(player2, List.of(new Forbid(), new Spellbook()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstantWithDiscardBuyback(player2, 0, spellId, List.of(1)))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player2, "Forbid");
    }
}
