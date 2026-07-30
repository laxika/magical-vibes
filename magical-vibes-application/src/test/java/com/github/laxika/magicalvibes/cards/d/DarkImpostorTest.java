package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DarkImpostorTest extends BaseCardTest {

    @Test
    @DisplayName("Ability exiles target creature and puts a +1/+1 counter on Dark Impostor")
    void exilesTargetCreatureAndGrowsSelf() {
        Permanent impostor = addImpostorReady(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        payAbilityCost(player1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Grizzly Bears")).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gqs.getEffectivePower(gd, impostor)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, impostor)).isEqualTo(3);
    }

    @Test
    @DisplayName("Exiled creature is tracked as exiled with Dark Impostor")
    void tracksExiledCardWithSource() {
        Permanent impostor = addImpostorReady(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        payAbilityCost(player1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(impostor.getId()))
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("Rejects a non-creature permanent as target")
    void rejectsNonCreatureTarget() {
        addImpostorReady(player1);
        Permanent rod = new Permanent(new RodOfRuin());
        gd.playerBattlefields.get(player2.getId()).add(rod);
        payAbilityCost(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, rod.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Gains activated abilities of creature cards exiled with it")
    void gainsAbilitiesOfExiledCreature() {
        Permanent impostor = addImpostorReady(player1);
        Permanent target = addCreatureReady(player2, new ProdigalSorcerer());
        payAbilityCost(player1);

        assertThat(gqs.computeStaticBonus(gd, impostor).grantedActivatedAbilities()).isEmpty();

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        List<ActivatedAbility> granted = gqs.computeStaticBonus(gd, impostor).grantedActivatedAbilities();
        assertThat(granted).hasSize(1);
        assertThat(granted.getFirst().isRequiresTap()).isTrue();
    }

    @Test
    @DisplayName("Can activate a damage ability gained from an exiled creature card")
    void canActivateGainedAbility() {
        Permanent impostor = addImpostorReady(player1);
        Permanent target = addCreatureReady(player2, new ProdigalSorcerer());
        payAbilityCost(player1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(impostor.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    private Permanent addImpostorReady(Player player) {
        return addCreatureReady(player, new DarkImpostor());
    }

    private void payAbilityCost(Player player) {
        harness.addMana(player, ManaColor.BLACK, 2);
        harness.addMana(player, ManaColor.COLORLESS, 4);
    }
}
