package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SmolderingSpiresTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and makes the chosen creature unable to block this turn")
    void entersTappedAndStopsTargetCreatureFromBlocking() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SmolderingSpires()));

        harness.playLand(player1, 0);

        Permanent spires = findPermanent(player1, "Smoldering Spires");
        assertThat(spires.isTapped()).isTrue();

        harness.handlePermanentChosen(player1, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Tapping the land adds one red mana")
    void tapsForRedMana() {
        Permanent spires = new Permanent(new SmolderingSpires());
        spires.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(spires);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(spires.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }
}
