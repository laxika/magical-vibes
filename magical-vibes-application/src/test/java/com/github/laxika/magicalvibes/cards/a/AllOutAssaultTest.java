package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AllOutAssault.class, GrizzlyBears.class})
class AllOutAssaultTest extends BaseCardTest {

    @Test
    void boostsControlledCreaturesAndGrantsDeathtouch() {
        harness.addToBattlefield(player1, new AllOutAssault());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingBear = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opposingBear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opposingBear, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void enteringDuringMainPhaseAddsCombatAndMainPhaseAndSchedulesNextAttackUntap() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent tappedCreature = addCreatureReady(player1, new GrizzlyBears());
        tappedCreature.tap();

        castFromPostcombatMainPhase();

        assertThat(gd.additionalCombatMainPhasePairs).isEqualTo(1);

        declareAttackers(List.of(0));
        assertThat(attacker.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(attacker.isTapped()).isFalse();
        assertThat(tappedCreature.isTapped()).isFalse();
        assertThat(gd.additionalCombatMainPhasePairs).isEqualTo(1);
    }

    @Test
    void nextAttackUntapTriggerFiresOnlyOnce() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        castFromPostcombatMainPhase();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("All-Out Assault"));
    }

    private void castFromPostcombatMainPhase() {
        harness.setHand(player1, List.of(new AllOutAssault()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
