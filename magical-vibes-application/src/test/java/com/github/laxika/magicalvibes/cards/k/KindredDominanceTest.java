package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.b.BayouDragonfly;
import com.github.laxika.magicalvibes.cards.b.BenthicBehemoth;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

@CardUsed({KindredDominance.class, AvianChangeling.class, BayouDragonfly.class,
        BenthicBehemoth.class, Plains.class})
class KindredDominanceTest extends BaseCardTest {

    @Test
    void destroysCreaturesNotOfChosenTypeAcrossAllBattlefields() {
        harness.addToBattlefield(player1, new BenthicBehemoth());
        harness.addToBattlefield(player1, new BayouDragonfly());
        harness.addToBattlefield(player2, new BenthicBehemoth());
        harness.addToBattlefield(player2, new BayouDragonfly());
        harness.addToBattlefield(player1, new Plains());

        cast();
        harness.handleListChoice(player1, "SERPENT");

        harness.assertOnBattlefield(player1, "Benthic Behemoth");
        harness.assertOnBattlefield(player2, "Benthic Behemoth");
        harness.assertNotOnBattlefield(player1, "Bayou Dragonfly");
        harness.assertNotOnBattlefield(player2, "Bayou Dragonfly");
        harness.assertOnBattlefield(player1, "Plains");
    }

    @Test
    void changelingCountsAsChosenType() {
        harness.addToBattlefield(player1, new AvianChangeling());
        harness.addToBattlefield(player1, new BayouDragonfly());

        cast();
        harness.handleListChoice(player1, "GOBLIN");

        harness.assertOnBattlefield(player1, "Avian Changeling");
        harness.assertNotOnBattlefield(player1, "Bayou Dragonfly");
    }

    private void cast() {
        harness.setHand(player1, List.of(new KindredDominance()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
    }
}
