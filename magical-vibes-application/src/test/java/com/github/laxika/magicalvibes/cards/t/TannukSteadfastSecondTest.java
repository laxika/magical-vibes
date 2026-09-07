package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CryogenRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NebulaDragon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TannukSteadfastSecond.class, GrizzlyBears.class, CryogenRelic.class, NebulaDragon.class})
class TannukSteadfastSecondTest extends BaseCardTest {

    @Test
    void otherCreaturesYouControlHaveHaste() {
        harness.addToBattlefield(player1, new TannukSteadfastSecond());
        harness.addToBattlefield(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        resolveCombat();

        harness.assertLife(player2, 18);
    }

    @Test
    void warpsArtifactFromHand() {
        CryogenRelic relic = new CryogenRelic();
        harness.addToBattlefield(player1, new TannukSteadfastSecond());
        harness.setHand(player1, List.of(relic));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(relic.getId()));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(relic.getId())).isNotNull();
    }

    @Test
    void warpsRedCreatureFromHand() {
        NebulaDragon dragon = new NebulaDragon();
        harness.addToBattlefield(player1, new TannukSteadfastSecond());
        harness.setHand(player1, List.of(dragon));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(dragon.getId()));
        harness.assertLife(player2, 17);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(dragon.getId())).isNotNull();
    }
}
