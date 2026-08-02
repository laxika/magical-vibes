package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CutTheTethersTest extends BaseCardTest {

    @Test
    @DisplayName("Declining the payment returns the Spirit to its owner's hand")
    void decliningBouncesTheSpirit() {
        harness.addToBattlefield(player2, new ChapelGeist());
        castCutTheTethers();

        harness.handleMayAbilityChosen(player2, false);

        harness.assertInHand(player2, "Chapel Geist");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Paying {3} keeps the Spirit on the battlefield")
    void payingKeepsTheSpirit() {
        harness.addToBattlefield(player2, new ChapelGeist());
        castCutTheTethers();

        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertOnBattlefield(player2, "Chapel Geist");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Each Spirit is a separate payment — paying for one still bounces the other")
    void eachSpiritIsAnIndependentPayment() {
        harness.addToBattlefield(player2, new ChapelGeist());
        harness.addToBattlefield(player2, new ChapelGeist());
        castCutTheTethers();

        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(countPermanents(player2, "Chapel Geist")).isEqualTo(1);
    }

    @Test
    @DisplayName("Non-Spirit permanents are untouched and never prompted for")
    void nonSpiritsAreUntouched() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castCutTheTethers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The caster's own Spirits are put to the same choice")
    void casterIsNotSpared() {
        harness.addToBattlefield(player1, new ChapelGeist());
        harness.addToBattlefield(player2, new ChapelGeist());
        castCutTheTethers();

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInHand(player1, "Chapel Geist");
        harness.assertInHand(player2, "Chapel Geist");
    }

    @Test
    @DisplayName("A stolen Spirit is decided by its owner, not by the player controlling it")
    void stolenSpiritIsDecidedByItsOwner() {
        Permanent geist = stealPlayer2Geist();

        castCutTheTethers();

        // The prompt goes to the owner, not to player1 who controls the Geist
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleMayAbilityChosen(player2, false);

        harness.assertInHand(player2, "Chapel Geist");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(geist.getId()));
    }

    @Test
    @DisplayName("The owner of a stolen Spirit pays to keep it, and it stays under the thief's control")
    void owningPlayerPaysForAStolenSpirit() {
        Permanent geist = stealPlayer2Geist();

        castCutTheTethers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(geist.getId()));
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    /** Player 2 owns the Chapel Geist; player 1 takes control of it with Control Magic. */
    private Permanent stealPlayer2Geist() {
        Permanent geist = addCreatureReady(player2, new ChapelGeist());

        harness.setHand(player1, List.of(new ControlMagic()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castEnchantment(player1, 0, geist.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(geist.getId()));
        assertThat(gd.stolenCreatures).containsEntry(geist.getId(), player2.getId());
        return geist;
    }

    private void castCutTheTethers() {
        harness.setHand(player1, List.of(new CutTheTethers()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
