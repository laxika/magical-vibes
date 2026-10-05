package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.h.HuntingTriad;
import com.github.laxika.magicalvibes.cards.m.MosquitoGuard;
import com.github.laxika.magicalvibes.cards.b.BurrentonBombardier;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LysAlanaBowmaster.class, ElvishWarrior.class, BurrentonBombardier.class, MosquitoGuard.class, HuntingTriad.class})
class LysAlanaBowmasterTest extends BaseCardTest {

    /** Casts Elvish Warrior (an Elf spell) from player1's hand. */
    private void castElfSpell() {
        harness.castFromHand(player1, new ElvishWarrior(), "{G}{G}");
    }

    @Test
    @DisplayName("Casting an Elf spell triggers the may ability")
    void elfSpellTriggers() {
        harness.addToBattlefield(player1, new LysAlanaBowmaster());
        harness.addToBattlefield(player2, new BurrentonBombardier());

        castElfSpell();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Burrenton Bombardier"));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting deals 2 damage to the target flyer")
    void acceptDealsDamageToFlyer() {
        harness.addToBattlefield(player1, new LysAlanaBowmaster());
        harness.addToBattlefield(player2, new BurrentonBombardier());
        UUID flyerId = harness.getPermanentId(player2, "Burrenton Bombardier");

        castElfSpell();
        harness.handlePermanentChosen(player1, flyerId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player2, "Burrenton Bombardier");
    }

    @Test
    @DisplayName("Declining leaves the target unharmed")
    void declineLeavesTarget() {
        harness.addToBattlefield(player1, new LysAlanaBowmaster());
        harness.addToBattlefield(player2, new BurrentonBombardier());

        castElfSpell();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Burrenton Bombardier"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Burrenton Bombardier");
    }

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetNonFlyer() {
        harness.addToBattlefield(player1, new LysAlanaBowmaster());
        harness.addToBattlefield(player2, new MosquitoGuard());
        harness.addToBattlefield(player2, new BurrentonBombardier());

        castElfSpell();
        UUID nonFlyerId = harness.getPermanentId(player2, "Mosquito Guard");

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonFlyerId))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Burrenton Bombardier"));
    }

    @Test
    @DisplayName("Casting a non-Elf spell does not trigger")
    void nonElfSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new LysAlanaBowmaster());
        harness.addToBattlefield(player2, new BurrentonBombardier());
        harness.castFromHand(player1, new MosquitoGuard(), "{W}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("An opponent's Elf spell does not trigger the ability")
    void opponentElfDoesNotTrigger() {
        harness.addToBattlefield(player1, new LysAlanaBowmaster());
        harness.addToBattlefield(player1, new BurrentonBombardier());

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new ElvishWarrior(), "{G}{G}");

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Casting Bowmaster itself does not trigger its battlefield ability")
    void ownCastDoesNotTrigger() {
        harness.addToBattlefield(player2, new BurrentonBombardier());

        harness.castFromHand(player1, new LysAlanaBowmaster(), "{2}{G}");

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The ability can damage a flyer its controller controls")
    void canTargetOwnFlyer() {
        harness.addToBattlefield(player1, new LysAlanaBowmaster());
        harness.addToBattlefield(player1, new BurrentonBombardier());

        castElfSpell();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Burrenton Bombardier"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Burrenton Bombardier");
    }

    @Test
    @DisplayName("Without a legal flying target there is no optional damage choice")
    void noFlyerMeansNoChoice() {
        harness.addToBattlefield(player1, new LysAlanaBowmaster());
        harness.addToBattlefield(player2, new MosquitoGuard());

        castElfSpell();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Elvish Warrior");
    }

    @Test
    @DisplayName("A noncreature Elf spell also triggers the damage ability")
    void kindredElfSpellTriggers() {
        harness.addToBattlefield(player1, new LysAlanaBowmaster());
        harness.addToBattlefield(player2, new BurrentonBombardier());

        harness.castFromHand(player1, new HuntingTriad(), "{3}{G}");
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Burrenton Bombardier"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player2, "Burrenton Bombardier");
        harness.assertNotInGraveyard(player1, "Hunting Triad");
    }
}
