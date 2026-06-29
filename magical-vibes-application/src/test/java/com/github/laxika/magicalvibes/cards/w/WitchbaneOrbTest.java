package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CurseOfTheBloodyTome;
import com.github.laxika.magicalvibes.cards.c.CurseOfThePiercedHeart;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WitchbaneOrbTest extends BaseCardTest {

    // ===== ETB: destroy all Curses attached to you =====

    @Test
    @DisplayName("ETB destroys Curse attached to its controller")
    void etbDestroysCurseAttachedToController() {
        Permanent cursePerm = placeCurseOnPlayer(player2, player1);

        harness.setHand(player1, List.of(new WitchbaneOrb()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve artifact
        harness.passBothPriorities(); // resolve ETB trigger

        // Curse should be destroyed (moved to graveyard)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Curse of the Pierced Heart"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Curse of the Pierced Heart"));
    }

    @Test
    @DisplayName("ETB destroys multiple Curses attached to its controller")
    void etbDestroysMultipleCursesAttachedToController() {
        placeCurseOnPlayer(player2, player1);
        placeCurseOnPlayer(player2, player1, new CurseOfTheBloodyTome());

        harness.setHand(player1, List.of(new WitchbaneOrb()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve artifact
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Curse of the Pierced Heart"))
                .noneMatch(p -> p.getCard().getName().equals("Curse of the Bloody Tome"));
    }

    @Test
    @DisplayName("ETB does not destroy Curses attached to the opponent")
    void etbDoesNotDestroyCursesAttachedToOpponent() {
        placeCurseOnPlayer(player1, player2);

        harness.setHand(player1, List.of(new WitchbaneOrb()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve artifact
        harness.passBothPriorities(); // resolve ETB trigger

        // Curse on opponent should remain
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Curse of the Pierced Heart"));
    }

    // ===== Static: controller has hexproof =====

    @Test
    @DisplayName("Controller has hexproof while Witchbane Orb is on the battlefield")
    void controllerHasHexproof() {
        harness.addToBattlefield(player1, new WitchbaneOrb());

        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isTrue();
    }

    @Test
    @DisplayName("Opponent cannot target controller with a spell")
    void opponentCannotTargetControllerWithSpell() {
        harness.addToBattlefield(player1, new WitchbaneOrb());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new CurseOfThePiercedHeart()));
        harness.addMana(player2, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("Hexproof does not protect the opponent")
    void hexproofDoesNotProtectOpponent() {
        harness.addToBattlefield(player1, new WitchbaneOrb());

        assertThat(gqs.playerHasHexproof(gd, player2.getId())).isFalse();
    }

    @Test
    @DisplayName("Hexproof is lost when Witchbane Orb leaves the battlefield")
    void hexproofLostWhenOrbRemoved() {
        harness.addToBattlefield(player1, new WitchbaneOrb());
        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isTrue();

        // Remove the orb
        Permanent orbPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Witchbane Orb"))
                .findFirst().orElseThrow();
        gd.playerBattlefields.get(player1.getId()).remove(orbPerm);

        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isFalse();
    }

    // ===== Helpers =====

    private Permanent placeCurseOnPlayer(Player controller, Player enchantedPlayer) {
        return placeCurseOnPlayer(controller, enchantedPlayer, new CurseOfThePiercedHeart());
    }

    private Permanent placeCurseOnPlayer(Player controller, Player enchantedPlayer, com.github.laxika.magicalvibes.model.Card curseCard) {
        Permanent cursePerm = new Permanent(curseCard);
        cursePerm.setAttachedTo(enchantedPlayer.getId());
        gd.playerBattlefields.get(controller.getId()).add(cursePerm);
        return cursePerm;
    }
}
