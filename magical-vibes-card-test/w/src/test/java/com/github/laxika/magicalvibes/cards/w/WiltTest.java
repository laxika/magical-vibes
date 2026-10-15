package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CrystallineGiant;
import com.github.laxika.magicalvibes.cards.d.DrannithMagistrate;
import com.github.laxika.magicalvibes.cards.s.SanctuaryLockdown;
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

@CardUsed({Wilt.class, SanctuaryLockdown.class, CrystallineGiant.class, DrannithMagistrate.class})
class WiltTest extends BaseCardTest {

    private void prepareWilt() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Wilt()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Destroys target artifact")
    void destroysArtifact() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new CrystallineGiant()).getId();
        prepareWilt();
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Crystalline Giant");
        harness.assertInGraveyard(player2, "Crystalline Giant");
    }

    @Test
    @DisplayName("Destroys target enchantment")
    void destroysEnchantment() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new SanctuaryLockdown()).getId();
        prepareWilt();
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Sanctuary Lockdown");
        harness.assertInGraveyard(player2, "Sanctuary Lockdown");
    }

    @Test
    @DisplayName("Cannot target a nonartifact, nonenchantment creature")
    void cannotTargetCreature() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new DrannithMagistrate()).getId();

        prepareWilt();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cycling discards Wilt and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new Wilt()));
        harness.setLibrary(player1, List.of(new DrannithMagistrate()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Wilt");
        harness.assertInHand(player1, "Drannith Magistrate");
    }

    @Test
    @DisplayName("Can destroy an artifact controlled by the caster")
    void destroysOwnArtifact() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new CrystallineGiant()).getId();
        prepareWilt();
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Crystalline Giant");
        harness.assertInGraveyard(player1, "Crystalline Giant");
    }

    @Test
    @DisplayName("Cycling discards as a cost and draws only on resolution")
    void cyclingDiscardsBeforeDrawing() {
        harness.setHand(player1, List.of(new Wilt()));
        harness.setLibrary(player1, List.of(new DrannithMagistrate()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Wilt");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertInHand(player1, "Drannith Magistrate");
    }

    @Test
    @DisplayName("Cycling requires two mana and does not discard on failed payment")
    void cannotCycleWithInsufficientMana() {
        harness.setHand(player1, List.of(new Wilt()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Wilt");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
