package com.github.laxika.magicalvibes.cards.k;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.a.ArtificialEvolution;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.Lignify;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({KarrthusTyrantOfJund.class, ShivanDragon.class, GrizzlyBears.class,
        Unsummon.class, Lignify.class, ArtificialEvolution.class})
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
        harness.assertOnBattlefield(player1, "Shivan Dragon");
        harness.assertNotOnBattlefield(player2, "Shivan Dragon");
        // And it was untapped
        assertThat(enemyDragon.isTapped()).isFalse();
    }

    @Test
    @DisplayName("ETB leaves a non-Dragon the opponent controls untouched")
    void etbDoesNotStealNonDragons() {
        Permanent enemyBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castKarrthus();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(enemyBears);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
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

    @Test
    void untapsOwnedDragonsAndGainsAllOpposingDragons() {
        Permanent ownDragon = harness.addToBattlefieldAndReturn(player1, new ShivanDragon());
        Permanent firstEnemy = harness.addToBattlefieldAndReturn(player2, new ShivanDragon());
        Permanent secondEnemy = harness.addToBattlefieldAndReturn(player2, new ShivanDragon());
        Permanent enemyBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        ownDragon.tap();
        firstEnemy.tap();
        secondEnemy.tap();
        enemyBears.tap();

        castKarrthus();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownDragon, firstEnemy, secondEnemy);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(enemyBears)
                .doesNotContain(firstEnemy, secondEnemy);
        assertThat(ownDragon.isTapped()).isFalse();
        assertThat(firstEnemy.isTapped()).isFalse();
        assertThat(secondEnemy.isTapped()).isFalse();
        assertThat(enemyBears.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, firstEnemy, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, secondEnemy, Keyword.HASTE)).isTrue();
    }

    @Test
    void controlPersistsButGrantedHasteEndsWhenKarrthusLeaves() {
        Permanent enemyDragon = harness.addToBattlefieldAndReturn(player2, new ShivanDragon());
        castKarrthus();
        assertThat(gqs.hasKeyword(gd, enemyDragon, Keyword.HASTE)).isTrue();

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Karrthus, Tyrant of Jund"));

        harness.assertNotOnBattlefield(player1, "Karrthus, Tyrant of Jund");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(enemyDragon);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(enemyDragon);
        assertThat(gqs.hasKeyword(gd, enemyDragon, Keyword.HASTE)).isFalse();
    }

    @Test
    void triggerStillGainsAndUntapsDragonsWhenKarrthusLeavesBeforeResolution() {
        Permanent enemyDragon = harness.addToBattlefieldAndReturn(player2, new ShivanDragon());
        enemyDragon.tap();
        harness.setHand(player1, List.of(new KarrthusTyrantOfJund(), new Unsummon()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Karrthus, Tyrant of Jund"));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(enemyDragon);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(enemyDragon);
        assertThat(enemyDragon.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, enemyDragon, Keyword.HASTE)).isFalse();
    }

    @Test
    void gainsAndUntapsNoncreatureDragonPermanents() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new Lignify()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.castEnchantment(player2, 0, bears.getId());
        harness.passBothPriorities();
        Permanent aura = findPermanent(player2, "Lignify");

        harness.setHand(player2, List.of(new ArtificialEvolution()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, aura.getId());
        harness.handleListChoice(player2, "TREEFOLK");
        harness.handleListChoice(player2, "DRAGON");
        assertThat(gqs.hasEffectiveSubtype(gd, aura, CardSubtype.DRAGON)).isTrue();
        assertThat(gqs.isCreature(gd, aura)).isFalse();
        aura.tap();

        harness.forceActivePlayer(player1);
        castKarrthus();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(aura);
        assertThat(aura.isTapped()).isFalse();
    }
}
