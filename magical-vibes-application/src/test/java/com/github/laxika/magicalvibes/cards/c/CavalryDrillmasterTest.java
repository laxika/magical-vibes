package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CavalryDrillmaster.class, GrizzlyBears.class})
class CavalryDrillmasterTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives target creature +2/+0 and first strike")
    void etbBoostsAndGrantsFirstStrike() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CavalryDrillmaster()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0, bears.getId());

        harness.passBothPriorities(); // Resolve creature
        harness.passBothPriorities(); // Resolve ETB

        assertThat(gd.stack).isEmpty();

        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
        assertThat(bears.getGrantedKeywords()).contains(Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("Can target an opponent's creature")
    void canTargetOpponentCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CavalryDrillmaster()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0, bears.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.getGrantedKeywords()).contains(Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("Boost and first strike wear off at end of turn")
    void boostAndFirstStrikeWearOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CavalryDrillmaster()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0, bears.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
        assertThat(bears.getGrantedKeywords()).doesNotContain(Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("ETB fizzles if target creature is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CavalryDrillmaster()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castCreature(player1, 0, targetId);

        harness.passBothPriorities(); // ETB on stack

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getId().equals(targetId));

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Drillmaster can target itself when it enters without being cast")
    void canTargetItselfWhenEnteringWithoutBeingCast() {
        Permanent drillmaster = harness.enterBattlefieldAndReturn(player1, new CavalryDrillmaster());

        harness.handlePermanentChosen(player1, drillmaster.getId());
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(drillmaster.getEffectivePower()).isEqualTo(4);
        assertThat(drillmaster.getEffectiveToughness()).isEqualTo(1);
        assertThat(drillmaster.getGrantedKeywords()).contains(Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("ETB resolves even if Drillmaster leaves before resolution")
    void abilityResolvesAfterSourceLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CavalryDrillmaster());
        harness.setHand(player1, List.of(new CavalryDrillmaster()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();

        Permanent source = findPermanent(player1, "Cavalry Drillmaster");
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
        assertThat(target.getGrantedKeywords()).contains(Keyword.FIRST_STRIKE);
    }
}
