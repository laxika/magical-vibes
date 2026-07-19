package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GluttonousSlimeTest extends BaseCardTest {

    private void castSlime() {
        harness.setHand(player1, new ArrayList<>(List.of(new GluttonousSlime())));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
    }

    private Permanent slime() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Gluttonous Slime"))
                .findFirst().orElseThrow();
    }

    @Test
    @DisplayName("Devouring a creature enters with a +1/+1 counter")
    void devourAddsCounter() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castSlime();
        harness.passBothPriorities(); // resolve creature spell -> devour choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(fodder.getId()));

        assertThat(slime().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        // Fodder was sacrificed to devour.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Devouring nothing enters with no counters")
    void devourNoneNoCounters() {
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castSlime();
        harness.passBothPriorities(); // resolve creature spell -> devour choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(slime().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        // Choosing to devour nothing keeps the other creature.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("With no other creatures, the Slime enters with no counters and no prompt")
    void noOtherCreaturesNoPrompt() {
        castSlime();
        harness.passBothPriorities(); // resolve creature spell (no devour prompt: no other creatures)

        assertThat(slime().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
