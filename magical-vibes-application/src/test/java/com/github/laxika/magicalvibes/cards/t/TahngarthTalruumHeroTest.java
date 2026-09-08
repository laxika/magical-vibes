package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TahngarthTalruumHeroTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to its power and receives damage equal to the target's power")
    void dealsReciprocalPowerDamage() {
        Permanent tahngarth = addReadyTahngarth(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        addAbilityMana();

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(tahngarth.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Both creatures are destroyed when reciprocal damage is lethal")
    void bothCreaturesDieFromReciprocalDamage() {
        Permanent tahngarth = addReadyTahngarth(player1);
        Permanent target = addReadyCreature(player2, new CrawWurm());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(tahngarth);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addReadyTahngarth(player1);
        harness.addToBattlefield(player2, new Forest());
        addAbilityMana();

        UUID forestId = harness.getPermanentId(player2, "Forest");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forestId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate the tap ability while tapped")
    void cannotActivateWhileTapped() {
        addReadyTahngarth(player1);
        Permanent target = addReadyCreature(player2, new GrizzlyBears());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        Permanent secondTarget = addReadyCreature(player2, new GrizzlyBears());
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, secondTarget.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyTahngarth(Player player) {
        return addReadyCreature(player, new TahngarthTalruumHero());
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.RED, 2);
    }
}
