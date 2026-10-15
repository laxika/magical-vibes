package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.c.CityOfTraitors;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.w.WelkinHawk;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZealotsEnDal.class, WelkinHawk.class, RagingGoblin.class, CityOfTraitors.class, Spellbook.class})
class ZealotsEnDalTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 1 life when all nonland permanents are white")
    void gainsLifeWithOnlyWhiteNonlandPermanents() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new ZealotsEnDal());
        harness.addToBattlefield(player1, new WelkinHawk());
        harness.addToBattlefield(player1, new CityOfTraitors());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Does not trigger with a nonwhite nonland permanent")
    void doesNotGainLifeWithNonwhiteNonlandPermanent() {
        harness.addToBattlefield(player1, new ZealotsEnDal());
        harness.addToBattlefield(player1, new RagingGoblin());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Does nothing if a nonwhite nonland permanent appears before resolution")
    void doesNothingIfConditionFailsBeforeResolution() {
        harness.addToBattlefield(player1, new ZealotsEnDal());
        harness.addToBattlefield(player1, new WelkinHawk());

        advanceToUpkeep(player1);
        harness.addToBattlefield(player1, new RagingGoblin());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Ignores nonwhite permanents controlled by an opponent")
    void ignoresOpponentsNonwhitePermanents() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new ZealotsEnDal());
        harness.addToBattlefield(player1, new WelkinHawk());
        harness.addToBattlefield(player2, new RagingGoblin());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Colorless nonland permanents prevent the upkeep trigger")
    void doesNotTriggerWithColorlessArtifact() {
        harness.addToBattlefield(player1, new ZealotsEnDal());
        harness.addToBattlefield(player1, new Spellbook());

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Removing a nonwhite permanent after upkeep begins cannot create a trigger")
    void doesNotTriggerRetroactivelyWhenConditionBecomesTrue() {
        harness.addToBattlefield(player1, new ZealotsEnDal());
        harness.addToBattlefield(player1, new RagingGoblin());

        advanceToUpkeep(player1);
        assertThat(gd.stack).isEmpty();
        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Raging Goblin"));
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new ZealotsEnDal());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The trigger resolves after its source leaves with no nonland permanents remaining")
    void gainsLifeWithNoNonlandPermanentsAtResolution() {
        harness.addToBattlefield(player1, new ZealotsEnDal());
        harness.addToBattlefield(player1, new CityOfTraitors());

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Zealots en-Dal"));
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
    }
}
