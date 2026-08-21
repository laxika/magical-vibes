package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StadiumHeadliner.class, AirElemental.class, Forest.class, GrizzlyBears.class})
class StadiumHeadlinerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates a tapped and attacking red Warrior token")
    void attackingCreatesTappedAndAttackingWarriorToken() {
        addCreatureReady(player1, new StadiumHeadliner());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        List<Permanent> tokens = findPermanents(player1, "Warrior").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().isTapped()).isTrue();
        assertThat(tokens.getFirst().isAttackedThisTurn()).isTrue();
    }

    @Test
    @DisplayName("The mobilized token is sacrificed at the beginning of the next end step")
    void mobilizedTokenIsSacrificedAtNextEndStep() {
        addCreatureReady(player1, new StadiumHeadliner());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Warrior").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList()).isEmpty();
    }

    @Test
    @DisplayName("Sacrificing deals damage equal to the creatures still controlled")
    void sacrificeAbilityDealsDamageEqualToCreatureCount() {
        addCreatureReady(player1, new StadiumHeadliner());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new AirElemental());

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Stadium Headliner");
    }

    @Test
    @DisplayName("The sacrifice ability cannot target a noncreature permanent")
    void sacrificeAbilityCannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new StadiumHeadliner());
        harness.addToBattlefield(player2, new Forest());
        Permanent target = findPermanent(player2, "Forest");

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }
}
