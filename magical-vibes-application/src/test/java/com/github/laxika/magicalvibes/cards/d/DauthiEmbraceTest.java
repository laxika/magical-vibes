package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CanopySpider;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DauthiEmbrace.class, CanopySpider.class, DauthiMarauder.class})
class DauthiEmbraceTest extends BaseCardTest {

    @Test
    @DisplayName("Ability grants shadow to target creature you control")
    void grantsShadowToOwnCreature() {
        harness.addToBattlefield(player1, new DauthiEmbrace());
        Permanent creature = addCreatureReady(player1, new CanopySpider());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHADOW)).isTrue();
    }

    @Test
    @DisplayName("Ability can grant shadow to a creature an opponent controls")
    void grantsShadowToOpponentCreature() {
        harness.addToBattlefield(player1, new DauthiEmbrace());
        Permanent opponentCreature = addCreatureReady(player2, new CanopySpider());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.SHADOW)).isTrue();
    }

    @Test
    @DisplayName("Granted shadow wears off at end of turn")
    void shadowWearsOff() {
        harness.addToBattlefield(player1, new DauthiEmbrace());
        Permanent creature = addCreatureReady(player1, new CanopySpider());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHADOW)).isFalse();
    }

    @Test
    @DisplayName("Ability can target creatures only")
    void cannotTargetNonCreaturePermanent() {
        Permanent embrace = harness.addToBattlefieldAndReturn(player1, new DauthiEmbrace());
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, embrace.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Granted shadow prevents a non-shadow creature from blocking")
    void grantedShadowPreventsNonShadowCreatureFromBlocking() {
        harness.addToBattlefield(player1, new DauthiEmbrace());
        Permanent attacker = addCreatureReady(player1, new CanopySpider());
        addCreatureReady(player2, new CanopySpider());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shadow");
    }

    @Test
    @DisplayName("Granted shadow can be blocked by a creature with shadow")
    void grantedShadowCanBeBlockedByShadowCreature() {
        harness.addToBattlefield(player1, new DauthiEmbrace());
        Permanent attacker = addCreatureReady(player1, new CanopySpider());
        Permanent blocker = addCreatureReady(player2, new DauthiMarauder());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1))))
                .doesNotThrowAnyException();
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Ability requires {B}{B}")
    void requiresMana() {
        harness.addToBattlefield(player1, new DauthiEmbrace());
        Permanent creature = addCreatureReady(player1, new CanopySpider());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
    }
}
