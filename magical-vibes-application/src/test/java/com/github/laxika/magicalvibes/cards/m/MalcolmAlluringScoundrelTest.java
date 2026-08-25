package com.github.laxika.magicalvibes.cards.m;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

@CardUsed({MalcolmAlluringScoundrel.class, Forest.class, GrizzlyBears.class})
class MalcolmAlluringScoundrelTest extends BaseCardTest {

    @Test
    void fourthChorusCounterOffersTheDiscardedCardAndCastsItForFree() {
        Permanent malcolm = addAttacker();
        malcolm.setCounterCount(CounterType.CHORUS, 3);
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest())));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(malcolm.getCounterCount(CounterType.CHORUS)).isEqualTo(4);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Malcolm, Alluring Scoundrel");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void aDiscardedLandIsNotOfferedForCasting() {
        Permanent malcolm = addAttacker();
        malcolm.setCounterCount(CounterType.CHORUS, 4);
        harness.setHand(player1, new ArrayList<>(List.of(new Forest())));
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest())));

        resolveCombat();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    void fourCountersAreRememberedIfMalcolmLeavesBeforeTheDiscard() {
        Permanent malcolm = addAttacker();
        malcolm.setCounterCount(CounterType.CHORUS, 3);
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest())));

        resolveCombat();
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(malcolm);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    private Permanent addAttacker() {
        Permanent attacker = new Permanent(new MalcolmAlluringScoundrel());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);
        return attacker;
    }
}
