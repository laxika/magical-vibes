package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.ScornfulEgotist;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TendrilsOfAgony.class, ScornfulEgotist.class})
class TendrilsOfAgonyTest extends BaseCardTest {

    @Test
    @DisplayName("Target player loses 2 life and the controller gains 2 life")
    void targetPlayerLosesLifeAndControllerGainsLife() {
        harness.setLife(player1, 16);
        harness.setLife(player2, 20);
        castTendrilsOfAgony(player2.getId());

        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Storm creates one copy for each spell cast before Tendrils of Agony")
    void stormCreatesCopiesForEachPriorSpell() {
        gd.recordSpellCast(player1.getId(), new TendrilsOfAgony());
        gd.recordSpellCast(player2.getId(), new TendrilsOfAgony());

        castTendrilsOfAgony(player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);
    }

    @Test
    @DisplayName("Each Storm copy resolves Tendrils of Agony's life loss and life gain")
    void stormCopyResolvesLifeDrain() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        gd.recordSpellCast(player2.getId(), new TendrilsOfAgony());

        castTendrilsOfAgony(player2.getId());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("A Storm copy may be retargeted to another player")
    void stormCopyMayBeRetargeted() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        gd.recordSpellCast(player2.getId(), new TendrilsOfAgony());

        castTendrilsOfAgony(player2.getId());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("Tendrils of Agony cannot target a permanent")
    void cannotTargetPermanent() {
        var creature = harness.addToBattlefieldAndReturn(player2, new ScornfulEgotist());

        harness.setHand(player1, List.of(new TendrilsOfAgony()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castTendrilsOfAgony(UUID targetId) {
        harness.setHand(player1, List.of(new TendrilsOfAgony()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castSorcery(player1, 0, targetId);
    }
}
