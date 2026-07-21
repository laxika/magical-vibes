package com.github.laxika.magicalvibes.cards.k;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class KarrthusTyrantOfJundTest extends BaseCardTest {

    private void castKarrthus() {
        harness.setHand(player1, List.of(new KarrthusTyrantOfJund()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve Karrthus — it enters, ETB triggers
        harness.passBothPriorities(); // resolve ETB — gain control + untap
    }

    @Test
    @DisplayName("ETB gains control of an opponent's Dragon and untaps it")
    void etbStealsAndUntapsOpponentDragon() {
        Permanent enemyDragon = harness.addToBattlefieldAndReturn(player2, new ShivanDragon());
        enemyDragon.tap();

        castKarrthus();

        // Control moved to player1
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Shivan Dragon"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Shivan Dragon"));
        // And it was untapped
        assertThat(enemyDragon.isTapped()).isFalse();
    }

    @Test
    @DisplayName("ETB leaves a non-Dragon the opponent controls untouched")
    void etbDoesNotStealNonDragons() {
        Permanent enemyBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castKarrthus();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(enemyBears);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Grants haste to other Dragon creatures you control, not to non-Dragons")
    void grantsHasteToOtherDragons() {
        Permanent myDragon = harness.addToBattlefieldAndReturn(player1, new ShivanDragon());
        Permanent myBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new KarrthusTyrantOfJund());

        assertThat(gqs.hasKeyword(gd, myDragon, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, myBears, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Does not grant haste to a Dragon an opponent controls")
    void doesNotGrantHasteToOpponentDragon() {
        Permanent enemyDragon = harness.addToBattlefieldAndReturn(player2, new ShivanDragon());
        harness.addToBattlefield(player1, new KarrthusTyrantOfJund());

        assertThat(gqs.hasKeyword(gd, enemyDragon, Keyword.HASTE)).isFalse();
    }
}
