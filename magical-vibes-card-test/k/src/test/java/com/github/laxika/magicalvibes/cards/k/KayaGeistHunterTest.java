package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BladeSplicer;
import com.github.laxika.magicalvibes.cards.b.BloodFountain;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SarkhanTheMasterless;
import com.github.laxika.magicalvibes.cards.t.TravelingMinister;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KayaGeistHunter.class, BladeSplicer.class, GrizzlyBears.class, Forest.class, Shock.class,
        BloodFountain.class, SarkhanTheMasterless.class, TravelingMinister.class})
class KayaGeistHunterTest extends BaseCardTest {

    @Test
    @DisplayName("+1 grants deathtouch to your creatures and puts a counter on a target creature token")
    void plusOneGrantsDeathtouchAndCounter() {
        Permanent kaya = addReadyKaya(3);
        harness.enterBattlefieldAndReturn(player1, new BladeSplicer());
        resolveAllTriggers();

        Permanent golem = findPermanent(player1, "Phyrexian Golem");
        harness.activateAbility(player1, battlefieldIndex(kaya), 0, null, golem.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, golem, Keyword.DEATHTOUCH)).isTrue();
        assertThat(golem.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(kaya.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("+1 can be activated without choosing a token")
    void plusOneAllowsNoTarget() {
        Permanent kaya = addReadyKaya(3);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, battlefieldIndex(kaya), 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("-2 doubles tokens created under your control until end of turn")
    void minusTwoDoublesTokensUntilEndOfTurn() {
        Permanent kaya = addReadyKaya(4);

        harness.activateAbility(player1, battlefieldIndex(kaya), 1, null, null);
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(kaya);

        harness.enterBattlefieldAndReturn(player1, new BladeSplicer());
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Phyrexian Golem")).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.enterBattlefieldAndReturn(player1, new BladeSplicer());
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Phyrexian Golem")).isEqualTo(3);
    }

    @Test
    @DisplayName("-6 exiles all graveyards and creates one Spirit for each exiled card")
    void minusSixExilesGraveyardsAndCreatesSpirits() {
        Permanent kaya = addReadyKaya(7);
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        Card instant = new Shock();
        harness.setGraveyard(player1, List.of(creature, land));
        harness.setGraveyard(player2, List.of(instant));

        harness.activateAbility(player1, battlefieldIndex(kaya), 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(creature, land);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(instant);
        assertThat(countPermanents(player1, "Spirit")).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Spirit"), Keyword.FLYING)).isTrue();
    }

    @Test
    void plusOneIncludesKayaWhenSheIsACreature() {
        Permanent kaya = addReadyKaya(3);
        Permanent sarkhan = harness.addToBattlefieldAndReturn(player1, new SarkhanTheMasterless());
        sarkhan.setCounterCount(CounterType.LOYALTY, 5);

        harness.activateAbility(player1, battlefieldIndex(sarkhan), 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, kaya)).isTrue();

        harness.activateAbility(player1, battlefieldIndex(kaya), 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, kaya, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, sarkhan, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    void plusOneAffectsOnlyCreaturesControlledAtResolutionAndExpires() {
        Permanent kaya = addReadyKaya(3);
        Permanent creature = addCreatureReady(player1, new TravelingMinister());
        Permanent opponent = addCreatureReady(player2, new TravelingMinister());

        harness.activateAbility(player1, battlefieldIndex(kaya), 0, null, null);
        harness.passBothPriorities();
        Permanent laterCreature = addCreatureReady(player1, new TravelingMinister());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, laterCreature, Keyword.DEATHTOUCH)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void plusOneRejectsNontokenCreaturesAndNoncreatureTokensWithoutPayingLoyalty() {
        Permanent kaya = addReadyKaya(3);
        Permanent creature = addCreatureReady(player1, new TravelingMinister());
        harness.enterBattlefieldAndReturn(player1, new BloodFountain());
        resolveAllTriggers();
        Permanent blood = findPermanent(player1, "Blood");

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(kaya), 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(kaya), 0, null, blood.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(kaya.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void plusOneRejectsAnOpponentsCreatureToken() {
        Permanent kaya = addReadyKaya(3);
        harness.enterBattlefieldAndReturn(player2, new BladeSplicer());
        resolveAllTriggers();
        Permanent token = findPermanent(player2, "Phyrexian Golem");

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(kaya), 0, null, token.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(kaya.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    void plusOneDoesNotGrantDeathtouchWhenItsOnlyTargetLeaves() {
        Permanent kaya = addReadyKaya(3);
        harness.enterBattlefieldAndReturn(player1, new BladeSplicer());
        resolveAllTriggers();
        Permanent token = findPermanent(player1, "Phyrexian Golem");
        Permanent creature = findPermanent(player1, "Blade Splicer");

        harness.activateAbility(player1, battlefieldIndex(kaya), 0, null, token.getId());
        gd.playerBattlefields.get(player1.getId()).remove(token);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isFalse();
        assertThat(kaya.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    void minusTwoDoublesNoncreatureTokensButNotOpponentsTokensAfterKayaLeaves() {
        Permanent kaya = addReadyKaya(2);
        harness.activateAbility(player1, battlefieldIndex(kaya), 1, null, null);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Kaya, Geist Hunter");

        harness.enterBattlefieldAndReturn(player1, new BloodFountain());
        resolveAllTriggers();
        harness.enterBattlefieldAndReturn(player2, new BloodFountain());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Blood")).isEqualTo(2);
        assertThat(countPermanents(player2, "Blood")).isEqualTo(1);
    }

    @Test
    void minusSixCreatesNoTokensWhenGraveyardsAreEmpty() {
        Permanent kaya = addReadyKaya(7);
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());

        harness.activateAbility(player1, battlefieldIndex(kaya), 2, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Spirit")).isZero();
    }

    @Test
    void minusTwoEffectsMultiplyAndDoubleSpiritsFromTheUltimate() {
        Permanent firstKaya = addReadyKaya(2);
        harness.activateAbility(player1, battlefieldIndex(firstKaya), 1, null, null);
        harness.passBothPriorities();
        Permanent secondKaya = addReadyKaya(2);
        harness.activateAbility(player1, battlefieldIndex(secondKaya), 1, null, null);
        harness.passBothPriorities();
        Permanent thirdKaya = addReadyKaya(7);

        harness.activateAbility(player1, battlefieldIndex(thirdKaya), 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(firstKaya.getCard(), secondKaya.getCard());
        assertThat(countPermanents(player1, "Spirit")).isEqualTo(8);
        for (Permanent spirit : findPermanents(player1, "Spirit")) {
            assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(1);
            assertThat(gqs.hasKeyword(gd, spirit, Keyword.FLYING)).isTrue();
        }
    }

    @Test
    void minusSixCountsKayaWhenTheLoyaltyCostPutsHerInTheGraveyard() {
        Permanent kaya = addReadyKaya(6);
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());

        harness.activateAbility(player1, battlefieldIndex(kaya), 2, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Kaya, Geist Hunter");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(kaya.getCard());
        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
    }

    private Permanent addReadyKaya(int loyalty) {
        Permanent kaya = harness.addToBattlefieldAndReturn(player1, new KayaGeistHunter());
        kaya.setCounterCount(CounterType.LOYALTY, loyalty);
        kaya.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return kaya;
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
