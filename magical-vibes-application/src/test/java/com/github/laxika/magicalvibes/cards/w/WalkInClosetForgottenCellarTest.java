package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WalkInClosetForgottenCellar.class, Forest.class, LightningBolt.class})
class WalkInClosetForgottenCellarTest extends BaseCardTest {

    @Test
    void walkInClosetAllowsPlayingALandFromTheGraveyard() {
        Forest forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));
        castRoom(0, 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.playGraveyardLand(player1, 0);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void forgottenCellarAllowsCastingFromTheGraveyardAndExilesTheSpell() {
        LightningBolt lightningBolt = new LightningBolt();
        harness.setGraveyard(player1, List.of(lightningBolt));
        castRoom(1, 5);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castFromGraveyardTargeting(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(lightningBolt);
    }

    private Permanent castRoom(int doorIndex, int manaAmount) {
        harness.setHand(player1, List.of(new WalkInClosetForgottenCellar()));
        harness.addMana(player1, ManaColor.GREEN, manaAmount);
        harness.castModalSorcery(player1, 0, doorIndex, List.of());
        harness.passBothPriorities();
        if (doorIndex == 1) {
            harness.passBothPriorities();
        }
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().contains("Walk-In Closet"))
                .findFirst()
                .orElseThrow();
    }
}
