package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DemonOfDeathsGate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PsychogenicProbe;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AspiringChampion.class, DemonOfDeathsGate.class, GrizzlyBears.class, Shock.class,
        PsychogenicProbe.class, SolRing.class})
class AspiringChampionTest extends BaseCardTest {

    @Test
    void sacrificesItselfAndRevealsUntilCreature() {
        Permanent champion = addReadyCreature(new AspiringChampion());
        Card nonCreature = new Shock();
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(nonCreature, creature));

        dealUnblockedCombat(champion);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(champion);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonCreature);
    }

    @Test
    void revealedDemonDealsItsPowerToEachOpponent() {
        Permanent champion = addReadyCreature(new AspiringChampion());
        Card demon = new DemonOfDeathsGate();
        harness.setLibrary(player1, List.of(demon));

        dealUnblockedCombat(champion);

        Permanent enteredDemon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == demon)
                .findFirst()
                .orElseThrow();
        assertThat(gd.getLife(player2.getId())).isEqualTo(17 - enteredDemon.getEffectivePower());
    }

    @Test
    void revealedNonDemonDoesNotDealAdditionalDamage() {
        Permanent champion = addReadyCreature(new AspiringChampion());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        dealUnblockedCombat(champion);

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @CardUsed({AspiringChampion.class, PsychogenicProbe.class, SolRing.class})
    void firstCardCreatureStillCausesLibraryShuffle() {
        Permanent champion = addReadyCreature(new AspiringChampion());
        Card creature = new AspiringChampion();
        Card remaining = new SolRing();
        harness.setLibrary(player1, List.of(creature, remaining));
        harness.addToBattlefield(player2, new PsychogenicProbe());

        dealUnblockedCombat(champion);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 17);
    }

    @Test
    @CardUsed({AspiringChampion.class, PsychogenicProbe.class})
    void lastLibraryCardCreatureStillCausesShuffle() {
        Permanent champion = addReadyCreature(new AspiringChampion());
        harness.setLibrary(player1, List.of(new AspiringChampion()));
        harness.addToBattlefield(player2, new PsychogenicProbe());

        dealUnblockedCombat(champion);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 18);
    }

    @Test
    @CardUsed({AspiringChampion.class, SolRing.class})
    void libraryWithoutCreaturesIsReturnedAfterSacrifice() {
        Permanent champion = addReadyCreature(new AspiringChampion());
        Card first = new SolRing();
        Card second = new SolRing();
        harness.setLibrary(player1, List.of(first, second));

        dealUnblockedCombat(champion);

        harness.assertInGraveyard(player1, "Aspiring Champion");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        harness.assertLife(player2, 17);
    }

    @Test
    @CardUsed({AspiringChampion.class})
    void emptyLibraryStillSacrificesChampion() {
        Permanent champion = addReadyCreature(new AspiringChampion());
        harness.setLibrary(player1, List.of());

        dealUnblockedCombat(champion);

        harness.assertInGraveyard(player1, "Aspiring Champion");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertLife(player2, 17);
    }

    @Test
    @CardUsed({AspiringChampion.class, PsychogenicProbe.class})
    void emptyLibraryStillCausesShuffleAfterSacrifice() {
        Permanent champion = addReadyCreature(new AspiringChampion());
        harness.setLibrary(player1, List.of());
        harness.addToBattlefield(player2, new PsychogenicProbe());

        dealUnblockedCombat(champion);

        harness.assertInGraveyard(player1, "Aspiring Champion");
        harness.assertLife(player1, 18);
    }

    @Test
    @CardUsed({AspiringChampion.class})
    void sourceLeavingBeforeResolutionDoesNotRevealCreature() {
        Permanent champion = addReadyCreature(new AspiringChampion());
        Card creature = new AspiringChampion();
        harness.setLibrary(player1, List.of(creature));
        champion.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        assertThat(gd.stack).hasSize(1);

        champion.setMarkedDamage(3);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Aspiring Champion");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        harness.assertLife(player2, 17);
    }

    private Permanent addReadyCreature(Card card) {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, card);
        creature.setSummoningSick(false);
        return creature;
    }

    private void dealUnblockedCombat(Permanent attacker) {
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.passUntil(TurnStep.END_OF_COMBAT);
    }
}
