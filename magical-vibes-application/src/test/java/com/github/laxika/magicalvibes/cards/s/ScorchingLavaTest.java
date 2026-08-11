package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScorchingLavaTest extends BaseCardTest {

    @Test
    void dealsTwoDamageToTargetPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ScorchingLava()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void withoutKickerLethalDamagePutsCreatureInGraveyard() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castOnCreature(false);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    void kickedLethalDamageExilesCreatureInsteadOfPuttingItInGraveyard() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castOnCreature(true);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    void kickedDamagePreventsRegeneration() {
        Permanent skeletons = new Permanent(new DrudgeSkeletons());
        gd.playerBattlefields.get(player2.getId()).add(skeletons);
        skeletons.setRegenerationShield(1);
        castOnPermanent(true, skeletons);

        harness.assertNotOnBattlefield(player2, "Drudge Skeletons");
        harness.assertNotInGraveyard(player2, "Drudge Skeletons");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Drudge Skeletons"));
    }

    private void castOnCreature(boolean kicked) {
        castOnPermanent(kicked, gd.playerBattlefields.get(player2.getId()).getFirst());
    }

    private void castOnPermanent(boolean kicked, Permanent target) {
        harness.setHand(player1, List.of(new ScorchingLava()));
        harness.addMana(player1, ManaColor.RED, kicked ? 2 : 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        if (kicked) {
            harness.castKickedInstant(player1, 0, target.getId());
        } else {
            harness.castInstant(player1, 0, target.getId());
        }
        harness.passBothPriorities();
    }
}
