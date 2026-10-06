package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GhostWarden;
import com.github.laxika.magicalvibes.cards.i.IzzetSignet;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MartyredRusalka.class, GhostWarden.class, IzzetSignet.class})
class MartyredRusalkaTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature stops the target creature from attacking this turn")
    void sacrificeCreatureLocksTargetFromAttacking() {
        addReadyRusalka(player1);
        Permanent fodder = addCreatureReady(player1, new GhostWarden());
        Permanent target = addCreatureReady(player2, new GhostWarden());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(fodder);
        harness.assertInGraveyard(player1, "Ghost Warden");
        assertThatThrownBy(() -> declareAttack(target))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("The attack restriction wears off at end of turn")
    void attackRestrictionWearsOffAtEndOfTurn() {
        addReadyRusalka(player1);
        Permanent fodder = addCreatureReady(player1, new GhostWarden());
        Permanent target = addCreatureReady(player2, new GhostWarden());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();
        gd.expireEndOfTurnFloatingEffects();

        assertThatCode(() -> declareAttack(target)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        addReadyRusalka(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new IzzetSignet());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Requires white mana to activate")
    void requiresWhiteMana() {
        addReadyRusalka(player1);
        Permanent fodder = addCreatureReady(player1, new GhostWarden());
        Permanent target = addCreatureReady(player2, new GhostWarden());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(fodder);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("May sacrifice the Rusalka itself to pay the cost")
    void maySacrificeItself() {
        Permanent rusalka = addReadyRusalka(player1);
        Permanent target = addCreatureReady(player2, new GhostWarden());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(rusalka);
        assertThatThrownBy(() -> declareAttack(target))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Can activate while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent rusalka = harness.addToBattlefieldAndReturn(player1, new MartyredRusalka());
        rusalka.setSummoningSick(true);
        rusalka.tap();
        Permanent target = addCreatureReady(player2, new GhostWarden());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(rusalka);
        harness.assertInGraveyard(player1, "Martyred Rusalka");
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttack(target))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Can target a creature you control")
    void canTargetOwnCreature() {
        addReadyRusalka(player1);
        Permanent target = addCreatureReady(player1, new GhostWarden());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handlePermanentChosen(player1, findPermanent(player1, "Martyred Rusalka").getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("The affected creature can still block and activate abilities")
    void restrictionDoesNotPreventBlockingOrActivatedAbilities() {
        addReadyRusalka(player1);
        Permanent target = addCreatureReady(player2, new GhostWarden());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(bls.canBlock(gd, target)).isTrue();
        harness.activateAbility(player2, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    private Permanent addReadyRusalka(Player player) {
        return addCreatureReady(player, new MartyredRusalka());
    }

    private void declareAttack(Permanent creature) {
        int index = gd.playerBattlefields.get(player2.getId()).indexOf(creature);
        declareAttackers(player2, List.of(index));
    }
}
