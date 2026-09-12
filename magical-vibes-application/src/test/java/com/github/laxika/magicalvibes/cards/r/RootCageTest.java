package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.m.MercenaryInformer;
import com.github.laxika.magicalvibes.cards.m.MarshBoa;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RootCage.class, MarshBoa.class, MercenaryInformer.class})
class RootCageTest extends BaseCardTest {

    @Test
    @DisplayName("Mercenaries stay tapped through their controller's untap step")
    void mercenariesDontUntap() {
        harness.addToBattlefield(player1, new RootCage());
        Permanent mercenary = addCreatureReady(player1, new MercenaryInformer());
        mercenary.tap();

        advanceToUpkeep(player1);

        assertThat(mercenary.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Root Cage's restriction remains active while Root Cage is tapped")
    void restrictionRemainsWhileRootCageIsTapped() {
        Permanent rootCage = harness.addToBattlefieldAndReturn(player1, new RootCage());
        rootCage.tap();
        Permanent mercenary = addCreatureReady(player1, new MercenaryInformer());
        mercenary.tap();

        advanceToUpkeep(player1);

        assertThat(mercenary.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Non-Mercenary creatures untap normally")
    void nonMercenariesUntap() {
        harness.addToBattlefield(player1, new RootCage());
        Permanent creature = addCreatureReady(player1, new MarshBoa());
        creature.tap();

        advanceToUpkeep(player1);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Root Cage affects opponents' Mercenaries")
    void affectsOpponentMercenaries() {
        harness.addToBattlefield(player1, new RootCage());
        Permanent mercenary = addCreatureReady(player2, new MercenaryInformer());
        mercenary.tap();

        advanceToUpkeep(player2);

        assertThat(mercenary.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mercenaries untap after Root Cage leaves the battlefield")
    void untapsAfterRootCageLeaves() {
        Permanent rootCage = harness.addToBattlefieldAndReturn(player1, new RootCage());
        Permanent mercenary = addCreatureReady(player1, new MercenaryInformer());
        mercenary.tap();
        gd.playerBattlefields.get(player1.getId()).remove(rootCage);

        advanceToUpkeep(player1);

        assertThat(mercenary.isTapped()).isFalse();
    }
}
