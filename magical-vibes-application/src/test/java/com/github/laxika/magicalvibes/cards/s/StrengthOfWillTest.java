package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StrengthOfWill.class, GrizzlyBears.class, Shock.class, FugitiveWizard.class})
class StrengthOfWillTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gains indestructible and gets +1/+1 counters equal to damage dealt")
    void protectsAndGrowsCreatureAfterNoncombatDamage() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        castOn(player1, bears);

        assertThat(bears.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(bears.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The damage trigger also works for combat damage")
    void growsCreatureAfterCombatDamage() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        castOn(player1, bears);

        Permanent attacker = addCreatureReady(player2, new FugitiveWizard());
        attacker.setAttacking(true);
        bears.setBlocking(true);
        bears.addBlockingTarget(0);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The granted abilities wear off at end of turn")
    void grantedAbilitiesWearOffAtEndOfTurn() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        castOn(player1, bears);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a creature controlled by an opponent")
    void cannotTargetOpponentCreature() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new StrengthOfWill()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castOn(Player caster, Permanent target) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new StrengthOfWill()));
        harness.addMana(caster, ManaColor.GREEN, 1);
        harness.addMana(caster, ManaColor.COLORLESS, 1);
        harness.castInstant(caster, 0, target.getId());
        harness.passBothPriorities();
    }
}
