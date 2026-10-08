package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CurseOfTheBloodyTome;
import com.github.laxika.magicalvibes.cards.c.CurseOfThePiercedHeart;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WitchbaneOrb.class, CurseOfTheBloodyTome.class, CurseOfThePiercedHeart.class, Naturalize.class})
class WitchbaneOrbTest extends BaseCardTest {

    @Test
    @DisplayName("ETB destroys Curse attached to its controller")
    void etbDestroysCurseAttachedToController() {
        placeCurseOnPlayer(player2, player1);

        harness.setHand(player1, List.of(new WitchbaneOrb()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve artifact
        harness.passBothPriorities(); // resolve ETB trigger

        // Curse should be destroyed (moved to graveyard)
        harness.assertNotOnBattlefield(player2, "Curse of the Pierced Heart");
        harness.assertInGraveyard(player2, "Curse of the Pierced Heart");
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

        harness.assertNotOnBattlefield(player2, "Curse of the Pierced Heart");
        harness.assertNotOnBattlefield(player2, "Curse of the Bloody Tome");
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
        harness.assertOnBattlefield(player1, "Curse of the Pierced Heart");
    }

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
        Permanent orbPerm = findPermanent(player1, "Witchbane Orb");
        gd.playerBattlefields.get(player1.getId()).remove(orbPerm);

        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isFalse();
    }

    @Test
    @DisplayName("Controller can target themselves with a Curse despite hexproof")
    void controllerCanTargetThemselves() {
        harness.addToBattlefield(player1, new WitchbaneOrb());
        harness.setHand(player1, List.of(new CurseOfThePiercedHeart()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Curse of the Pierced Heart").getAttachedTo())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("ETB destroys the controller's own Curse attached to them")
    void etbDestroysSelfControlledCurse() {
        placeCurseOnPlayer(player1, player1);
        harness.setHand(player1, List.of(new WitchbaneOrb()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Curse of the Pierced Heart");
        harness.assertNotOnBattlefield(player1, "Curse of the Pierced Heart");
    }

    @Test
    @DisplayName("ETB still destroys Curses after the Orb is destroyed in response")
    void etbResolvesAfterOrbLeaves() {
        placeCurseOnPlayer(player2, player1);
        harness.setHand(player1, List.of(new WitchbaneOrb()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Witchbane Orb"));
        harness.assertInGraveyard(player1, "Witchbane Orb");
        harness.assertOnBattlefield(player2, "Curse of the Pierced Heart");

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Curse of the Pierced Heart");
        harness.assertNotOnBattlefield(player2, "Curse of the Pierced Heart");
    }

    private Permanent placeCurseOnPlayer(Player controller, Player enchantedPlayer) {
        return placeCurseOnPlayer(controller, enchantedPlayer, new CurseOfThePiercedHeart());
    }

    private Permanent placeCurseOnPlayer(Player controller, Player enchantedPlayer, com.github.laxika.magicalvibes.model.Card curseCard) {
        Permanent cursePerm = harness.addToBattlefieldAndReturn(controller, curseCard);
        cursePerm.setAttachedTo(enchantedPlayer.getId());
        return cursePerm;
    }
}
