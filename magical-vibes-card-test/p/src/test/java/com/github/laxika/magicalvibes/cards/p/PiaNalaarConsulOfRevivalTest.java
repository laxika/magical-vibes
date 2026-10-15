package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MaskwoodNexus;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PiaNalaarConsulOfRevival.class, Forest.class, GrizzlyBears.class, MaskwoodNexus.class})
class PiaNalaarConsulOfRevivalTest extends BaseCardTest {

    @Test
    void createsAHastyThopterWhenPlayingALandFromExile() {
        addCreatureReady(player1, new PiaNalaarConsulOfRevival());
        Forest forest = new Forest();
        gd.addToExile(player1.getId(), forest);
        gd.exilePlayPermissions.put(forest.getId(), player1.getId());

        prepareMainPhase();
        harness.castFromExile(player1, forest.getId());
        harness.passBothPriorities();

        Permanent thopter = findPermanent(player1, "Thopter");
        assertThat(gqs.hasKeyword(gd, thopter, Keyword.HASTE)).isTrue();
    }

    @Test
    void createsAHastyThopterWhenCastingASpellFromExile() {
        addCreatureReady(player1, new PiaNalaarConsulOfRevival());
        GrizzlyBears bears = new GrizzlyBears();
        gd.addToExile(player1.getId(), bears);
        gd.exilePlayPermissions.put(bears.getId(), player1.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        prepareMainPhase();
        harness.castFromExile(player1, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent thopter = findPermanent(player1, "Thopter");
        assertThat(gqs.hasKeyword(gd, thopter, Keyword.HASTE)).isTrue();
    }

    @Test
    @CardUsed({PiaNalaarConsulOfRevival.class, MaskwoodNexus.class})
    void grantsHasteToItselfWhenItIsAThopter() {
        Permanent pia = harness.addToBattlefieldAndReturn(player1, new PiaNalaarConsulOfRevival());
        harness.addToBattlefield(player1, new MaskwoodNexus());

        assertThat(gqs.hasEffectiveSubtype(gd, pia, CardSubtype.THOPTER)).isTrue();
        assertThat(gqs.hasKeyword(gd, pia, Keyword.HASTE)).isTrue();
    }

    @Test
    void doesNotTriggerForLandOrSpellPlayedFromHand() {
        addCreatureReady(player1, new PiaNalaarConsulOfRevival());
        harness.setHand(player1, List.of(new Forest(), new GrizzlyBears()));
        prepareMainPhase();
        harness.playLand(player1, 0);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Thopter")).isZero();
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Grizzly Bears"), Keyword.HASTE)).isFalse();
    }

    @Test
    void doesNotTriggerForAnOpponentsLandFromExile() {
        addCreatureReady(player1, new PiaNalaarConsulOfRevival());
        Forest forest = new Forest();
        gd.addToExile(player2.getId(), forest);
        gd.exilePlayPermissions.put(forest.getId(), player2.getId());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player2, forest.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Thopter")).isZero();
        assertThat(countPermanents(player2, "Thopter")).isZero();
    }

    @Test
    void exileCastTriggerResolvesBeforeSpellAndSurvivesPiaLeavingBattlefield() {
        Permanent pia = addCreatureReady(player1, new PiaNalaarConsulOfRevival());
        GrizzlyBears bears = new GrizzlyBears();
        gd.addToExile(player1.getId(), bears);
        gd.exilePlayPermissions.put(bears.getId(), player1.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        prepareMainPhase();
        harness.castFromExile(player1, bears.getId());
        gd.playerBattlefields.get(player1.getId()).remove(pia);
        gd.playerGraveyards.get(player1.getId()).add(pia.getCard());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Thopter")).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        Permanent thopter = findPermanent(player1, "Thopter");
        assertThat(gqs.hasKeyword(gd, thopter, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, thopter, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, thopter)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, thopter)).isEqualTo(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
