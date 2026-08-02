package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
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

    private void castCutTheTethers() {
        harness.setHand(player1, List.of(new CutTheTethers()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
