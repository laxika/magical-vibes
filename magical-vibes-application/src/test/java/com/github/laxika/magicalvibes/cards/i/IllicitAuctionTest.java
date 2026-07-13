package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

class IllicitAuctionTest extends BaseCardTest {

    private void cast(Player caster, Permanent target) {
        harness.setHand(caster, List.of(new IllicitAuction()));
        harness.addMana(caster, ManaColor.RED, 2);
        harness.addMana(caster, ManaColor.COLORLESS, 3);
        harness.castSorcery(caster, 0, target.getId());
        harness.passBothPriorities();
    }

    private boolean controls(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).stream().anyMatch(p -> p.getId().equals(perm.getId()));
    }

    @Test
    @DisplayName("If everyone passes, the caster wins the auction at 0 and steals the creature for free")
    void everyonePassesCasterStealsForFree() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent creature = addReadyCreature(player2);

        cast(player1, creature);
        // First (and only) bidder is the opponent; the caster opened as the high bidder at 0.
        harness.handleXValueChosen(player2, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(controls(player1, creature)).isTrue();
        assertThat(controls(player2, creature)).isFalse();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Bidding war: the high bidder loses life equal to the high bid; topped bidders lose nothing")
    void biddingWarHighBidderLosesLifeAndGainsControl() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent creature = addReadyCreature(player2);

        cast(player1, creature);
        harness.handleXValueChosen(player2, 5); // opponent bids to keep its creature
        harness.handleXValueChosen(player1, 8); // caster tops the bid
        harness.handleXValueChosen(player2, 0); // opponent passes; high bid stands

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(controls(player1, creature)).isTrue();
        assertThat(controls(player2, creature)).isFalse();
        harness.assertLife(player1, 12); // winner loses the high bid
        harness.assertLife(player2, 20); // losing bid costs nothing
    }

    @Test
    @DisplayName("A non-caster can win the auction; control transfers to them and only the winner loses life")
    void opponentWinsControlTransfersAway() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent creature = addReadyCreature(player1); // caster targets their own creature

        cast(player1, creature);
        harness.handleXValueChosen(player2, 5); // opponent outbids the caster's opening 0
        harness.handleXValueChosen(player1, 0); // caster passes; opponent's bid stands

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(controls(player2, creature)).isTrue();
        assertThat(controls(player1, creature)).isFalse();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("Control gained by Illicit Auction is permanent and does not wear off at cleanup")
    void controlIsPermanent() {
        Permanent creature = addReadyCreature(player2);

        cast(player1, creature);
        harness.handleXValueChosen(player2, 0);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(controls(player1, creature)).isTrue();
        assertThat(controls(player2, creature)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addReadyCreature(player1); // a legal creature target exists so the spell is castable
        Permanent land = harness.addToBattlefieldAndReturn(player2, new com.github.laxika.magicalvibes.cards.f.Forest());
        harness.setHand(player1, List.of(new IllicitAuction()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Fizzles if the target creature leaves before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent creature = addReadyCreature(player2);
        harness.setHand(player1, List.of(new IllicitAuction()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, creature.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent addReadyCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
