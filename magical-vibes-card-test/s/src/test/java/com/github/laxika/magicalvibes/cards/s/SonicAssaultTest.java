package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.v.VernadiShieldmate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SonicAssault.class, VernadiShieldmate.class})
class SonicAssaultTest extends BaseCardTest {

    @Test
    @DisplayName("Taps the target creature and deals 2 damage to its controller")
    void tapsCreatureAndDamagesItsController() {
        Permanent target = addCreatureReady(player2, new VernadiShieldmate());
        harness.setHand(player1, List.of(new SonicAssault()));
        addMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Jump-start discards a card, taps the target, damages its controller, and exiles Sonic Assault")
    void jumpStartDiscardsTapsDamagesAndExiles() {
        Permanent target = addCreatureReady(player2, new VernadiShieldmate());
        SonicAssault spell = new SonicAssault();
        VernadiShieldmate discarded = new VernadiShieldmate();
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player1, List.of(discarded));
        addMana();

        harness.castJumpStart(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(discarded.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(spell.getId()));
    }

    @Test
    @DisplayName("An already tapped creature still causes damage to its controller")
    void alreadyTappedOwnCreatureStillDamagesController() {
        Permanent target = addCreatureReady(player1, new VernadiShieldmate());
        target.tap();
        harness.setHand(player1, List.of(new SonicAssault()));
        addMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getMarkedDamage()).isZero();
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Sonic Assault");
    }

    @Test
    @DisplayName("An absent target prevents both tapping and controller damage")
    void absentTargetPreventsControllerDamage() {
        Permanent target = addCreatureReady(player2, new VernadiShieldmate());
        harness.setHand(player1, List.of(new SonicAssault()));
        addMana();

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setGraveyard(player2, List.of(target.getCard()));
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Sonic Assault");
    }

    @Test
    @DisplayName("Jump-start still exiles the spell when its target becomes illegal")
    void jumpStartExilesWithAbsentTarget() {
        Permanent target = addCreatureReady(player2, new VernadiShieldmate());
        SonicAssault spell = new SonicAssault();
        VernadiShieldmate discarded = new VernadiShieldmate();
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player1, List.of(discarded));
        addMana();

        harness.castJumpStart(player1, 0, 0, target.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Vernadi Shieldmate");
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setGraveyard(player2, List.of(target.getCard()));
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertNotInGraveyard(player1, "Sonic Assault");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(spell.getId()));
    }

    @Test
    @DisplayName("Sonic Assault cannot target a player directly")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new SonicAssault()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Sonic Assault");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Jump-start requires a card to discard")
    void jumpStartRequiresDiscard() {
        Permanent target = addCreatureReady(player2, new VernadiShieldmate());
        harness.setGraveyard(player1, List.of(new SonicAssault()));
        harness.setHand(player1, List.of());
        addMana();

        assertThatThrownBy(() -> harness.castJumpStart(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Sonic Assault");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(target.isTapped()).isFalse();
        harness.assertLife(player2, 20);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
