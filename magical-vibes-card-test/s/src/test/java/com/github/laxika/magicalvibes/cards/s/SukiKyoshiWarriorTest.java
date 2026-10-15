package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SukiKyoshiWarrior.class, GrizzlyBears.class, Plains.class})
class SukiKyoshiWarriorTest extends BaseCardTest {

    @Test
    @DisplayName("Power equals the number of creatures you control and toughness stays 4")
    void powerEqualsControlledCreatures() {
        Permanent suki = addCreatureReady(player1, new SukiKyoshiWarrior());

        assertThat(gqs.getEffectivePower(gd, suki)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, suki)).isEqualTo(4);

        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, suki)).isEqualTo(3);
    }

    @Test
    @DisplayName("Power ignores creatures controlled by an opponent")
    void powerIgnoresOpponentsCreatures() {
        Permanent suki = addCreatureReady(player1, new SukiKyoshiWarrior());

        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, suki)).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking creates a tapped and attacking Ally token")
    void attackingCreatesTappedAndAttackingAllyToken() {
        Permanent suki = addCreatureReady(player1, new SukiKyoshiWarrior());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });

        Permanent token = findPermanents(player1, "Ally").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.isTapped()).isTrue();
        assertThat(token.isAttacking()).isTrue();
        assertThat(token.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(token.isAttackedThisTurn()).isFalse();
        assertThat(findPermanents(player1, "Ally")).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(token.getCard().getColors()).containsExactly(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.ALLY);
        assertThat(gqs.getEffectivePower(gd, suki)).isEqualTo(2);
    }

    @Test
    @DisplayName("Noncreature permanents do not increase Suki's power")
    void powerIgnoresNoncreaturePermanents() {
        Permanent suki = addCreatureReady(player1, new SukiKyoshiWarrior());
        harness.addToBattlefield(player1, new Plains());

        assertThat(gqs.getEffectivePower(gd, suki)).isEqualTo(1);
    }

    @Test
    @DisplayName("Counters modify Suki's characteristic power and printed toughness")
    void countersModifyPowerAndToughness() {
        Permanent suki = addCreatureReady(player1, new SukiKyoshiWarrior());
        suki.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        assertThat(gqs.getEffectivePower(gd, suki)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, suki)).isEqualTo(6);
    }

    @Test
    @DisplayName("Suki's characteristic power works in the graveyard without counting itself")
    void powerWorksInGraveyard() {
        SukiKyoshiWarrior suki = new SukiKyoshiWarrior();
        gd.playerGraveyards.get(player1.getId()).add(suki);

        assertThat(gqs.getEffectiveCardPower(gd, suki)).isZero();
        addCreatureReady(player1, new SukiKyoshiWarrior());
        addCreatureReady(player2, new SukiKyoshiWarrior());

        assertThat(gqs.getEffectiveCardPower(gd, suki)).isEqualTo(1);
        assertThat(gqs.getEffectiveCardToughness(gd, suki)).isEqualTo(4);
    }

    @Test
    @DisplayName("The attack ability creates its token even after Suki leaves the battlefield")
    void attackTriggerSurvivesSourceLeaving() {
        Permanent suki = addCreatureReady(player1, new SukiKyoshiWarrior());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            gd.playerBattlefields.get(player1.getId()).remove(suki);
            gd.playerGraveyards.get(player1.getId()).add(suki.getCard());
            resolveAllTriggers();
        });

        Permanent token = findPermanent(player1, "Ally");
        assertThat(token.isTapped()).isTrue();
        assertThat(token.isAttacking()).isTrue();
        assertThat(token.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(findPermanents(player1, "Ally")).hasSize(1);
    }
}
