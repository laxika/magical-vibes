package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AetherSyphonTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Aether Syphon draws a card and taps it")
    void activatingDrawsAndTaps() {
        Permanent syphon = addSyphon();
        harness.setLibrary(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card instanceof LlanowarElves);
        assertThat(syphon.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("At max speed, drawing with Aether Syphon mills two cards from each opponent")
    void maxSpeedMillsEachOpponent() {
        addSyphon();
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.setLibrary(player1, List.of(new LlanowarElves()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Aether Syphon does not mill below max speed")
    void belowMaxSpeedDoesNotMill() {
        addSyphon();
        gd.playerSpeeds.put(player1.getId(), 3);
        harness.setLibrary(player1, List.of(new LlanowarElves()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    private Permanent addSyphon() {
        Permanent syphon = new Permanent(new AetherSyphon());
        syphon.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(syphon);
        return syphon;
    }
}
