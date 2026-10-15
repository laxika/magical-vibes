package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScrappyBruiser.class, GrizzlyBears.class})
class ScrappyBruiserTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking boosts another attacking creature and gives it trample")
    void boostsAnotherAttackingCreature() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new ScrappyBruiser());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The targeted attacker returns to its owner's hand at end of combat")
    void returnsTargetedAttackerAtEndOfCombat() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new ScrappyBruiser());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        Permanent attacker = gd.playerBattlefields.get(player1.getId()).get(1);
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        resolveCombat();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The attack trigger can resolve without a target")
    void canChooseNoTarget() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new ScrappyBruiser());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertOnBattlefield(player1, "Scrappy Bruiser");
    }

    @Test
    @DisplayName("A non-attacking creature cannot be targeted")
    void cannotTargetNonAttackingCreature() {
        addCreatureReady(player1, new ScrappyBruiser());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent nonAttacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonAttacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Scrappy Bruiser can target itself and return itself at end of combat")
    void canTargetItself() {
        Permanent bruiser = addCreatureReady(player1, new ScrappyBruiser());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, bruiser.getId());
            harness.passBothPriorities();
        });

        assertThat(gqs.getEffectivePower(gd, bruiser)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, bruiser, Keyword.TRAMPLE)).isTrue();

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertNotOnBattlefield(player1, "Scrappy Bruiser");
        harness.assertInHand(player1, "Scrappy Bruiser");
    }

    @Test
    @DisplayName("The delayed return retains Scrappy Bruiser as its source")
    void delayedReturnRetainsOriginalSource() {
        Permanent bruiser = addCreatureReady(player1, new ScrappyBruiser());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1));
            harness.handlePermanentChosen(player1, attacker.getId());
            harness.passBothPriorities();
        });
        harness.passUntil(TurnStep.END_OF_COMBAT);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(bruiser.getId());
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
