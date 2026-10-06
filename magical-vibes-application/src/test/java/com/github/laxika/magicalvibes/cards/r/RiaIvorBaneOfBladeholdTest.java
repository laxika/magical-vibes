package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BladeholdWarWhip;
import com.github.laxika.magicalvibes.cards.c.ChromeProwler;
import com.github.laxika.magicalvibes.cards.d.DuelistOfDeepFaith;
import com.github.laxika.magicalvibes.cards.o.OrthodoxyEnforcer;
import com.github.laxika.magicalvibes.cards.t.TheFairBasilica;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RiaIvorBaneOfBladehold.class, ChromeProwler.class, DuelistOfDeepFaith.class,
        OrthodoxyEnforcer.class, TheFairBasilica.class, BladeholdWarWhip.class})
class RiaIvorBaneOfBladeholdTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents a target creature's combat damage to a player and creates one Mite per damage")
    void preventsCombatDamageAndCreatesMites() {
        addCreatureReady(player1, new RiaIvorBaneOfBladehold());
        Permanent attacker = addCreatureReady(player1, new ChromeProwler());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        declareAttackers(List.of(1));
        resolveCombat();

        List<Permanent> mites = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(mites).hasSize(3);
        assertThat(mites).allSatisfy(mite -> {
            assertThat(mite.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(mite.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
            assertThat(mite.getCard().getSubtypes()).contains(com.github.laxika.magicalvibes.model.CardSubtype.PHYREXIAN,
                    com.github.laxika.magicalvibes.model.CardSubtype.MITE);
            assertThat(mite.hasKeyword(Keyword.TOXIC)).isTrue();
            assertThat(bls.canBlock(gd, mite)).isFalse();
        });
    }

    @Test
    @DisplayName("Does not prevent combat damage dealt to a blocking creature")
    void onlyPreventsDamageToPlayers() {
        addCreatureReady(player1, new RiaIvorBaneOfBladehold());
        Permanent attacker = addCreatureReady(player1, new ChromeProwler());
        Permanent blocker = addCreatureReady(player2, new OrthodoxyEnforcer());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .noneMatch(permanent -> permanent.getCard().isToken())).isTrue();
        assertThat(blocker.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's combat")
    void doesNotTriggerDuringOpponentsCombat() {
        addCreatureReady(player1, new RiaIvorBaneOfBladehold());
        addCreatureReady(player1, new ChromeProwler());

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a noncreature")
    void cannotTargetNoncreature() {
        addCreatureReady(player1, new RiaIvorBaneOfBladehold());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new TheFairBasilica());

        advanceToCombat(player1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Battle cry increases another attacker's prevented damage without boosting Ria Ivor")
    void battleCryIncreasesMiteCount() {
        addCreatureReady(player1, new RiaIvorBaneOfBladehold());
        Permanent attacker = addCreatureReady(player1, new ChromeProwler());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();
        resolveCombat();

        harness.assertLife(player2, 17);
        assertThat(countPermanents(player1, "Mite")).isEqualTo(4);
    }

    @Test
    @DisplayName("Ria Ivor can prevent its own damage and create Mites")
    void canTargetItself() {
        Permanent ria = addCreatureReady(player1, new RiaIvorBaneOfBladehold());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, ria.getId());
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        resolveAllTriggers();
        resolveCombat();

        harness.assertLife(player2, 20);
        assertThat(countPermanents(player1, "Mite")).isEqualTo(3);
    }

    @Test
    @DisplayName("An opponent's creature is a legal target and does not shield another attacker")
    void canTargetOpponentsCreature() {
        addCreatureReady(player1, new RiaIvorBaneOfBladehold());
        addCreatureReady(player1, new ChromeProwler());
        Permanent opponentCreature = addCreatureReady(player2, new ChromeProwler());
        opponentCreature.setTapped(true);

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        declareAttackers(List.of(1));
        resolveCombat();

        harness.assertLife(player2, 17);
        assertThat(countPermanents(player1, "Mite")).isZero();
    }

    @Test
    @DisplayName("Preventing a toxic creature's first-strike damage also prevents poison counters")
    void preventedToxicDamageGivesNoPoison() {
        addCreatureReady(player1, new RiaIvorBaneOfBladehold());
        Permanent attacker = addCreatureReady(player1, new DuelistOfDeepFaith());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        declareAttackers(List.of(1));
        resolveCombat();

        harness.assertLife(player2, 20);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
        assertThat(countPermanents(player1, "Mite")).isEqualTo(2);
    }

    @Test
    @DisplayName("Only the first damage step of a double-striking creature is prevented")
    void onlyPreventsNextDamageEvent() {
        addCreatureReady(player1, new RiaIvorBaneOfBladehold());
        Permanent attacker = addCreatureReady(player1, new ChromeProwler());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new BladeholdWarWhip());
        equipment.setAttachedTo(attacker.getId());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        declareAttackers(List.of(1));
        resolveCombat();
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(countPermanents(player1, "Mite")).isEqualTo(3);
    }

    @Test
    @DisplayName("Created Mites give poison immediately as a result of combat damage")
    void mitesToxicDoesNotUseTheStack() {
        addCreatureReady(player1, new RiaIvorBaneOfBladehold());
        Permanent attacker = addCreatureReady(player1, new ChromeProwler());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();
        declareAttackers(List.of(1));
        resolveCombat();

        Permanent mite = findPermanent(player1, "Mite");
        gd.playerBattlefields.get(player1.getId()).forEach(permanent -> permanent.setAttacking(false));
        mite.setSummoningSick(false);
        mite.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        harness.assertLife(player2, 19);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
