package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeaGateStormcaller.class, LightningBolt.class, LavaAxe.class})
class SeaGateStormcallerTest extends BaseCardTest {

    @Test
    void copiesNextLowManaValueInstantOnceWithoutKicker() {
        castStormcaller(false);

        castLightningBolt();
        resolveCopies(1);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    void copiesNextLowManaValueInstantTwiceWhenKicked() {
        castStormcaller(true);

        castLightningBolt();
        resolveCopies(2);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(11);
    }

    @Test
    void waitsForTheNextInstantOrSorceryWithManaValueTwoOrLess() {
        castStormcaller(false);

        harness.setHand(player1, List.of(new LavaAxe()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        castLightningBolt();
        resolveCopies(1);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(9);
    }

    private void castStormcaller(boolean kicked) {
        harness.setHand(player1, List.of(new SeaGateStormcaller()));
        harness.addMana(player1, ManaColor.BLUE, kicked ? 2 : 1);
        harness.addMana(player1, ManaColor.COLORLESS, kicked ? 5 : 1);

        if (kicked) {
            harness.castKickedCreature(player1, 0);
        } else {
            harness.castCreature(player1, 0);
        }
        resolveAllTriggers();
    }

    private void castLightningBolt() {
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
    }

    private void resolveCopies(int copyCount) {
        harness.passBothPriorities();
        for (int i = 0; i < copyCount; i++) {
            harness.handleMayAbilityChosen(player1, false);
        }
        resolveAllTriggers();
    }
}
