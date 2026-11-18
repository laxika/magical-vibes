package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UmaraMystic.class, Shock.class, Divination.class, FugitiveWizard.class, GrizzlyBears.class})
class UmaraMysticTest extends BaseCardTest {

    @Test
    void instantAndSorcerySpellsBoostSelf() {
        Permanent mystic = addMystic(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addMana(player1, ManaColor.RED, 2);
        harness.setHand(player1, List.of(new Shock(), new Divination()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(PermanentGrants.powerBoostOf(gd, mystic)).isEqualTo(2);

        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castAndResolveSorcery(player1, 0, 0);
        assertThat(PermanentGrants.powerBoostOf(gd, mystic)).isEqualTo(4);
    }

    @Test
    void wizardSpellBoostsSelf() {
        Permanent mystic = addMystic(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player1, List.of(new FugitiveWizard()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, mystic)).isEqualTo(2);
    }

    @Test
    void nonMatchingCreatureSpellDoesNotTrigger() {
        Permanent mystic = addMystic(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, mystic)).isEqualTo(0);
    }

    @Test
    void boostWearsOffAtEndOfTurn() {
        Permanent mystic = addMystic(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(PermanentGrants.powerBoostOf(gd, mystic)).isEqualTo(2);

        resolveAllTriggers();
        harness.forceStep(TurnStep.END_STEP);
        assertThat(PermanentGrants.powerBoostOf(gd, mystic)).isEqualTo(2);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(PermanentGrants.powerBoostOf(gd, mystic)).isEqualTo(0);
    }

    @Test
    void opponentsMatchingSpellDoesNotTrigger() {
        Permanent mystic = addMystic(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.RED, 2);
        harness.setHand(player2, List.of(new UmaraMystic()));

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, mystic)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, mystic)).isZero();
    }

    @Test
    void controllerInstantTriggersDuringOpponentsTurn() {
        Permanent mystic = addMystic(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(PermanentGrants.powerBoostOf(gd, mystic)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, mystic)).isZero();
    }

    @Test
    void castingMysticBoostsExistingMysticsButNotTheNewOne() {
        Permanent first = addMystic(player1);
        Permanent second = addMystic(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setHand(player1, List.of(new UmaraMystic()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, first)).isEqualTo(2);
        assertThat(PermanentGrants.powerBoostOf(gd, second)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, first)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, second)).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent != first && permanent != second)
                .singleElement()
                .satisfies(permanent -> {
                    assertThat(PermanentGrants.powerBoostOf(gd, permanent)).isZero();
                    assertThat(PermanentGrants.toughnessBoostOf(gd, permanent)).isZero();
                });
    }

    @Test
    void mysticDoesNotTriggerFromItsOwnCast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setHand(player1, List.of(new UmaraMystic()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement()
                .satisfies(permanent -> {
                    assertThat(PermanentGrants.powerBoostOf(gd, permanent)).isZero();
                    assertThat(PermanentGrants.toughnessBoostOf(gd, permanent)).isZero();
                });
    }

    private Permanent addMystic(Player player) {
        return addCreatureReady(player, new UmaraMystic());
    }
}
