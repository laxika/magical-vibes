package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(ThopterSquadron.class)
class ThopterSquadronTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with three +1/+1 counters")
    void entersWithThreeCounters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ThopterSquadron()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent squadron = findPermanent(player1, "Thopter Squadron");
        assertThat(squadron.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(squadron.getEffectivePower()).isEqualTo(3);
        assertThat(squadron.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Removing a +1/+1 counter creates a 1/1 colorless Thopter artifact creature token")
    void removingCounterCreatesThopterToken() {
        Permanent squadron = addSquadron(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, indexOf(player1, squadron), 0, null, null);
        assertThat(squadron.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
        harness.passBothPriorities();

        assertThat(squadron.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        Permanent token = findThopterToken();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isNull();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.THOPTER);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Sacrificing another Thopter puts a +1/+1 counter on Thopter Squadron")
    void sacrificingAnotherThopterAddsCounter() {
        Permanent squadron = addSquadron(player1);
        Permanent sacrificedThopter = addSquadron(player1);
        Permanent remainingThopter = addSquadron(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, indexOf(player1, squadron), 1, null, null);
        harness.handlePermanentChosen(player1, sacrificedThopter.getId());
        harness.passBothPriorities();

        assertThat(squadron.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sacrificedThopter);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(remainingThopter);
        harness.assertOnBattlefield(player1, "Thopter Squadron");
    }

    @Test
    @DisplayName("The sacrifice ability cannot sacrifice Thopter Squadron itself")
    void sacrificeAbilityRequiresAnotherThopter() {
        Permanent squadron = addSquadron(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, squadron), 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activated abilities can only be activated at sorcery speed")
    void activatedAbilitiesRequireSorcerySpeed() {
        Permanent squadron = addSquadron(player1);
        addSquadron(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, squadron), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, squadron), 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("The token ability cannot be activated without a +1/+1 counter")
    void tokenAbilityRequiresPlusOneCounter() {
        Permanent squadron = addSquadron(player1);
        squadron.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, squadron), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");
    }

    @Test
    @DisplayName("Removing the last counter kills the Squadron but still creates a token")
    void lastCounterStillCreatesTokenAfterSourceDies() {
        Permanent squadron = addSquadron(player1);
        squadron.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, indexOf(player1, squadron), 0, null, null);

        harness.assertNotOnBattlefield(player1, "Thopter Squadron");
        harness.assertInGraveyard(player1, "Thopter Squadron");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(findThopterToken()).isNotNull();
        assertThat(countPermanents(player1, "Thopter")).isEqualTo(1);
    }

    @Test
    @DisplayName("A created token can be sacrificed as a cost before the counter is added")
    void createdTokenCanBeReabsorbed() {
        Permanent squadron = addSquadron(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, indexOf(player1, squadron), 0, null, null);
        harness.passBothPriorities();
        Permanent token = findThopterToken();

        harness.activateAbility(player1, indexOf(player1, squadron), 1, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token);
        assertThat(squadron.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(squadron.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(countPermanents(player1, "Thopter")).isZero();
    }

    @Test
    @DisplayName("Neither ability can be activated with an ability already on the stack")
    void abilitiesRequireEmptyStack() {
        Permanent squadron = addSquadron(player1);
        addSquadron(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, indexOf(player1, squadron), 0, null, null);

        for (int abilityIndex = 0; abilityIndex < 2; abilityIndex++) {
            int index = abilityIndex;
            assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, squadron), index, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("stack is empty");
        }
        assertThat(squadron.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("An opponent's Thopter cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsThopter() {
        Permanent squadron = addSquadron(player1);
        Permanent opponentThopter = addSquadron(player2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, squadron), 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentThopter);
        assertThat(squadron.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    private Permanent addSquadron(Player player) {
        return harness.enterBattlefieldAndReturn(player, new ThopterSquadron());
    }

    private Permanent findThopterToken() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.THOPTER))
                .findFirst()
                .orElseThrow();
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
