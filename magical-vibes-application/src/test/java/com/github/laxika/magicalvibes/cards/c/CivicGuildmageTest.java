package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CivicGuildmage.class, Island.class})
class CivicGuildmageTest extends BaseCardTest {

    @Test
    @DisplayName("{G}, {T}: target creature gets +0/+1 until end of turn")
    void boostsTargetCreature() {
        Permanent guildmage = addCreatureReady(player1, new CivicGuildmage());
        Permanent target = addCreatureReady(player2, new CivicGuildmage());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(guildmage.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("The +0/+1 boost wears off at end of turn")
    void boostWearsOff() {
        Permanent guildmage = addCreatureReady(player1, new CivicGuildmage());
        Permanent target = addCreatureReady(player1, new CivicGuildmage());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(guildmage.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("{U}, {T}: puts target creature you control on top of its owner's library")
    void tucksControlledCreature() {
        Permanent guildmage = addCreatureReady(player1, new CivicGuildmage());
        Permanent target = addCreatureReady(player1, new CivicGuildmage());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(guildmage.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerDecks.get(player1.getId()).get(0).getId())
                .isEqualTo(target.getCard().getId());
    }

    @Test
    @DisplayName("The tuck ability cannot target a creature you don't control")
    void tuckRejectsOpponentCreature() {
        addCreatureReady(player1, new CivicGuildmage());
        Permanent target = addCreatureReady(player2, new CivicGuildmage());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("The boost ability cannot target a noncreature permanent")
    void boostRejectsNonCreaturePermanent() {
        addCreatureReady(player1, new CivicGuildmage());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("The tuck ability can target Civic Guildmage itself")
    void tucksItself() {
        Permanent guildmage = addCreatureReady(player1, new CivicGuildmage());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, guildmage.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(guildmage.getId()));
        assertThat(gd.playerDecks.get(player1.getId()).get(0).getId())
                .isEqualTo(guildmage.getCard().getId());
    }
}
