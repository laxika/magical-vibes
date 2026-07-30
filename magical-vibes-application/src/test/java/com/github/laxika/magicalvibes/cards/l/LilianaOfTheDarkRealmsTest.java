package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LilianaOfTheDarkRealmsTest extends BaseCardTest {

    private static final String PLUS_MODE = "Target creature gets +X/+X until end of turn.";
    private static final String MINUS_MODE = "Target creature gets -X/-X until end of turn.";

    @Test
    @DisplayName("+1 tutors a Swamp from the library to hand")
    void plusOneTutorsSwamp() {
        Permanent liliana = addReadyLiliana(player1);
        harness.setLibrary(player1, List.of(new Forest(), new Swamp()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        var offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        assertThat(offered).allMatch(card -> card.getName().equals("Swamp"));
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        harness.assertInHand(player1, "Swamp");
    }

    @Test
    @DisplayName("-3 with the +X/+X mode pumps the target by the number of Swamps controlled")
    void minusThreePumpsBySwampCount() {
        Permanent liliana = addReadyLiliana(player1);
        liliana.setCounterCount(CounterType.LOYALTY, 4);
        addSwamp(player1);
        addSwamp(player1);
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, bear.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, PLUS_MODE);

        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(bear.getEffectivePower()).isEqualTo(4);
        assertThat(bear.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("-3 with the -X/-X mode shrinks the target and kills it when toughness hits zero")
    void minusThreeShrinksAndKills() {
        addReadyLiliana(player1);
        addSwamp(player1);
        addSwamp(player1);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, bear.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, MINUS_MODE);

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("-3 counts only Swamps you control")
    void minusThreeCountsOnlyOwnSwamps() {
        addReadyLiliana(player1);
        addSwamp(player1);
        addSwamp(player2);
        addSwamp(player2);
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, bear.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, PLUS_MODE);

        assertThat(bear.getEffectivePower()).isEqualTo(3);
    }

    @Test
    @DisplayName("-3 pump wears off at end of turn")
    void minusThreePumpWearsOff() {
        addReadyLiliana(player1);
        addSwamp(player1);
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, bear.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, PLUS_MODE);
        assertThat(bear.getEffectivePower()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("-3 cannot target a noncreature permanent")
    void minusThreeCannotTargetLand() {
        addReadyLiliana(player1);
        Permanent swamp = addSwamp(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, swamp.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-6 creates an emblem granting Swamps you control a four-black mana ability")
    void minusSixCreatesEmblem() {
        Permanent liliana = addReadyLiliana(player1);
        liliana.setCounterCount(CounterType.LOYALTY, 6);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.emblems).hasSize(1);
        Emblem emblem = gd.emblems.getFirst();
        assertThat(emblem.controllerId()).isEqualTo(player1.getId());
        assertThat(emblem.staticEffects()).hasSize(1);
        GrantActivatedAbilityEffect grant = (GrantActivatedAbilityEffect) emblem.staticEffects().getFirst();
        assertThat(grant.scope()).isEqualTo(GrantScope.OWN_PERMANENTS);
        assertThat(grant.filter()).isEqualTo(new PermanentHasSubtypePredicate(CardSubtype.SWAMP));
        assertThat(grant.ability().isRequiresTap()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate -6 with only 3 loyalty")
    void cannotUltimateWithoutLoyalty() {
        addReadyLiliana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    private Permanent addReadyLiliana(Player player) {
        Permanent perm = new Permanent(new LilianaOfTheDarkRealms());
        perm.setCounterCount(CounterType.LOYALTY, 3);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private Permanent addSwamp(Player player) {
        Permanent perm = new Permanent(new Swamp());
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
