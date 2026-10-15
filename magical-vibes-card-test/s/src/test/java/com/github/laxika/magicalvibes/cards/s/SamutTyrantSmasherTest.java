package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GoblinAssailant;
import com.github.laxika.magicalvibes.cards.p.PollenbrightDruid;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SamutTyrantSmasher.class, GoblinAssailant.class, PollenbrightDruid.class})
class SamutTyrantSmasherTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control have haste")
    void grantsHasteToOwnCreatures() {
        addReadySamut(player1);
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GoblinAssailant());
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GoblinAssailant());

        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingBear, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("-1 boosts a target creature, grants haste, and scries 1")
    void minusOneBoostsAndScries() {
        Permanent samut = addReadySamut(player1);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GoblinAssailant());

        harness.activateAbility(player1, 0, 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.HASTE)).isTrue();
        assertThat(samut.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("-1 cannot target a planeswalker")
    void minusOneCannotTargetPlaneswalker() {
        Permanent samut = addReadySamut(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, samut.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void illegalTargetPreventsScryAndStillCostsLoyalty() {
        Permanent samut = addReadySamut(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PollenbrightDruid());
        PollenbrightDruid top = new PollenbrightDruid();
        harness.setLibrary(player1, List.of(top));

        harness.activateAbility(player1, 0, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(samut.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    void abilityResolvesAfterSpendingSamutsLastLoyaltyAndCanBottomTheScryCard() {
        Permanent samut = addReadySamut(player1);
        samut.setCounterCount(CounterType.LOYALTY, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PollenbrightDruid());
        PollenbrightDruid top = new PollenbrightDruid();
        SamutTyrantSmasher second = new SamutTyrantSmasher();
        harness.setLibrary(player1, List.of(top, second));

        harness.activateAbility(player1, 0, 0, null, target.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(samut);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(samut.getCard());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, top);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void staticHasteEndsWhenSamutLeavesTheBattlefield() {
        Permanent samut = addReadySamut(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new PollenbrightDruid());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(samut);
        gd.playerGraveyards.get(player1.getId()).add(samut.getCard());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
    }

    private Permanent addReadySamut(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new SamutTyrantSmasher());
        perm.setCounterCount(CounterType.LOYALTY, 4);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
