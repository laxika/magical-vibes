package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.b.BarbarianGuides;
import com.github.laxika.magicalvibes.cards.r.RimeDryad;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Hammerheim.class, SnowCoveredForest.class, BalduvianBears.class, RimeDryad.class, BarbarianGuides.class})
class HammerheimTest extends BaseCardTest {

    @Test
    @DisplayName("Taps for red mana")
    void tapsForRedMana() {
        Permanent hammerheim = harness.addToBattlefieldAndReturn(player1, new Hammerheim());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(hammerheim), 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Removes every basic landwalk ability until end of turn")
    void removesAllLandwalkAbilitiesUntilEndOfTurn() {
        Permanent hammerheim = harness.addToBattlefieldAndReturn(player1, new Hammerheim());
        Permanent target = addCreatureReady(player2, allLandwalkCreature());

        for (Keyword landwalk : Keyword.LANDWALK_MAP.keySet()) {
            assertThat(gqs.hasKeyword(gd, target, landwalk)).isTrue();
        }

        activateRemoval(hammerheim, target);

        for (Keyword landwalk : Keyword.LANDWALK_MAP.keySet()) {
            assertThat(gqs.hasKeyword(gd, target, landwalk)).isFalse();
        }

        endTurn();

        for (Keyword landwalk : Keyword.LANDWALK_MAP.keySet()) {
            assertThat(gqs.hasKeyword(gd, target, landwalk)).isTrue();
        }
    }

    @Test
    @DisplayName("Allows a target with snow landwalk to be blocked until end of turn")
    void allowsSnowLandwalkCreatureToBeBlocked() {
        Permanent hammerheim = harness.addToBattlefieldAndReturn(player1, new Hammerheim());
        Permanent dryad = addCreatureReady(player1, new RimeDryad());
        dryad.setAttacking(true);
        harness.addToBattlefield(player2, new SnowCoveredForest());
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());

        activateRemoval(hammerheim, dryad);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(dryad))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Snow landwalk granted after removal prevents blocking")
    void laterSnowLandwalkGrantPreventsBlocking() {
        Permanent hammerheim = harness.addToBattlefieldAndReturn(player1, new Hammerheim());
        Permanent attacker = addCreatureReady(player1, new BalduvianBears());
        Permanent guides = addCreatureReady(player1, new BarbarianGuides());
        harness.addToBattlefield(player2, new SnowCoveredForest());
        addCreatureReady(player2, new BalduvianBears());

        activateRemoval(hammerheim, attacker);
        grantSnowForestwalk(guides, attacker);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());

        assertThat(harness.getCombatBlockService()
                .getBlockableAttackerIndices(gd, player1.getId(), player2.getId()))
                .doesNotContain(gd.playerBattlefields.get(player1.getId()).indexOf(attacker));
    }

    @Test
    @DisplayName("Removal after a snow landwalk grant permits blocking")
    void removesEarlierSnowLandwalkGrant() {
        Permanent hammerheim = harness.addToBattlefieldAndReturn(player1, new Hammerheim());
        Permanent attacker = addCreatureReady(player1, new BalduvianBears());
        Permanent guides = addCreatureReady(player1, new BarbarianGuides());
        harness.addToBattlefield(player2, new SnowCoveredForest());
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        grantSnowForestwalk(guides, attacker);
        activateRemoval(hammerheim, attacker);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Printed snow landwalk returns after cleanup and other creatures retain landwalk")
    void restoresSnowLandwalkAfterCleanupAndOnlyAffectsTarget() {
        Permanent hammerheim = harness.addToBattlefieldAndReturn(player1, new Hammerheim());
        Permanent target = addCreatureReady(player1, new RimeDryad());
        Permanent other = addCreatureReady(player1, new RimeDryad());
        harness.addToBattlefield(player2, new SnowCoveredForest());
        addCreatureReady(player2, new BalduvianBears());

        activateRemoval(hammerheim, target);
        target.setAttacking(true);
        target.setAttackTarget(player2.getId());
        other.setAttacking(true);
        other.setAttackTarget(player2.getId());
        assertThat(harness.getCombatBlockService()
                .getBlockableAttackerIndices(gd, player1.getId(), player2.getId()))
                .contains(gd.playerBattlefields.get(player1.getId()).indexOf(target))
                .doesNotContain(gd.playerBattlefields.get(player1.getId()).indexOf(other));

        endTurn();
        target.setAttacking(true);
        target.setAttackTarget(player2.getId());
        other.setAttacking(true);
        other.setAttackTarget(player2.getId());
        assertThat(harness.getCombatBlockService()
                .getBlockableAttackerIndices(gd, player1.getId(), player2.getId()))
                .doesNotContain(gd.playerBattlefields.get(player1.getId()).indexOf(target))
                .doesNotContain(gd.playerBattlefields.get(player1.getId()).indexOf(other));
    }

    private void grantSnowForestwalk(Permanent guides, Permanent target) {
        harness.addMana(player1, ManaColor.RED, 3);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(guides), 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "FOREST");
    }

    private void activateRemoval(Permanent hammerheim, Permanent target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(hammerheim), 1, null, target.getId());
        harness.passBothPriorities();
    }

    private void endTurn() {
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private static Card allLandwalkCreature() {
        Card card = new Card();
        card.setName("Test Landwalker");
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(CardColor.GREEN);
        card.setPower(2);
        card.setToughness(2);
        card.setKeywords(EnumSet.copyOf(Keyword.LANDWALK_MAP.keySet()));
        return card;
    }

}
