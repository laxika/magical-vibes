package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FeralShadow;
import com.github.laxika.magicalvibes.cards.i.IgneousGolem;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RavenousVampire.class, FeralShadow.class, IgneousGolem.class})
class RavenousVampireTest extends BaseCardTest {

    @Test
    @DisplayName("Declining the sacrifice taps the Vampire and puts no counter on it")
    void declineTapsVampire() {
        harness.addToBattlefield(player1, new RavenousVampire());
        harness.addToBattlefield(player1, new FeralShadow());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger → may prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanent(player1, "Ravenous Vampire").isTapped()).isTrue();
        assertThat(findPermanent(player1, "Ravenous Vampire")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertOnBattlefield(player1, "Feral Shadow");
    }

    @Test
    @DisplayName("Sacrificing another nonartifact creature puts a +1/+1 counter on the Vampire and leaves it untapped")
    void acceptSacrificesChosenCreatureForCounter() {
        harness.addToBattlefield(player1, new RavenousVampire());
        harness.addToBattlefield(player1, new FeralShadow());
        UUID shadow = harness.getPermanentId(player1, "Feral Shadow");

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        // The Vampire is itself a nonartifact creature, so both it and the Feral Shadow are legal.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, shadow);

        harness.assertNotOnBattlefield(player1, "Feral Shadow");
        assertThat(findPermanent(player1, "Ravenous Vampire")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanent(player1, "Ravenous Vampire").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Artifact creatures can't be sacrificed, so the Vampire itself is the only legal choice")
    void artifactCreatureIsNotALegalSacrifice() {
        harness.addToBattlefield(player1, new RavenousVampire());
        harness.addToBattlefield(player1, new IgneousGolem());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Ravenous Vampire");
        harness.assertOnBattlefield(player1, "Igneous Golem");
    }
}
