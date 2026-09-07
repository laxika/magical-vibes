package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.w.WurmcoilEngine;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KarnLegacyReforged.class, GrizzlyBears.class, MindStone.class, WurmcoilEngine.class})
class KarnLegacyReforgedTest extends BaseCardTest {

    @Test
    void powerAndToughnessUseTheGreatestManaValueAmongArtifactsYouControl() {
        Permanent karn = harness.addToBattlefieldAndReturn(player1, new KarnLegacyReforged());
        Permanent wurmcoil = harness.addToBattlefieldAndReturn(player1, new WurmcoilEngine());

        assertThat(gqs.getEffectivePower(gd, karn)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, karn)).isEqualTo(6);

        gd.playerBattlefields.get(player1.getId()).remove(wurmcoil);

        assertThat(gqs.getEffectivePower(gd, karn)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, karn)).isEqualTo(5);
    }

    @Test
    void upkeepAddsPowerstoneManaForEachArtifactAndItSurvivesUntilEndOfTurn() {
        harness.addToBattlefield(player1, new KarnLegacyReforged());
        harness.addToBattlefield(player1, new WurmcoilEngine());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.getPowerstoneOnlyColorless()).isEqualTo(2);
        assertThat(pool.getPersistentPowerstoneOnlyColorless()).isEqualTo(2);

        pool.drainNonPersistent();
        assertThat(pool.getPowerstoneOnlyColorless()).isEqualTo(2);

        pool.clearPersistentMana();
        pool.drainNonPersistent();
        assertThat(pool.getPowerstoneOnlyColorless()).isZero();
    }

    @Test
    void karnsManaCanPayForArtifactsButNotNonartifactSpells() {
        harness.addToBattlefield(player1, new KarnLegacyReforged());
        harness.addToBattlefield(player1, new WurmcoilEngine());
        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.setHand(player1, java.util.List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player1, java.util.List.of(new MindStone()));
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Mind Stone")).hasSize(1);
    }
}
