package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FolkOfAnHavva;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({AnHavvaInn.class, AnabaAncestor.class, FolkOfAnHavva.class})
class AnHavvaInnTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 1 life when no green creatures are on the battlefield")
    void gainsOneWithNoGreenCreatures() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player2, new AnabaAncestor());

        harness.castFromHand(player1, new AnHavvaInn(), "{1}{G}{G}");
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Counts green creatures on every battlefield, plus one")
    void countsGreenCreaturesOnAllBattlefields() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new FolkOfAnHavva());
        harness.addToBattlefield(player1, new FolkOfAnHavva());
        harness.addToBattlefield(player2, new FolkOfAnHavva());
        harness.addToBattlefield(player2, new AnabaAncestor());

        harness.castFromHand(player1, new AnHavvaInn(), "{1}{G}{G}");
        harness.passBothPriorities();

        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Counts green creatures when the spell resolves")
    void countsGreenCreaturesAtResolution() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new FolkOfAnHavva());

        harness.castFromHand(player1, new AnHavvaInn(), "{1}{G}{G}");
        harness.addToBattlefield(player2, new FolkOfAnHavva());
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
    }
}
