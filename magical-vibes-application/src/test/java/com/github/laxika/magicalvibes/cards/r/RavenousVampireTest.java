package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RavenousVampireTest extends BaseCardTest {

    private Permanent vampire(Player owner) {
        UUID id = harness.getPermanentId(owner, "Ravenous Vampire");
        return gd.playerBattlefields.get(owner.getId()).stream()
                .filter(p -> p.getId().equals(id))
                .findFirst().orElseThrow();
    }

    @Test
    @DisplayName("Declining the sacrifice taps the Vampire and puts no counter on it")
    void declineTapsVampire() {
        harness.addToBattlefield(player1, new RavenousVampire());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger → may prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(vampire(player1).isTapped()).isTrue();
        assertThat(vampire(player1).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Sacrificing another nonartifact creature puts a +1/+1 counter on the Vampire and leaves it untapped")
    void acceptSacrificesChosenCreatureForCounter() {
        harness.addToBattlefield(player1, new RavenousVampire());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bears = harness.getPermanentId(player1, "Grizzly Bears");

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        // The Vampire is itself a nonartifact creature, so both it and the Bears are legal.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(vampire(player1).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(vampire(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Artifact creatures can't be sacrificed, so the Vampire itself is the only legal choice")
    void artifactCreatureIsNotALegalSacrifice() {
        harness.addToBattlefield(player1, new RavenousVampire());
        harness.addToBattlefield(player1, new Ornithopter());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Ravenous Vampire");
        harness.assertOnBattlefield(player1, "Ornithopter");
    }
}
