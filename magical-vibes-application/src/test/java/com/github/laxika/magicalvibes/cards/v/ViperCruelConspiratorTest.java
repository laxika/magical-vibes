package com.github.laxika.magicalvibes.cards.v;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ViperCruelConspirator.class, GrizzlyBears.class})
class ViperCruelConspiratorTest extends BaseCardTest {

    private static final String DEATHTOUCH_MODE = "It gains deathtouch";
    private static final String LIFELINK_MODE = "It gains lifelink";

    @Test
    @DisplayName("Gives a lone attacker +1/+1 until end of turn")
    void boostsLoneAttacker() {
        Permanent attacker = setUpLoneAttacker();
        int power = gqs.getEffectivePower(gd, attacker);
        int toughness = gqs.getEffectiveToughness(gd, attacker);

        activate(0, attacker);

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(power + 1);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(toughness + 1);
    }

    @Test
    @DisplayName("The keyword ability grants the chosen keyword")
    void grantsChosenKeyword() {
        Permanent attacker = setUpLoneAttacker();

        activate(1, attacker);
        harness.handleListChoice(player1, DEATHTOUCH_MODE);

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("The keyword ability can choose lifelink")
    void grantsLifelink() {
        Permanent attacker = setUpLoneAttacker();

        activate(1, attacker);
        harness.handleListChoice(player1, LIFELINK_MODE);

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking alone")
    void requiresAttackingAloneCreature() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new ViperCruelConspirator());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking alone");
    }

    private Permanent setUpLoneAttacker() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new ViperCruelConspirator());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(1)));
        return attacker;
    }

    private void activate(int abilityIndex, Permanent target) {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, abilityIndex, null, target.getId());
        harness.passBothPriorities();
    }
}
