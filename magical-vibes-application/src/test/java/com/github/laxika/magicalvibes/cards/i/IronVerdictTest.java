package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IronVerdictTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 5 damage to a tapped creature")
    void dealsDamageToTappedCreature() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.tap();

        harness.setHand(player1, List.of(new IronVerdict()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target an untapped creature")
    void cannotTargetUntappedCreature() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new IronVerdict()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Target must be a tapped creature");
    }

    @Test
    @DisplayName("Can be foretold and cast for white mana on a later turn")
    void foretellsAndCastsOnLaterTurn() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.tap();
        IronVerdict verdict = new IronVerdict();
        harness.setHand(player1, List.of(verdict));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.foretell(player1, 0);

        ExiledCardEntry entry = gd.findExiledCard(verdict.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isTrue();

        gd.turnNumber++;
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castFromExile(player1, verdict.getId(), bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
