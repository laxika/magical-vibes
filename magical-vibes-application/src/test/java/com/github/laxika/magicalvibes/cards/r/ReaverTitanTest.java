package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CanoptekTombSentinel;
import com.github.laxika.magicalvibes.cards.c.CanoptekWraith;
import com.github.laxika.magicalvibes.cards.c.ChaosWarp;
import com.github.laxika.magicalvibes.cards.u.UtterEnd;
import com.github.laxika.magicalvibes.cards.s.Starstorm;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReaverTitan.class, CanoptekWraith.class, CanoptekTombSentinel.class,
        ChaosWarp.class, UtterEnd.class, Starstorm.class})
class ReaverTitanTest extends BaseCardTest {

    @Test
    @DisplayName("Protection applies to sources with mana value 3 or less")
    void protectionFromManaValueAtMostThree() {
        Permanent titan = addReaverTitan();
        CanoptekWraith manaValueThree = new CanoptekWraith();
        CanoptekTombSentinel manaValueFour = new CanoptekTombSentinel();

        assertThat(gqs.hasProtectionFromSource(gd, titan, manaValueThree)).isTrue();
        assertThat(gqs.hasProtectionFromSource(gd, titan, manaValueFour)).isFalse();
    }

    @Test
    @DisplayName("Crew 4 animates Reaver Titan and its attack trigger damages each opponent")
    void crewAndAttackTrigger() {
        harness.setLife(player2, 20);
        addReaverTitan();
        addCreatureReady(player1, new CanoptekTombSentinel());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent titan = gd.playerBattlefields.get(player1.getId()).get(0);
        assertThat(gqs.isCreature(gd, titan)).isTrue();
        assertThat(titan.isAnimatedUntilEndOfTurn()).isTrue();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(5);
    }

    @Test
    void protectionPreventsManaValueThreeSpellFromTargetingUncrewedTitan() {
        Permanent titan = addReaverTitan();
        harness.setHand(player2, List.of(new ChaosWarp()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, titan.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
        harness.assertOnBattlefield(player1, "Reaver Titan");
    }

    @Test
    void manaValueFourSpellCanExileUncrewedTitan() {
        Permanent titan = addReaverTitan();
        harness.setHand(player2, List.of(new UtterEnd()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player2, 0, titan.getId());

        harness.assertNotOnBattlefield(player1, "Reaver Titan");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(titan.getCard().getId())
                && entry.ownerId().equals(player1.getId()));
    }

    @Test
    void twoSummoningSickCreaturesCanPayCrewFour() {
        Permanent titan = addReaverTitan();
        harness.addToBattlefield(player1, new CanoptekWraith());
        harness.addToBattlefield(player1, new CanoptekWraith());
        List<Permanent> crew = findPermanents(player1, "Canoptek Wraith");
        crew.forEach(permanent -> permanent.setSummoningSick(true));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(crew).allMatch(Permanent::isTapped);
        assertThat(gqs.isCreature(gd, titan)).isTrue();
        assertThat(titan.isTapped()).isFalse();
    }

    @Test
    void insufficientPowerCannotCrewTitan() {
        Permanent titan = addReaverTitan();
        Permanent crew = addCreatureReady(player1, new CanoptekWraith());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power");

        assertThat(crew.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, titan)).isFalse();
    }

    @Test
    void crewAnimationExpiresAfterTheTurn() {
        Permanent titan = addReaverTitan();
        addCreatureReady(player1, new CanoptekTombSentinel());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, titan)).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.isCreature(gd, titan)).isFalse();
    }

    @Test
    void attackTriggerDealsFiveEvenWhenBlockedAndDoesNotDamageController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addReaverTitan();
        addCreatureReady(player1, new CanoptekTombSentinel());
        addCreatureReady(player2, new CanoptekTombSentinel());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });
        prepareDeclareBlockers();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void manaValueThreeCreatureCannotBlockTitan() {
        addReaverTitan();
        addCreatureReady(player1, new CanoptekTombSentinel());
        addCreatureReady(player2, new CanoptekWraith());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    void starstormWithXTenCanKillCrewedTitan() {
        addReaverTitan();
        addCreatureReady(player1, new CanoptekTombSentinel());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new Starstorm()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 10);

        harness.castInstant(player2, 0, 10, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Reaver Titan");
    }

    @Test
    void starstormWithXOneCannotDamageCrewedTitan() {
        Permanent titan = addReaverTitan();
        addCreatureReady(player1, new CanoptekTombSentinel());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new Starstorm()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.castInstant(player2, 0, 1, null);
            harness.passBothPriorities();
        });

        harness.assertOnBattlefield(player1, "Reaver Titan");
        assertThat(titan.getMarkedDamage()).isZero();
        assertThat(findPermanent(player1, "Canoptek Tomb Sentinel").getMarkedDamage()).isEqualTo(1);
    }

    private Permanent addReaverTitan() {
        return addCreatureReady(player1, new ReaverTitan());
    }
}
