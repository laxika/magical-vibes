package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.i.InnocentBystander;
import com.github.laxika.magicalvibes.cards.s.SanitationAutomaton;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GleamingGeardrake.class, SanitationAutomaton.class, InnocentBystander.class})
class GleamingGeardrakeTest extends BaseCardTest {

    @Test
    void entersAndInvestigates() {
        harness.enterBattlefieldAndReturn(player1, new GleamingGeardrake());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void growsWhenYouSacrificeAnArtifact() {
        Permanent drake = harness.addToBattlefieldAndReturn(player1, new GleamingGeardrake());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new SanitationAutomaton());

        sacrifice(player1, artifact);
        resolveAllTriggers();

        assertThat(drake.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotGrowWhenYouSacrificeANonartifact() {
        Permanent drake = harness.addToBattlefieldAndReturn(player1, new GleamingGeardrake());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new InnocentBystander());

        sacrifice(player1, creature);
        resolveAllTriggers();

        assertThat(drake.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotGrowWhenAnOpponentSacrificesAnArtifact() {
        Permanent drake = harness.addToBattlefieldAndReturn(player1, new GleamingGeardrake());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new SanitationAutomaton());

        sacrifice(player2, artifact);
        resolveAllTriggers();

        assertThat(drake.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void sacrificingItsCluePutsCounterOnStackBeforeDrawing() {
        Permanent drake = harness.enterBattlefieldAndReturn(player1, new GleamingGeardrake());
        resolveAllTriggers();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GleamingGeardrake()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Permanent clue = findPermanents(player1, "Clue").getFirst();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(clue), null, null);

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(drake.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        assertThat(drake.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void growsForEveryArtifactSacrificedInTheSameTurn() {
        Permanent drake = harness.addToBattlefieldAndReturn(player1, new GleamingGeardrake());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SanitationAutomaton());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SanitationAutomaton());

        sacrifice(player1, first);
        resolveAllTriggers();
        sacrifice(player1, second);
        resolveAllTriggers();

        assertThat(drake.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private void sacrifice(Player player, Permanent permanent) {
        Card card = permanent.getCard();
        gd.playerBattlefields.get(player.getId()).remove(permanent);
        gd.playerGraveyards.get(player.getId()).add(card);
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .checkAllyPermanentSacrificedTriggers(gd, player.getId(), card));
    }
}
