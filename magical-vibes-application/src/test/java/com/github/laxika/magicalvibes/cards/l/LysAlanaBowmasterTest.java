package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LysAlanaBowmasterTest extends BaseCardTest {

    /** Casts Elvish Warrior (an Elf spell) from player1's hand. */
    private void castElfSpell() {
        harness.setHand(player1, List.of(new ElvishWarrior()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
    }

    @Test
    @DisplayName("Casting an Elf spell triggers the may ability")
    void elfSpellTriggers() {
        harness.addToBattlefield(player1, new LysAlanaBowmaster());
        harness.addToBattlefield(player2, new SuntailHawk());

        castElfSpell();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting deals 2 damage to the target flyer")
    void acceptDealsDamageToFlyer() {
        harness.addToBattlefield(player1, new LysAlanaBowmaster());
        harness.addToBattlefield(player2, new SuntailHawk());
        UUID hawkId = harness.getPermanentId(player2, "Suntail Hawk");

        castElfSpell();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, hawkId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Suntail Hawk");
    }

    @Test
    @DisplayName("Declining leaves the target unharmed")
    void declineLeavesTarget() {
        harness.addToBattlefield(player1, new LysAlanaBowmaster());
        harness.addToBattlefield(player2, new SuntailHawk());

        castElfSpell();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Suntail Hawk");
    }

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetNonFlyer() {
        harness.addToBattlefield(player1, new LysAlanaBowmaster());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castElfSpell();
        harness.handleMayAbilityChosen(player1, true);
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, bearsId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Casting a non-Elf spell does not trigger")
    void nonElfSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new LysAlanaBowmaster());
        harness.addToBattlefield(player2, new SuntailHawk());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }
}
