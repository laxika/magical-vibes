package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FoundryStreetDenizenTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+0 when another red creature you control enters")
    void boostsWhenRedCreatureEnters() {
        harness.addToBattlefield(player1, new FoundryStreetDenizen());
        Permanent denizen = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve the creature spell
        harness.passBothPriorities(); // resolve the triggered ability

        assertThat(gqs.getEffectivePower(gd, denizen)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, denizen)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger for a nonred creature")
    void noBoostForNonredCreature() {
        harness.addToBattlefield(player1, new FoundryStreetDenizen());
        Permanent denizen = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, denizen)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger for an opponent's red creature")
    void noBoostForOpponentRedCreature() {
        harness.addToBattlefield(player1, new FoundryStreetDenizen());
        Permanent denizen = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new HillGiant()));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, denizen)).isEqualTo(1);
    }

    @Test
    @DisplayName("The boost is cumulative and wears off at end of turn")
    void boostStacksAndWearsOff() {
        harness.addToBattlefield(player1, new FoundryStreetDenizen());
        Permanent denizen = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.setHand(player1, List.of(new HillGiant(), new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 8);

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
