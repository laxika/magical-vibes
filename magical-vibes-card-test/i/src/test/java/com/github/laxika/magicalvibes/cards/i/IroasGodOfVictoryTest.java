package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BorosReckoner;
import com.github.laxika.magicalvibes.cards.e.EagleOfTheWatch;
import com.github.laxika.magicalvibes.cards.m.MagmaSpray;
import com.github.laxika.magicalvibes.cards.r.RottedHulk;
import com.github.laxika.magicalvibes.cards.s.SupplyLineCranes;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IroasGodOfVictory.class, BorosReckoner.class, EagleOfTheWatch.class,
        MagmaSpray.class, RottedHulk.class, SupplyLineCranes.class, TurnToFrog.class})
class IroasGodOfVictoryTest extends BaseCardTest {

    @Test
    @DisplayName("Iroas is not a creature below seven combined red and white devotion")
    void isNotCreatureBelowDevotionThreshold() {
        Permanent iroas = addIroas();
        addDevotionPermanents(1);

        assertThat(gqs.isCreature(gd, iroas)).isFalse();
        assertThat(gqs.isEnchantment(gd, iroas)).isTrue();
    }

    @Test
    @DisplayName("Iroas becomes a creature at seven combined red and white devotion")
    void becomesCreatureAtDevotionThreshold() {
        Permanent iroas = addIroas();
        addDevotionAtThreshold();

        assertThat(gqs.isCreature(gd, iroas)).isTrue();
    }

    @Test
    @DisplayName("Creatures you control have menace")
    void grantsMenaceToYourCreatures() {
        addIroas();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new RottedHulk());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Combat damage to attacking creatures you control is prevented")
    void preventsCombatDamageToYourAttacker() {
        addIroas();
        Permanent attacker = addAttacker(player1);
        Permanent firstBlocker = addBlocker(player2, attacker);
        addBlocker(player2, attacker);

        resolveCombat();
        harness.handleCombatDamageAssigned(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker),
                Map.of(firstBlocker.getId(), 2));

        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(firstBlocker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Noncombat damage to an attacking creature you control is prevented")
    void preventsNoncombatDamage() {
        addIroas();
        Permanent attacker = addAttacker(player1);

        castMagmaSpray(attacker);

        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
    }

    @Test
    void grantsMenaceToItselfAtSevenDevotion() {
        Permanent iroas = addIroas();
        addDevotionAtThreshold();

        assertThat(gqs.isCreature(gd, iroas)).isTrue();
        assertThat(gqs.hasKeyword(gd, iroas, Keyword.MENACE)).isTrue();
    }

    @Test
    void doesNotGrantMenaceToOpponentsCreatures() {
        addIroas();
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new RottedHulk());

        assertThat(gqs.hasKeyword(gd, opponent, Keyword.MENACE)).isFalse();
    }

    @Test
    void hybridSymbolsCountOnceAndDevotionUpdatesWhenPermanentsLeave() {
        Permanent iroas = addIroas();
        addDevotionPermanents(1);
        harness.addToBattlefield(player1, new EagleOfTheWatch());
        assertThat(gqs.isCreature(gd, iroas)).isFalse();

        Permanent seventhSymbol = harness.addToBattlefieldAndReturn(player1, new EagleOfTheWatch());
        assertThat(gqs.isCreature(gd, iroas)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(seventhSymbol);
        assertThat(gqs.isCreature(gd, iroas)).isFalse();
        assertThat(gqs.isEnchantment(gd, iroas)).isTrue();
    }

    @Test
    void doesNotPreventDamageToNonattackingCreatures() {
        addIroas();
        Permanent creature = addCreatureReady(player1, new RottedHulk());

        castMagmaSpray(creature);

        assertThat(creature.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void doesNotPreventDamageToOpponentsAttacker() {
        addIroas();
        Permanent attacker = addAttacker(player2);
        addBlocker(player1, attacker);

        resolveCombat(player2);

        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void doesNotPreventDamageToYourBlocker() {
        addIroas();
        Permanent attacker = addAttacker(player2);
        Permanent blocker = addBlocker(player1, attacker);

        resolveCombat(player2);

        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void losingAllAbilitiesEndsDamagePrevention() {
        Permanent iroas = addIroas();
        addDevotionAtThreshold();
        Permanent attacker = addAttacker(player1);
        Permanent firstBlocker = addBlocker(player2, attacker);
        addBlocker(player2, attacker);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.setHand(player2, List.of(new TurnToFrog()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, iroas.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, iroas, Keyword.INDESTRUCTIBLE)).isFalse();
        resolveCombat();
        harness.handleCombatDamageAssigned(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker),
                Map.of(firstBlocker.getId(), 2));

        assertThat(attacker.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    void indestructibleProtectsNonattackingIroasFromLethalDamage() {
        Permanent iroas = addIroas();
        addDevotionAtThreshold();

        castMagmaSpray(iroas);
        castMagmaSpray(iroas);

        assertThat(iroas.getMarkedDamage()).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(iroas);
    }

    private void castMagmaSpray(Permanent target) {
        harness.setHand(player2, List.of(new MagmaSpray()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addIroas() {
        return harness.addToBattlefieldAndReturn(player1, new IroasGodOfVictory());
    }

    private void addDevotionPermanents(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new BorosReckoner());
        }
    }

    private void addDevotionAtThreshold() {
        harness.addToBattlefield(player1, new SupplyLineCranes());
        harness.addToBattlefield(player1, new SupplyLineCranes());
        harness.addToBattlefield(player1, new EagleOfTheWatch());
    }

    private Permanent addAttacker(Player controller) {
        Permanent attacker = addCreatureReady(controller, new RottedHulk());
        attacker.setAttacking(true);
        return attacker;
    }

    private Permanent addBlocker(Player controller, Permanent attacker) {
        Permanent blocker = addCreatureReady(controller, new RottedHulk());
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(attacker.getId());
        return blocker;
    }
}
