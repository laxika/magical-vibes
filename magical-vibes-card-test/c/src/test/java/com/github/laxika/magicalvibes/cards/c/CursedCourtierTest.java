package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BreakTheSpell;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CursedCourtier.class, BreakTheSpell.class})
class CursedCourtierTest extends BaseCardTest {

    @Test
    void entersWithCursedRoleAttachedAndSetsItsBasePowerAndToughness() {
        harness.castFromHand(player1, new CursedCourtier(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent courtier = findPermanent(player1, "Cursed Courtier");
        Permanent role = findPermanent(player1, "Cursed");

        assertThat(role.getCard().isToken()).isTrue();
        assertThat(role.getCard().isAura()).isTrue();
        assertThat(role.getCard().getSubtypes()).contains(CardSubtype.ROLE);
        assertThat(role.getAttachedTo()).isEqualTo(courtier.getId());
        assertThat(gqs.getEffectivePower(gd, courtier)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, courtier)).isEqualTo(1);
    }

    @Test
    void roleIsCreatedByTheTriggerControllerAfterCourtierChangesControl() {
        harness.castFromHand(player1, new CursedCourtier(), "{2}{W}");
        harness.passBothPriorities();
        Permanent courtier = findPermanent(player1, "Cursed Courtier");

        gd.playerBattlefields.get(player1.getId()).remove(courtier);
        gd.playerBattlefields.get(player2.getId()).add(courtier);
        harness.passBothPriorities();

        Permanent role = findPermanent(player1, "Cursed");
        assertThat(role.getAttachedTo()).isEqualTo(courtier.getId());
        assertThat(countPermanents(player2, "Cursed")).isZero();
        assertThat(gqs.getEffectivePower(gd, courtier)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, courtier)).isEqualTo(1);
    }

    @Test
    void doesNotCreateRoleWhenCourtierLeavesBeforeItsTriggerResolves() {
        harness.castFromHand(player1, new CursedCourtier(), "{2}{W}");
        harness.passBothPriorities();
        Permanent courtier = findPermanent(player1, "Cursed Courtier");

        gd.playerBattlefields.get(player1.getId()).remove(courtier);
        gd.playerGraveyards.get(player1.getId()).add(courtier.getCard());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Cursed")).isZero();
        assertThat(countPermanents(player2, "Cursed")).isZero();
    }

    @Test
    void cursedRoleDoesNotRemoveLifelink() {
        harness.castFromHand(player1, new CursedCourtier(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent courtier = findPermanent(player1, "Cursed Courtier");
        courtier.setSummoningSick(false);
        courtier.setAttacking(true);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.resolveCombatDamage();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    void cursedRoleSetsBaseStatsButDoesNotOverrideCounters() {
        harness.castFromHand(player1, new CursedCourtier(), "{2}{W}");
        harness.passBothPriorities();
        Permanent courtier = findPermanent(player1, "Cursed Courtier");
        courtier.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, courtier)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, courtier)).isEqualTo(3);
    }

    @Test
    void destroyingCursedRoleRestoresCourtiersStats() {
        harness.castFromHand(player1, new CursedCourtier(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent courtier = findPermanent(player1, "Cursed Courtier");
        Permanent role = findPermanent(player1, "Cursed");
        harness.setLibrary(player1, List.of(new CursedCourtier()));
        harness.setHand(player1, List.of(new BreakTheSpell()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, role.getId());

        assertThat(countPermanents(player1, "Cursed")).isZero();
        assertThat(gqs.getEffectivePower(gd, courtier)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, courtier)).isEqualTo(3);
    }
}
