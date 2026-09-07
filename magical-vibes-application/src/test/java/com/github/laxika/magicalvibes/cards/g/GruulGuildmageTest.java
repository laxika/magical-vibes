package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.e.ElspethKnightErrant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GruulGuildmage.class, Forest.class, ElspethKnightErrant.class, GrizzlyBears.class})
class GruulGuildmageTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a land deals 2 damage to a target player")
    void sacrificesLandAndDamagesPlayer() {
        addReadyGuildmage(player1);
        harness.addToBattlefield(player1, new Forest());
        int lifeBefore = gd.getLife(player2.getId());

        addRedAbilityMana();
        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("The first ability can target a planeswalker")
    void damagesTargetPlaneswalker() {
        addReadyGuildmage(player1);
        harness.addToBattlefield(player1, new Forest());

        Permanent planeswalker = new Permanent(new ElspethKnightErrant());
        planeswalker.setCounterCount(CounterType.LOYALTY, 4);
        gd.playerBattlefields.get(player2.getId()).add(planeswalker);

        addRedAbilityMana();
        harness.activateAbility(player1, 0, 0, null, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("The second ability gives a target creature +2/+2 until end of turn")
    void boostsTargetCreatureUntilEndOfTurn() {
        Permanent guildmage = addReadyGuildmage(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        int powerBefore = gqs.getEffectivePower(gd, target);
        int toughnessBefore = gqs.getEffectiveToughness(gd, target);

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(guildmage), 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(powerBefore + 2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(toughnessBefore + 2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(powerBefore);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(toughnessBefore);
    }

    @Test
    @DisplayName("The first ability cannot target a creature")
    void cannotTargetCreatureWithDamageAbility() {
        addReadyGuildmage(player1);
        harness.addToBattlefield(player1, new Forest());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        addRedAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyGuildmage(Player player) {
        Permanent perm = new Permanent(new GruulGuildmage());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private void addRedAbilityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
