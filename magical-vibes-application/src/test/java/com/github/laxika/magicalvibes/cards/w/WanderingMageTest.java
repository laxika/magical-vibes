package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GuerrillaTactics;
import com.github.laxika.magicalvibes.cards.s.SwornDefender;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WanderingMage.class, GuerrillaTactics.class, SwornDefender.class})
class WanderingMageTest extends BaseCardTest {

    private Permanent addMageReady() {
        Permanent mage = harness.addToBattlefieldAndReturn(player1, new WanderingMage());
        harness.forceActivePlayer(player1);
        return mage;
    }

    @Test
    @DisplayName("{W}, Pay 1 life shields target creature for 2 and costs 1 life")
    void whiteAbilityShieldsCreatureAndPaysLife() {
        addMageReady();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setLife(player1, 20);
        Permanent defender = harness.addToBattlefieldAndReturn(player2, new SwornDefender());

        harness.activateAbility(player1, 0, 0, null, defender.getId());
        harness.passBothPriorities();

        assertThat(defender.getDamagePreventionShield()).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("The white ability prevents the next 2 damage to its target creature")
    void whiteAbilityPreventsDamageToTargetCreature() {
        addMageReady();
        harness.addMana(player1, ManaColor.WHITE, 1);
        Permanent defender = harness.addToBattlefieldAndReturn(player2, new SwornDefender());

        harness.activateAbility(player1, 0, 0, null, defender.getId());
        harness.passBothPriorities();
        castGuerrillaTactics(player2, defender.getId());

        assertThat(defender.getMarkedDamage()).isZero();
        assertThat(defender.getDamagePreventionShield()).isZero();
    }

    @Test
    @DisplayName("The white ability cannot target a player")
    void whiteAbilityCannotTargetPlayer() {
        addMageReady();
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("{U} shields a Cleric or Wizard creature for 1")
    void blueAbilityShieldsClericOrWizard() {
        Permanent mage = addMageReady();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, mage.getId());
        harness.passBothPriorities();

        assertThat(mage.getDamagePreventionShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("The blue ability can target an opponent's Cleric or Wizard creature")
    void blueAbilityCanTargetOpponentsQualifyingCreature() {
        addMageReady();
        harness.addMana(player1, ManaColor.BLUE, 1);
        Permanent opponentMage = harness.addToBattlefieldAndReturn(player2, new WanderingMage());

        harness.activateAbility(player1, 0, 1, null, opponentMage.getId());
        harness.passBothPriorities();

        assertThat(opponentMage.getDamagePreventionShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("The blue ability prevents only the next 1 damage to its target")
    void blueAbilityPreventsOneDamage() {
        Permanent mage = addMageReady();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, mage.getId());
        harness.passBothPriorities();
        castGuerrillaTactics(player2, mage.getId());

        assertThat(mage.getMarkedDamage()).isEqualTo(1);
        assertThat(mage.getDamagePreventionShield()).isZero();
    }

    @Test
    @DisplayName("The blue ability cannot target a creature that is neither Cleric nor Wizard")
    void blueAbilityRejectsOtherCreatures() {
        addMageReady();
        harness.addMana(player1, ManaColor.BLUE, 1);
        Permanent defender = harness.addToBattlefieldAndReturn(player2, new SwornDefender());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, defender.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("{B} plus a -1/-1 counter shields target player for 2")
    void blackAbilityShieldsPlayer() {
        Permanent mage = addMageReady();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 2, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDamagePreventionShields.getOrDefault(player1.getId(), 0)).isEqualTo(2);
        assertThat(mage.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The black ability prevents the next 2 damage to its target player")
    void blackAbilityPreventsDamageToPlayer() {
        Permanent mage = addMageReady();
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 2, null, player1.getId());
        harness.passBothPriorities();
        castGuerrillaTactics(player2, player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(mage.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The black ability can target a planeswalker")
    void blackAbilityCanTargetPlaneswalker() {
        addMageReady();
        Permanent planeswalker = addTestPlaneswalker(player2, 5);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 2, null, planeswalker.getId());
        harness.passBothPriorities();
        castGuerrillaTactics(player2, planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(planeswalker.getDamagePreventionShield()).isZero();
    }

    @Test
    @DisplayName("The black ability puts its cost counter on the chosen controlled creature")
    void blackAbilityPutsCounterOnChosenControlledCreature() {
        Permanent mage = addMageReady();
        Permanent otherMage = harness.addToBattlefieldAndReturn(player1, new WanderingMage());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 2, null, player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();

        harness.handlePermanentChosen(player1, otherMage.getId());
        harness.passBothPriorities();

        assertThat(mage.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(otherMage.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.playerDamagePreventionShields.get(player1.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("The black ability cannot target a creature")
    void blackAbilityCannotTargetCreature() {
        addMageReady();
        harness.addMana(player1, ManaColor.BLACK, 1);
        UUID defenderId = harness.addToBattlefieldAndReturn(player2, new SwornDefender()).getId();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, defenderId))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castGuerrillaTactics(Player caster, UUID targetId) {
        harness.setHand(caster, List.of(new GuerrillaTactics()));
        harness.addMana(caster, ManaColor.RED, 2);
        harness.castAndResolveInstant(caster, 0, targetId);
    }

    private Permanent addTestPlaneswalker(Player player, int loyalty) {
        Card card = new Card();
        card.setName("Test Planeswalker");
        card.setType(CardType.PLANESWALKER);
        card.setOwnerId(player.getId());
        Permanent planeswalker = new Permanent(card);
        planeswalker.setCounterCount(CounterType.LOYALTY, loyalty);
        gd.playerBattlefields.get(player.getId()).add(planeswalker);
        return planeswalker;
    }
}
