package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.Lhurgoyf;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Pyrogoyf.class, Lhurgoyf.class, Forest.class, Shock.class, GrizzlyBears.class})
class PyrogoyfTest extends BaseCardTest {

    @Test
    @DisplayName("Pyrogoyf counts distinct card types in all graveyards")
    void countsDistinctCardTypesInAllGraveyards() {
        Permanent pyrogoyf = addCreatureReady(player1, new Pyrogoyf());
        harness.setGraveyard(player1, List.of(new Forest(), new Shock()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new Forest()));

        assertThat(gqs.getEffectivePower(gd, pyrogoyf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, pyrogoyf)).isEqualTo(4);
    }

    @Test
    @DisplayName("Pyrogoyf deals the entering Lhurgoyf's power to any target")
    void lhurgoyfEntryDealsItsPowerToAnyTarget() {
        harness.addToBattlefield(player1, new Pyrogoyf());
        setThreeCardTypesInGraveyards();
        harness.setHand(player1, List.of(new Lhurgoyf()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        resolveUntilInputOrEmpty();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        resolveUntilInputOrEmpty();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Pyrogoyf triggers for itself entering the battlefield")
    void selfEntryDealsItsPowerToAnyTarget() {
        setThreeCardTypesInGraveyards();
        harness.setHand(player1, List.of(new Pyrogoyf()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveUntilInputOrEmpty();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        resolveUntilInputOrEmpty();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Pyrogoyf does not trigger for a non-Lhurgoyf creature")
    void doesNotTriggerForNonLhurgoyf() {
        harness.addToBattlefield(player1, new Pyrogoyf());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        resolveUntilInputOrEmpty();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    private void setThreeCardTypesInGraveyards() {
        harness.setGraveyard(player1, List.of(new Forest(), new Shock()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
    }

    private void resolveUntilInputOrEmpty() {
        for (int i = 0; i < 12; i++) {
            GameData gameData = harness.getGameData();
            if (gameData.interaction.isAwaitingInput() || gameData.stack.isEmpty()) {
                return;
            }
            harness.passBothPriorities();
        }
    }
}
