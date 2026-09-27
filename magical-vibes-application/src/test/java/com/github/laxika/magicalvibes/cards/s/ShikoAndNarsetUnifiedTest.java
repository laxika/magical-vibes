package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShikoAndNarsetUnified.class, DarkRitual.class, LightningBolt.class})
class ShikoAndNarsetUnifiedTest extends BaseCardTest {

    @Test
    void drawsWhenTheSecondSpellDoesNotTarget() {
        harness.addToBattlefield(player1, new ShikoAndNarsetUnified());
        harness.setLibrary(player1, List.of(new DarkRitual(), new DarkRitual()));
        harness.setHand(player1, List.of(new DarkRitual(), new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void decliningCopyDrawsInstead() {
        harness.addToBattlefield(player1, new ShikoAndNarsetUnified());
        harness.setLibrary(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void acceptingCopyCopiesTheTargetedSpell() {
        harness.addToBattlefield(player1, new ShikoAndNarsetUnified());
        harness.setLibrary(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, false);
        }
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(11);
    }
}
