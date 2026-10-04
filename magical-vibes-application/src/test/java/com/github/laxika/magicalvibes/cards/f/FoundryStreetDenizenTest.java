package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.e.EmberBeast;
import com.github.laxika.magicalvibes.cards.g.GreensideWatcher;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FoundryStreetDenizen.class, EmberBeast.class, GreensideWatcher.class})
class FoundryStreetDenizenTest extends BaseCardTest {

    @Test
    @DisplayName("Does not trigger for its own entry")
    void noBoostForOwnEntry() {
        harness.setHand(player1, List.of(new FoundryStreetDenizen()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent denizen = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, denizen)).isEqualTo(1);
    }

    @Test
    @DisplayName("Another Denizen boosts the existing Denizen but not itself")
    void anotherDenizenOnlyBoostsExistingDenizen() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new FoundryStreetDenizen());
        harness.setHand(player1, List.of(new FoundryStreetDenizen()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent second = gd.playerBattlefields.get(player1.getId()).get(1);
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Gets +1/+0 when another red creature you control enters")
    void boostsWhenRedCreatureEnters() {
        Permanent denizen = harness.addToBattlefieldAndReturn(player1, new FoundryStreetDenizen());

        harness.setHand(player1, List.of(new EmberBeast()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve the creature spell
        harness.passBothPriorities(); // resolve the triggered ability

        assertThat(gqs.getEffectivePower(gd, denizen)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, denizen)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger for a nonred creature")
    void noBoostForNonredCreature() {
        Permanent denizen = harness.addToBattlefieldAndReturn(player1, new FoundryStreetDenizen());

        harness.setHand(player1, List.of(new GreensideWatcher()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, denizen)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger for an opponent's red creature")
    void noBoostForOpponentRedCreature() {
        Permanent denizen = harness.addToBattlefieldAndReturn(player1, new FoundryStreetDenizen());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new EmberBeast()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, denizen)).isEqualTo(1);
    }

    @Test
    @DisplayName("The boost is cumulative and wears off at end of turn")
    void boostStacksAndWearsOff() {
        Permanent denizen = harness.addToBattlefieldAndReturn(player1, new FoundryStreetDenizen());

        harness.setHand(player1, List.of(new EmberBeast(), new EmberBeast()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, denizen)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(denizen.getPowerModifier()).isEqualTo(0);
        assertThat(gqs.getEffectivePower(gd, denizen)).isEqualTo(1);
    }
}
