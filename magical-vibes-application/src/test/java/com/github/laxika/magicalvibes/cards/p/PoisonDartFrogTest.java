package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.m.MinersGuidewing;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PoisonDartFrog.class, MinersGuidewing.class})
class PoisonDartFrogTest extends BaseCardTest {

    @Test
    @DisplayName("Taps for one mana of any color")
    void tapsForAnyColor() {
        Permanent frog = addCreatureReady(player1, new PoisonDartFrog());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(frog.isTapped()).isTrue();
    }

    @Test
    @DisplayName("{2}: gains deathtouch until end of turn")
    void gainsDeathtouchUntilEndOfTurn() {
        Permanent frog = addCreatureReady(player1, new PoisonDartFrog());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, frog, Keyword.DEATHTOUCH)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, frog, Keyword.DEATHTOUCH)).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void manaAbilityResolvesImmediatelyForEachColor(ManaColor color) {
        Permanent frog = addCreatureReady(player1, new PoisonDartFrog());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(frog.isTapped()).isTrue();
    }

    @Test
    void cannotTapForManaWithSummoningSickness() {
        Permanent frog = addCreatureReady(player1, new PoisonDartFrog());
        frog.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(frog.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canGainDeathtouchWhileTappedAndSummoningSick() {
        Permanent frog = addCreatureReady(player1, new PoisonDartFrog());
        frog.setSummoningSick(true);
        frog.setTapped(true);
        Permanent otherFrog = addCreatureReady(player1, new PoisonDartFrog());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.hasKeyword(gd, frog, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, frog, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherFrog, Keyword.DEATHTOUCH)).isFalse();
        assertThat(frog.isTapped()).isTrue();
    }

    @Test
    void cannotGainDeathtouchWithoutPayingTwoMana() {
        Permanent frog = addCreatureReady(player1, new PoisonDartFrog());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, frog, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void reachAllowsBlockingFlyingCreature() {
        Permanent attacker = addCreatureReady(player1, new MinersGuidewing());
        attacker.setAttacking(true);
        Permanent frog = addCreatureReady(player2, new PoisonDartFrog());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(frog.isBlocking()).isTrue();
    }
}
