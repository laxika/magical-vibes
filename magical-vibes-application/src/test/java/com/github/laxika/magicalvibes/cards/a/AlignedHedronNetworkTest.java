package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AlignedHedronNetwork.class, ColossalDreadmaw.class, CrawWurm.class, GrizzlyBears.class, Shatter.class})
class AlignedHedronNetworkTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles every creature with power 5 or greater")
    void etbExilesCreaturesWithPowerFiveOrGreater() {
        harness.addToBattlefield(player1, new CrawWurm());
        harness.addToBattlefield(player2, new ColossalDreadmaw());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castAndResolveNetwork();

        harness.assertNotOnBattlefield(player1, "Craw Wurm");
        harness.assertNotOnBattlefield(player2, "Colossal Dreadmaw");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Aligned Hedron Network");
    }

    @Test
    @DisplayName("Exiled creatures return under their owners' control when the network leaves")
    void exiledCreaturesReturnWhenNetworkLeaves() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new CrawWurm());
        wurm.tap();
        harness.addToBattlefield(player2, new ColossalDreadmaw());

        castAndResolveNetwork();

        UUID networkId = harness.getPermanentId(player1, "Aligned Hedron Network");
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, networkId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Craw Wurm");
        harness.assertOnBattlefield(player2, "Colossal Dreadmaw");
        assertThat(findPermanent(player1, "Craw Wurm").isTapped()).isFalse();
    }

    private void castAndResolveNetwork() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new AlignedHedronNetwork()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
