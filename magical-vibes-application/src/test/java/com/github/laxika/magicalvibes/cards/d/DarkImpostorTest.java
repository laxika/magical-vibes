package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MarchOfTheMachines;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DarkImpostor.class, GrizzlyBears.class, ProdigalSorcerer.class,
        RodOfRuin.class, MarchOfTheMachines.class})
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
        Permanent rod = harness.addToBattlefieldAndReturn(player2, new RodOfRuin());
        payAbilityCost(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, rod.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Gains activated abilities of creature cards exiled with it")
    void gainsAbilitiesOfExiledCreature() {
        addImpostorReady(player1);
        Permanent target = addCreatureReady(player2, new ProdigalSorcerer());
        payAbilityCost(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
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

    @Test
    @DisplayName("An animated artifact is exiled but grants no activated abilities")
    void doesNotGainAbilitiesOfExiledNoncreatureCard() {
        Permanent impostor = addImpostorReady(player1);
        harness.addToBattlefield(player2, new MarchOfTheMachines());
        Permanent rod = harness.addToBattlefieldAndReturn(player2, new RodOfRuin());
        payAbilityCost(player1);

        harness.activateAbility(player1, 0, 0, null, rod.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName).contains("Rod of Ruin");
        assertThat(gqs.getEffectivePower(gd, impostor)).isEqualTo(3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cards exiled by a gained ability are not linked to the printed ability")
    void doesNotGainAbilitiesFromCardsExiledByGainedExileAbility() {
        Permanent impostor = addImpostorReady(player1);
        Permanent otherImpostor = addImpostorReady(player2);
        Permanent sorcerer = addCreatureReady(player2, new ProdigalSorcerer());
        payAbilityCost(player1);
        harness.activateAbility(player1, 0, 0, null, otherImpostor.getId());
        harness.passBothPriorities();

        payAbilityCost(player1);
        harness.activateAbility(player1, 0, 1, null, sorcerer.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName).contains("Dark Impostor", "Prodigal Sorcerer");
        assertThat(gqs.getEffectivePower(gd, impostor)).isEqualTo(4);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An ability with an illegal target on resolution does not add a counter")
    void illegalTargetOnResolutionDoesNotAddCounter() {
        Permanent impostor = addImpostorReady(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        payAbilityCost(player1);
        payAbilityCost(player1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName).containsExactly("Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, impostor)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, impostor)).isEqualTo(3);
    }

    @Test
    @DisplayName("Dark Impostor may exile itself")
    void canExileItself() {
        Permanent impostor = addImpostorReady(player1);
        payAbilityCost(player1);

        harness.activateAbility(player1, 0, 0, null, impostor.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Dark Impostor")).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getName).containsExactly("Dark Impostor");
    }

    private Permanent addImpostorReady(Player player) {
        return addCreatureReady(player, new DarkImpostor());
    }

    private void payAbilityCost(Player player) {
        harness.addMana(player, ManaColor.BLACK, 2);
        harness.addMana(player, ManaColor.COLORLESS, 4);
    }
}
