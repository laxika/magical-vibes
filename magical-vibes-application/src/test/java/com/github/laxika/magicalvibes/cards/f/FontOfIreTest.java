package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AjaniMentorOfHeroes;
import com.github.laxika.magicalvibes.cards.s.SatyrGrovedancer;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FontOfIre.class, AjaniMentorOfHeroes.class, SatyrGrovedancer.class})
class FontOfIreTest extends BaseCardTest {

    @Test
    @DisplayName("sacrificing Font of Ire deals 5 damage to a player")
    void sacrificingFontOfIreDealsFiveDamageToPlayer() {
        harness.setLife(player2, 20);
        Permanent font = harness.addToBattlefieldAndReturn(player1, new FontOfIre());
        addActivationMana();

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(font);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(font.getCard());

        harness.passBothPriorities();

        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("can deal damage to a planeswalker")
    void dealsDamageToPlaneswalker() {
        Permanent walker = harness.addToBattlefieldAndReturn(player2, new AjaniMentorOfHeroes());
        walker.setCounterCount(CounterType.LOYALTY, 6);
        harness.addToBattlefield(player1, new FontOfIre());
        addActivationMana();

        harness.activateAbility(player1, 0, null, walker.getId());
        harness.passBothPriorities();

        assertThat(walker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player1, new FontOfIre());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SatyrGrovedancer());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("can target its controller")
    void canTargetItsController() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new FontOfIre());
        addActivationMana();

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
    }

    @Test
    @DisplayName("cannot activate without the required red mana")
    void cannotActivateWithoutRedMana() {
        Permanent font = harness.addToBattlefieldAndReturn(player1, new FontOfIre());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(font);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(font.getCard());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("an absent planeswalker target does not redirect damage to its controller")
    void absentPlaneswalkerDoesNotRedirectDamage() {
        harness.setLife(player2, 20);
        Permanent walker = harness.addToBattlefieldAndReturn(player2, new AjaniMentorOfHeroes());
        walker.setCounterCount(CounterType.LOYALTY, 6);
        Permanent font = harness.addToBattlefieldAndReturn(player1, new FontOfIre());
        addActivationMana();

        harness.activateAbility(player1, 0, null, walker.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, walker));
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(font.getCard());
        assertThat(gd.stack).isEmpty();
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
