package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.v.VolunteerMilitia;
import com.github.laxika.magicalvibes.model.GameLogEntry;
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

@CardUsed({WieldingTheGreenDragon.class, VolunteerMilitia.class, Forest.class})
class WieldingTheGreenDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving gives +4/+4 to target creature")
    void resolvesAndBoostsTarget() {
        Permanent militia = harness.addToBattlefieldAndReturn(player1, new VolunteerMilitia());
        harness.setHand(player1, List.of(new WieldingTheGreenDragon()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, militia.getId());

        assertThat(militia.getEffectivePower()).isEqualTo(5);
        assertThat(militia.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("Can target a creature controlled by an opponent")
    void canTargetOpponentsCreature() {
        Permanent militia = harness.addToBattlefieldAndReturn(player2, new VolunteerMilitia());
        harness.setHand(player1, List.of(new WieldingTheGreenDragon()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, militia.getId());

        assertThat(militia.getEffectivePower()).isEqualTo(5);
        assertThat(militia.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("Boost wears off at cleanup step")
    void boostWearsOffAtCleanup() {
        Permanent militia = harness.addToBattlefieldAndReturn(player1, new VolunteerMilitia());
        harness.setHand(player1, List.of(new WieldingTheGreenDragon()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, militia.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player1, TurnStep.CLEANUP);

        assertThat(militia.getEffectivePower()).isEqualTo(1);
        assertThat(militia.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Fizzles if target is removed")
    void fizzlesIfTargetRemoved() {
        Permanent militia = harness.addToBattlefieldAndReturn(player1, new VolunteerMilitia());
        harness.setHand(player1, List.of(new WieldingTheGreenDragon()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, militia.getId());
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(harness.getGameData().gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new VolunteerMilitia()); // legal creature target so the spell is castable (CR 601.2c)
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new WieldingTheGreenDragon()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
