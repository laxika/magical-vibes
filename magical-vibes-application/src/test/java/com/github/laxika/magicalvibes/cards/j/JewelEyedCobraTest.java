package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.b.BannerhideKrushok;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JewelEyedCobra.class, Shock.class, BannerhideKrushok.class})
class JewelEyedCobraTest extends BaseCardTest {

    @Test
    void createsTreasureWhenItDies() {
        harness.addToBattlefield(player1, new JewelEyedCobra());

        harness.forceActivePlayer(player2);
        harness.setHand(player2, java.util.List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Jewel-Eyed Cobra"));

        harness.assertInGraveyard(player1, "Jewel-Eyed Cobra");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void deathtouchKillsBlockerWithMoreToughnessThanCobrasPower() {
        Permanent cobra = harness.addToBattlefieldAndReturn(player1, new JewelEyedCobra());
        cobra.setSummoningSick(false);
        cobra.setAttacking(true);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new BannerhideKrushok());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        resolveCombat();

        harness.assertInGraveyard(player1, "Jewel-Eyed Cobra");
        harness.assertInGraveyard(player2, "Bannerhide Krushok");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    void opponentsCobraCreatesTreasureForThatOpponentAndItProducesMana() {
        harness.addToBattlefield(player2, new JewelEyedCobra());
        harness.setHand(player1, java.util.List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Jewel-Eyed Cobra"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Jewel-Eyed Cobra");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(findPermanents(player2, "Treasure")).hasSize(1);

        harness.activateAbility(player2, 0, 0, null, null);
        harness.handleListChoice(player2, "GREEN");

        assertThat(findPermanents(player2, "Treasure")).isEmpty();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
