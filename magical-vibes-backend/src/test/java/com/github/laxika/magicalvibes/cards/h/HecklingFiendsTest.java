package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.MustAttackThisTurnEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HecklingFiendsTest extends BaseCardTest {

    @Test
    @DisplayName("Has activated ability that forces target creature to attack this turn if able")
    void hasActivatedAbility() {
        HecklingFiends card = new HecklingFiends();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isFalse();
        assertThat(ability.getManaCost()).isEqualTo("{2}{R}");
        assertThat(ability.getEffects()).singleElement().isInstanceOf(MustAttackThisTurnEffect.class);
        MustAttackThisTurnEffect effect = (MustAttackThisTurnEffect) ability.getEffects().getFirst();
        assertThat(effect.forceAttackController()).isFalse();
    }

    @Test
    @DisplayName("Activating ability puts it on the stack targeting a creature")
    void activatingPutsAbilityOnStack() {
        addReadyFiends(player1);
        Permanent target = addReadyCreature(player2);
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Resolving ability marks target creature as must attack without a specific player target")
    void resolvingMarksTargetMustAttack() {
        addReadyFiends(player1);
        Permanent target = addReadyCreature(player2);
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isMustAttackThisTurn()).isTrue();
        assertThat(target.getMustAttackTargetId()).isNull();
    }

    @Test
    @DisplayName("Can target its controller's own creature")
    void canTargetOwnCreature() {
        addReadyFiends(player1);
        Permanent target = addReadyCreature(player1);
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isMustAttackThisTurn()).isTrue();
        assertThat(target.getMustAttackTargetId()).isNull();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addReadyFiends(player1);
        Permanent target = addPermanent(player2, new DarksteelRelic());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not tap to activate")
    void doesNotTapToActivate() {
        Permanent fiends = addReadyFiends(player1);
        Permanent target = addReadyCreature(player2);
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(fiends.isTapped()).isFalse();
    }

    private Permanent addReadyFiends(Player player) {
        return addReadyPermanent(player, new HecklingFiends());
    }

    private Permanent addReadyCreature(Player player) {
        return addReadyPermanent(player, new GrizzlyBears());
    }

    private Permanent addReadyPermanent(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = addPermanent(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private Permanent addPermanent(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
