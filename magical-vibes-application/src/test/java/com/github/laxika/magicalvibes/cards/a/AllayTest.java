package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.h.HighGround;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Allay.class, HighGround.class, RagingGoblin.class})
class AllayTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Allay destroys target enchantment")
    void resolvingDestroysTargetEnchantment() {
        harness.addToBattlefield(player2, new HighGround());
        harness.setHand(player1, List.of(new Allay()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "High Ground");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "High Ground");
        harness.assertInGraveyard(player1, "Allay");
    }

    @Test
    @DisplayName("Paying buyback returns Allay to its owner's hand")
    void buybackReturnsToHand() {
        harness.addToBattlefield(player2, new HighGround());
        harness.setHand(player1, List.of(new Allay()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID targetId = harness.getPermanentId(player2, "High Ground");
        harness.castInstantWithBuyback(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Allay");
        harness.assertNotInGraveyard(player1, "Allay");
    }

    @Test
    @DisplayName("Allay cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new RagingGoblin());
        harness.setHand(player1, List.of(new Allay()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Raging Goblin");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Buyback does not return Allay when its target is gone at resolution")
    void buybackDoesNotReturnWhenTargetIsGoneAtResolution() {
        harness.addToBattlefield(player2, new HighGround());
        harness.setHand(player1, List.of(new Allay()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID targetId = harness.getPermanentId(player2, "High Ground");
        harness.castInstantWithBuyback(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Allay");
        harness.assertNotInHand(player1, "Allay");
    }
}
