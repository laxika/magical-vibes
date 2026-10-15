package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.p.PhyrexianRager;
import com.github.laxika.magicalvibes.cards.w.WalkingBulwark;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlightPile.class, WalkingBulwark.class, PhyrexianRager.class})
class BlightPileTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent loses life for each defender you control")
    void eachOpponentLosesLifeForControlledDefenders() {
        Permanent pile = addCreatureReady(player1, new BlightPile());
        harness.addToBattlefield(player1, new WalkingBulwark());
        harness.addToBattlefield(player1, new WalkingBulwark());
        harness.addToBattlefield(player1, new PhyrexianRager());
        harness.addToBattlefield(player2, new WalkingBulwark());
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 17);
        assertThat(pile.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The defender count is read when the ability resolves")
    void countsDefendersAtResolution() {
        addCreatureReady(player1, new BlightPile());
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new WalkingBulwark());
        harness.addToBattlefield(player1, new WalkingBulwark());
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(wall);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The ability still resolves after its source leaves the battlefield")
    void resolvesWithoutSourceAndCountsOnlyRemainingDefenders() {
        Permanent pile = addCreatureReady(player1, new BlightPile());
        harness.addToBattlefield(player1, new WalkingBulwark());
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(pile);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("No life is lost when no controlled defenders remain")
    void noLifeLostWhenAllDefendersLeaveBeforeResolution() {
        Permanent pile = addCreatureReady(player1, new BlightPile());
        harness.addToBattlefield(player1, new PhyrexianRager());
        harness.addToBattlefield(player2, new WalkingBulwark());
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(pile);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Defenders entering in response increase the life loss")
    void countsDefendersThatEnterBeforeResolution() {
        addCreatureReady(player1, new BlightPile());
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.addToBattlefield(player1, new WalkingBulwark());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
