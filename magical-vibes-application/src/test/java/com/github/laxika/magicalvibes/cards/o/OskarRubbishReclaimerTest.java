package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TormentingVoice;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OskarRubbishReclaimer.class, Forest.class, GrizzlyBears.class, Shock.class, TormentingVoice.class})
class OskarRubbishReclaimerTest extends BaseCardTest {

    @Test
    void costsLessForEachDistinctManaValueInControllerGraveyard() {
        harness.setHand(player1, List.of(new OskarRubbishReclaimer()));
        harness.setGraveyard(player1, List.of(new Forest(), new Shock(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Oskar, Rubbish Reclaimer");
    }

    @Test
    void mayCastTheExactDiscardedNonlandCardForItsNormalCost() {
        harness.addToBattlefield(player1, new OskarRubbishReclaimer());
        harness.setHand(player1, List.of(new TormentingVoice(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorceryWithDiscard(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Tormenting Voice");
    }

    @Test
    void doesNotOfferADiscardedLand() {
        harness.addToBattlefield(player1, new OskarRubbishReclaimer());
        harness.setHand(player1, List.of(new TormentingVoice(), new Forest()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorceryWithDiscard(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Forest");
    }
}
