package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.CyberConversion;
import com.github.laxika.magicalvibes.cards.c.CybermanPatrol;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VrestinMenoptraLeader.class, CybermanPatrol.class, CyberConversion.class})
class VrestinMenoptraLeaderTest extends BaseCardTest {

    @Test
    void entersWithXCountersAndCreatesXFlyingAlienInsects() {
        castVrestin(2);

        Permanent vrestin = findPermanent(player1, "Vrestin, Menoptra Leader");
        assertThat(vrestin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        List<Permanent> tokens = findPermanents(player1, "Alien Insect");
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.GREEN, CardColor.WHITE);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.ALIEN, CardSubtype.INSECT);
            assertThat(token.hasKeyword(Keyword.FLYING)).isTrue();
            assertThat(token.getEffectivePower()).isEqualTo(1);
            assertThat(token.getEffectiveToughness()).isEqualTo(1);
        });
    }

    @Test
    void putsCountersOnEachAttackingInsectOnce() {
        castVrestin(3);
        List<Permanent> insects = findPermanents(player1, "Alien Insect");
        insects.forEach(insect -> insect.setSummoningSick(false));
        Permanent soldier = addCreatureReady(player1, new CybermanPatrol());

        declareAttackers(List.of(1, 2, 4));
        resolveAllTriggers();

        assertThat(insects.get(0).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(insects.get(1).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(insects.get(2).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(soldier.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotTriggerWhenOnlyNonInsectsAttack() {
        castVrestin(1);
        Permanent soldier = addCreatureReady(player1, new CybermanPatrol());

        declareAttackers(List.of(2));

        assertThat(soldier.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void xZeroCreatesNoTokensAndVrestinDies() {
        castVrestin(0);

        harness.assertNotOnBattlefield(player1, "Vrestin, Menoptra Leader");
        harness.assertInGraveyard(player1, "Vrestin, Menoptra Leader");
        assertThat(findPermanents(player1, "Alien Insect")).isEmpty();
    }

    @Test
    void vrestinGetsACounterWhenItAttacks() {
        castVrestin(1);
        Permanent vrestin = findPermanent(player1, "Vrestin, Menoptra Leader");
        vrestin.setSummoningSick(false);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(vrestin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(findPermanent(player1, "Alien Insect").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void opponentsAttackDoesNotTriggerVrestin() {
        castVrestin(1);
        Permanent vrestin = addCreatureReady(player2, new VrestinMenoptraLeader());
        vrestin.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(vrestin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(findPermanent(player1, "Vrestin, Menoptra Leader")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanent(player1, "Alien Insect").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void originallyAttackingInsectStillGetsCounterAfterLeavingCombat() {
        castVrestin(1);
        Permanent insect = findPermanent(player1, "Alien Insect");
        insect.setSummoningSick(false);
        declareAttackers(List.of(1));
        assertThat(gd.stack).hasSize(1);

        insect.setAttacking(false);
        resolveAllTriggers();

        assertThat(insect.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void insectEnteringAttackingAfterDeclarationDoesNotGetCounter() {
        castVrestin(2);
        List<Permanent> insects = findPermanents(player1, "Alien Insect");
        insects.get(0).setSummoningSick(false);
        Permanent lateAttacker = insects.get(1);
        gd.playerBattlefields.get(player1.getId()).remove(lateAttacker);
        declareAttackers(List.of(1));
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).add(lateAttacker);
        lateAttacker.tap();
        lateAttacker.setAttacking(true);
        lateAttacker.setAttackTarget(player2.getId());
        resolveAllTriggers();

        assertThat(insects.get(0).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(lateAttacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void originallyAttackingInsectStillGetsCounterAfterCyberConversion() {
        castVrestin(1);
        Permanent vrestin = findPermanent(player1, "Vrestin, Menoptra Leader");
        vrestin.setSummoningSick(false);
        harness.setHand(player2, List.of(new CyberConversion()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        declareAttackers(List.of(0));
        assertThat(gd.stack).hasSize(1);

        harness.castAndResolveInstant(player2, 0, vrestin.getId());
        assertThat(vrestin.isFaceDown()).isTrue();
        resolveAllTriggers();

        assertThat(vrestin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private void castVrestin(int x) {
        harness.setHand(player1, List.of(new VrestinMenoptraLeader()));
        harness.addMana(player1, ManaColor.GREEN, x + 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0, x);
        harness.passBothPriorities();
        resolveAllTriggers();
    }
}
