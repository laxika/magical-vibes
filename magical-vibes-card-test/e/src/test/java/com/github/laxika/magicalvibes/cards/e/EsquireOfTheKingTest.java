package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EsquireOfTheKing.class, GrizzlyBears.class, IsamaruHoundOfKonda.class})
class EsquireOfTheKingTest extends BaseCardTest {

    @Test
    @DisplayName("A legendary creature reduces the activation cost and the ability boosts your creatures")
    void legendaryCreatureReducesCostAndBoostsOwnCreatures() {
        Permanent esquire = addReady(new EsquireOfTheKing());
        Permanent ownCreature = addReady(new GrizzlyBears());
        addReady(new IsamaruHoundOfKonda());
        Permanent opponentCreature = addReady(player2, new GrizzlyBears());

        addReducedAbilityMana();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(esquire.getEffectivePower()).isEqualTo(2);
        assertThat(esquire.getEffectiveToughness()).isEqualTo(2);
        assertThat(ownCreature.getEffectivePower()).isEqualTo(3);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(3);
        assertThat(opponentCreature.getEffectivePower()).isEqualTo(2);
        assertThat(opponentCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Without a legendary creature the unreduced activation cost is required")
    void legendaryCreatureIsRequiredForReducedCost() {
        addReady(new EsquireOfTheKing());
        addReducedAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("The team boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent ownCreature = addReady(new GrizzlyBears());
        addReady(new EsquireOfTheKing());
        addReady(new IsamaruHoundOfKonda());

        addReducedAbilityMana();
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        assertThat(ownCreature.getEffectivePower()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ownCreature.getEffectivePower()).isEqualTo(2);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(2);
    }

    private Permanent addReady(com.github.laxika.magicalvibes.model.Card card) {
        return addReady(player1, card);
    }

    private Permanent addReady(com.github.laxika.magicalvibes.model.Player player,
                               com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void addReducedAbilityMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
