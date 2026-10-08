package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SwarmGuildmage.class})
class SwarmGuildmageTest extends BaseCardTest {

    @Test
    @DisplayName("First ability boosts and grants menace to creatures you control only")
    void boostsOwnCreaturesAndGrantsMenace() {
        Permanent source = addCreatureReady(player1, new SwarmGuildmage());
        Permanent ownCreature = addCreatureReady(player1, new SwarmGuildmage());
        Permanent opponentCreature = addCreatureReady(player2, new SwarmGuildmage());
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, source, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.MENACE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("First ability wears off at end of turn")
    void firstAbilityWearsOffAtEndOfTurn() {
        Permanent source = addCreatureReady(player1, new SwarmGuildmage());
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, source, Keyword.MENACE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, source, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Second ability gains 2 life")
    void gainsTwoLife() {
        addCreatureReady(player1, new SwarmGuildmage());
        int lifeBefore = gd.getLife(player1.getId());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    void firstAbilityAffectsCreaturesPresentAtResolutionOnly() {
        addCreatureReady(player1, new SwarmGuildmage());
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.activateAbility(player1, 0, 0, null, null);

        Permanent beforeResolution = addCreatureReady(player1, new SwarmGuildmage());
        harness.passBothPriorities();
        Permanent afterResolution = addCreatureReady(player1, new SwarmGuildmage());

        assertThat(gqs.getEffectivePower(gd, beforeResolution)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, beforeResolution)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, beforeResolution, Keyword.MENACE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, afterResolution)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, afterResolution, Keyword.MENACE)).isFalse();
    }

    @Test
    void firstAbilityResolvesAfterSourceLeavesBattlefield() {
        Permanent source = addCreatureReady(player1, new SwarmGuildmage());
        Permanent other = addCreatureReady(player1, new SwarmGuildmage());
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.activateAbility(player1, 0, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, other, Keyword.MENACE)).isTrue();
    }

    @Test
    void lifeGainUsesStackAndTapsSourceImmediately() {
        Permanent source = addCreatureReady(player1, new SwarmGuildmage());
        int controllerLife = gd.getLife(player1.getId());
        int opponentLife = gd.getLife(player2.getId());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(source.isTapped()).isTrue();
        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLife);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLife + 2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife);
    }

    @Test
    void bothAbilitiesRequireUntappedSourceWithoutSummoningSickness() {
        Permanent source = addCreatureReady(player1, new SwarmGuildmage());
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.addMana(player1, ManaColor.GREEN, 2);

        source.setSummoningSick(true);
        for (int abilityIndex = 0; abilityIndex < 2; abilityIndex++) {
            int index = abilityIndex;
            assertThatThrownBy(() -> harness.activateAbility(player1, 0, index, null, null))
                    .isInstanceOf(IllegalStateException.class);
        }
        source.setSummoningSick(false);
        source.tap();
        for (int abilityIndex = 0; abilityIndex < 2; abilityIndex++) {
            int index = abilityIndex;
            assertThatThrownBy(() -> harness.activateAbility(player1, 0, index, null, null))
                    .isInstanceOf(IllegalStateException.class);
        }
        assertThat(gd.stack).isEmpty();
    }
}
