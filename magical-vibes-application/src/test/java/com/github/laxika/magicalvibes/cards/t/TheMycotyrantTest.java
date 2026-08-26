package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BroodrageMycoid;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YavimayaSapherd;
import com.github.laxika.magicalvibes.cards.z.ZuranOrb;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheMycotyrant.class, BroodrageMycoid.class, GrizzlyBears.class, YavimayaSapherd.class,
        ZuranOrb.class, Forest.class})
class TheMycotyrantTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness count controlled Fungi and Saprolings")
    void powerAndToughnessCountFungiAndSaprolings() {
        Permanent mycotyrant = harness.addToBattlefieldAndReturn(player1, new TheMycotyrant());
        harness.addToBattlefield(player1, new BroodrageMycoid());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new YavimayaSapherd()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mycotyrant)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, mycotyrant)).isEqualTo(4);
    }

    @Test
    @DisplayName("Creates one nonblocking Fungus token for each descent at your end step")
    void createsTokensForEachDescent() {
        harness.addToBattlefield(player1, new TheMycotyrant());
        harness.addToBattlefield(player1, new ZuranOrb());
        Permanent firstLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new ZuranOrb());
        harness.addToBattlefield(player1, new Forest());

        harness.activateAbility(player1, 1, null, null);
        harness.handlePermanentChosen(player1, firstLand.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        advanceToEndStep(player1);
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Fungus").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> assertThat(bls.canBlock(gd, token)).isFalse());
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
