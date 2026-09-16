package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.CavesOfKoilos;
import com.github.laxika.magicalvibes.cards.g.GerrardCapashen;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Vindicate.class, GerrardCapashen.class, CavesOfKoilos.class})
class VindicateTest extends BaseCardTest {

    @Test
    void destroysTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GerrardCapashen());

        castVindicate(target);

        harness.assertNotOnBattlefield(player2, "Gerrard Capashen");
        harness.assertInGraveyard(player2, "Gerrard Capashen");
    }

    @Test
    void destroysTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CavesOfKoilos());

        castVindicate(target);

        harness.assertNotOnBattlefield(player2, "Caves of Koilos");
        harness.assertInGraveyard(player2, "Caves of Koilos");
    }

    @Test
    void destroysYourOwnPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CavesOfKoilos());

        castVindicate(target);

        harness.assertNotOnBattlefield(player1, "Caves of Koilos");
        harness.assertInGraveyard(player1, "Caves of Koilos");
    }

    @Test
    void destructionAllowsRegeneration() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GerrardCapashen());
        target.setRegenerationShield(1);

        castVindicate(target);

        assertThat(harness.getGameData().playerBattlefields.get(player2.getId()))
                .contains(target);
        harness.assertNotInGraveyard(player2, "Gerrard Capashen");
    }

    private void castVindicate(Permanent target) {
        harness.setHand(player1, List.of(new Vindicate()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
