package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FootNinjas;
import com.github.laxika.magicalvibes.cards.f.FugitiveDroid;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DarkLeoShredder.class, FootNinjas.class, FugitiveDroid.class})
class DarkLeoShredderTest extends BaseCardTest {

    @org.junit.jupiter.api.BeforeEach
    void stopBeforeCombatDamage() {
        gd.playerAutoStopSteps.put(player2.getId(), java.util.Set.of(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_BLOCKERS));
    }

    @Test
    @DisplayName("Attacking Ninjas you control have deathtouch")
    void attackingNinjasYouControlHaveDeathtouch() {
        Permanent darkLeoShredder = addCreatureReady(player1, new DarkLeoShredder());
        Permanent ownNinja = addCreatureReady(player1, new FootNinjas());
        Permanent ownNonNinja = addCreatureReady(player1, new FugitiveDroid());
        Permanent opponentNinja = addCreatureReady(player2, new FootNinjas());

        darkLeoShredder.setAttacking(true);
        ownNinja.setAttacking(true);
        ownNonNinja.setAttacking(true);
        opponentNinja.setAttacking(true);

        assertThat(gqs.hasKeyword(gd, darkLeoShredder, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownNinja, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownNonNinja, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentNinja, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Combat damage creates a Ninja token but does not cause life loss below five Ninjas")
    void combatDamageCreatesNinjaWithoutConditionalLifeLoss() {
        harness.setLife(player2, 22);
        Permanent darkLeoShredder = addCreatureReady(player1, new DarkLeoShredder());
        darkLeoShredder.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(21);
        assertThat(findPermanents(player1, "Ninja")).hasSize(1);
    }

    @Test
    @DisplayName("The created Ninja counts toward the five-Ninja life-loss condition")
    void createdNinjaCountsTowardLifeLoss() {
        harness.setLife(player2, 22);
        addCreatureReady(player1, new FootNinjas());
        addCreatureReady(player1, new FootNinjas());
        addCreatureReady(player1, new FootNinjas());
        Permanent darkLeoShredder = addCreatureReady(player1, new DarkLeoShredder());
        darkLeoShredder.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(10);
        assertThat(findPermanents(player1, "Ninja")).hasSize(1);
    }

    @Test
    @DisplayName("Nonattacking Ninjas do not gain deathtouch")
    void nonattackingNinjasDoNotGainDeathtouch() {
        Permanent source = addCreatureReady(player1, new DarkLeoShredder());
        Permanent ninja = addCreatureReady(player1, new FootNinjas());

        assertThat(gqs.hasKeyword(gd, source, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, ninja, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("More than five Ninjas halves an even life total without affecting the controller")
    void moreThanFiveNinjasHalvesEvenLifeTotal() {
        harness.setLife(player1, 23);
        harness.setLife(player2, 21);
        for (int i = 0; i < 4; i++) {
            addCreatureReady(player1, new FootNinjas());
        }
        Permanent source = addCreatureReady(player1, new DarkLeoShredder());
        source.setAttacking(true);
        source.setAttackTarget(player2.getId());

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 10);
        harness.assertLife(player1, 23);
        assertThat(findPermanents(player1, "Ninja")).hasSize(1);
    }

    @Test
    @DisplayName("Opponent's Ninjas do not count toward the life-loss condition")
    void opposingNinjasDoNotCount() {
        harness.setLife(player2, 22);
        for (int i = 0; i < 4; i++) {
            addCreatureReady(player2, new FootNinjas());
        }
        Permanent source = addCreatureReady(player1, new DarkLeoShredder());
        source.setAttacking(true);
        source.setAttackTarget(player2.getId());

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 21);
        assertThat(findPermanents(player1, "Ninja")).hasSize(1);
        assertThat(findPermanents(player2, "Ninja")).isEmpty();
    }

    @Test
    @DisplayName("The Ninja count and life total are checked when the trigger resolves")
    void checksCurrentNinjaCountAndLifeAtResolution() {
        harness.setLife(player2, 22);
        Permanent source = addCreatureReady(player1, new DarkLeoShredder());
        source.setAttacking(true);
        source.setAttackTarget(player2.getId());

        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, this::resolveCombat);
        for (int i = 0; i < 3; i++) {
            addCreatureReady(player1, new FootNinjas());
        }
        harness.setLife(player2, 17);
        resolveAllTriggers();

        harness.assertLife(player2, 8);
        assertThat(findPermanents(player1, "Ninja")).hasSize(1);
    }

    @Test
    @DisplayName("The trigger creates its token after the source leaves but recounts remaining Ninjas")
    void sourceLeavingDoesNotStopTokenCreation() {
        harness.setLife(player2, 22);
        for (int i = 0; i < 3; i++) {
            addCreatureReady(player1, new FootNinjas());
        }
        Permanent source = addCreatureReady(player1, new DarkLeoShredder());
        source.setAttacking(true);
        source.setAttackTarget(player2.getId());

        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, this::resolveCombat);
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        resolveAllTriggers();

        harness.assertLife(player2, 21);
        assertThat(findPermanents(player1, "Ninja")).hasSize(1);
    }

    @Test
    @DisplayName("Sneak returns an unblocked attacker and puts Dark Leo & Shredder tapped and attacking")
    void sneakSwapsTheUnblockedAttacker() {
        Permanent attacker = addCreatureReady(player1, new FugitiveDroid());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.setHand(player1, List.of(new DarkLeoShredder()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.castWithAlternateCost(player1, 0, List.of(attacker.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Fugitive Droid");
        Permanent darkLeoShredder = findPermanent(player1, "Dark Leo & Shredder");
        assertThat(darkLeoShredder.isTapped()).isTrue();
        assertThat(darkLeoShredder.isAttacking()).isTrue();
        assertThat(darkLeoShredder.getAttackTarget()).isEqualTo(player2.getId());
    }
}
