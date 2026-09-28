package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DemonOfDeathsGate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AspiringChampion.class, DemonOfDeathsGate.class, GrizzlyBears.class, Shock.class})
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

    private Permanent addReadyCreature(Card card) {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, card);
        creature.setSummoningSick(false);
        return creature;
    }

    private void dealUnblockedCombat(Permanent attacker) {
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
