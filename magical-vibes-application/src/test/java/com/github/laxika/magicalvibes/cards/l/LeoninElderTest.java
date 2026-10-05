package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.m.MycosynthLattice;
import com.github.laxika.magicalvibes.cards.n.NecrogenSpellbomb;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LeoninElder.class, Ornithopter.class, NecrogenSpellbomb.class, MycosynthLattice.class})
class LeoninElderTest extends BaseCardTest {

    @Test
    @DisplayName("May gain 1 life when an artifact enters under its controller's control")
    void gainsLifeWhenOwnArtifactEnters() {
        harness.addToBattlefield(player1, new LeoninElder());
        harness.castFromHand(player1, new Ornithopter(), "{0}");
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("May decline life gain when an artifact enters")
    void mayDeclineLifeGain() {
        harness.addToBattlefield(player1, new LeoninElder());
        harness.castFromHand(player1, new Ornithopter(), "{0}");
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Triggers for an artifact entering under an opponent's control")
    void gainsLifeWhenOpponentArtifactEnters() {
        harness.addToBattlefield(player1, new LeoninElder());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new Ornithopter(), "{0}");
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Triggers separately for each artifact that enters")
    void triggersForEachArtifactEntry() {
        harness.addToBattlefield(player1, new LeoninElder());
        harness.setHand(player1, List.of(new Ornithopter(), new Ornithopter()));

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Does not trigger for a non-artifact permanent entering")
    void doesNotTriggerForNonArtifact() {
        harness.addToBattlefield(player1, new LeoninElder());
        harness.castFromHand(player1, new LeoninElder(), "{W}");
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Triggers for a noncreature artifact only after it enters")
    void gainsLifeWhenNoncreatureArtifactEnters() {
        harness.addToBattlefield(player1, new LeoninElder());
        harness.castFromHand(player1, new NecrogenSpellbomb(), "{1}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 20);

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each Elder offers a separate optional life gain for one artifact")
    void multipleEldersMakeIndependentChoices() {
        harness.addToBattlefield(player1, new LeoninElder());
        harness.addToBattlefield(player1, new LeoninElder());
        harness.castFromHand(player1, new Ornithopter(), "{0}");

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        harness.assertLife(player1, 20);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 21);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Under Mycosynth Lattice an entering Elder triggers itself and the existing Elder")
    void artifactTypeGrantedOnBattlefieldCountsForEntryTriggers() {
        harness.addToBattlefield(player1, new MycosynthLattice());
        harness.addToBattlefield(player1, new LeoninElder());
        harness.castFromHand(player1, new LeoninElder(), "{W}");

        resolveAllTriggers();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 22);
        assertThat(gd.stack).isEmpty();
    }
}
