package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.DrossCrocodile;
import com.github.laxika.magicalvibes.cards.n.NightsWhisper;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VedalkenOrrery.class, DrossCrocodile.class, NightsWhisper.class})
class VedalkenOrreryTest extends BaseCardTest {

    @Test
    @DisplayName("Can cast a creature at instant speed with Vedalken Orrery")
    void canCastCreatureAtInstantSpeed() {
        harness.addToBattlefield(player1, new VedalkenOrrery());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new DrossCrocodile()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Can cast a sorcery at instant speed with Vedalken Orrery")
    void canCastSorceryAtInstantSpeed() {
        harness.addToBattlefield(player1, new VedalkenOrrery());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new NightsWhisper()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Vedalken Orrery only grants flash to its controller")
    void onlyAffectsController() {
        harness.addToBattlefield(player2, new VedalkenOrrery());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new DrossCrocodile()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Spells lose flash timing when Vedalken Orrery leaves the battlefield")
    void losesFlashWhenOrreryLeaves() {
        harness.addToBattlefield(player1, new VedalkenOrrery());
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new DrossCrocodile()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Can cast a creature during an opponent's upkeep")
    void canCastDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new VedalkenOrrery());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.setHand(player1, List.of(new DrossCrocodile()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dross Crocodile");
    }

    @Test
    @DisplayName("Can cast an artifact in response to a sorcery")
    void canCastArtifactWithNonemptyStack() {
        harness.addToBattlefield(player1, new VedalkenOrrery());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new NightsWhisper(), new VedalkenOrrery()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorcery(player1, 0);
        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(2);
        harness.assertNotInHand(player1, "Vedalken Orrery");
    }

    @Test
    @DisplayName("A tapped Vedalken Orrery still permits instant timing")
    void tappedOrreryStillAllowsCasting() {
        harness.addToBattlefieldAndReturn(player1, new VedalkenOrrery()).tap();
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(new DrossCrocodile()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dross Crocodile");
    }

    @Test
    @DisplayName("An Orrery in hand does not give itself instant timing")
    void orreryInHandDoesNotGrantFlash() {
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(new VedalkenOrrery()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Vedalken Orrery");
    }
}
