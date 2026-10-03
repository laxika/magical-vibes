package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forbid;
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

@CardUsed({Allay.class, HighGround.class, RagingGoblin.class, Forbid.class})
class AllayTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Allay destroys target enchantment")
    void resolvingDestroysTargetEnchantment() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new HighGround()).getId();
        harness.setHand(player1, List.of(new Allay()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "High Ground");
        harness.assertInGraveyard(player1, "Allay");
    }

    @Test
    @DisplayName("Paying buyback returns Allay to its owner's hand")
    void buybackReturnsToHand() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new HighGround()).getId();
        harness.setHand(player1, List.of(new Allay()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstantWithBuyback(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Allay");
        harness.assertNotInGraveyard(player1, "Allay");
    }

    @Test
    @DisplayName("Allay cannot target a creature")
    void cannotTargetCreature() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new RagingGoblin()).getId();
        harness.setHand(player1, List.of(new Allay()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Buyback does not return Allay when its target is gone at resolution")
    void buybackDoesNotReturnWhenTargetIsGoneAtResolution() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new HighGround()).getId();
        harness.setHand(player1, List.of(new Allay()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstantWithBuyback(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Allay");
        harness.assertNotInHand(player1, "Allay");
    }

    @Test
    @DisplayName("Allay can destroy its controller's enchantment")
    void canDestroyOwnEnchantment() {
        var target = harness.addToBattlefieldAndReturn(player1, new HighGround());
        harness.setHand(player1, List.of(new Allay()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "High Ground");
        harness.assertInGraveyard(player1, "High Ground");
        harness.assertInGraveyard(player1, "Allay");
    }

    @Test
    @DisplayName("Buyback requires three additional mana")
    void insufficientBuybackManaDoesNotCastSpell() {
        var target = harness.addToBattlefieldAndReturn(player2, new HighGround());
        harness.setHand(player1, List.of(new Allay()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstantWithBuyback(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Allay");
        harness.assertOnBattlefield(player2, "High Ground");
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Allay");
        harness.assertInGraveyard(player2, "High Ground");
    }

    @Test
    @DisplayName("Countering Allay prevents its paid buyback from returning it")
    void counteredBuybackSpellGoesToGraveyard() {
        var target = harness.addToBattlefieldAndReturn(player2, new HighGround());
        Allay allay = new Allay();
        harness.setHand(player1, List.of(allay));
        harness.setHand(player2, List.of(new Forbid()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstantWithBuyback(player1, 0, target.getId());
        harness.castInstant(player2, 0, allay.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Allay");
        harness.assertNotInHand(player1, "Allay");
        harness.assertOnBattlefield(player2, "High Ground");
        harness.assertInGraveyard(player2, "Forbid");
    }
}
