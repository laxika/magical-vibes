package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Putrefy;
import com.github.laxika.magicalvibes.cards.w.Watchwolf;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SistersOfStoneDeath.class, Watchwolf.class, Forest.class, Putrefy.class})
class SistersOfStoneDeathTest extends BaseCardTest {

    @Test
    @DisplayName("The green ability forces a target creature to block Sisters of Stone Death")
    void greenAbilityForcesTargetCreatureToBlock() {
        Permanent sisters = addCreatureReady(player1, new SistersOfStoneDeath());
        Permanent blocker = addCreatureReady(player2, new Watchwolf());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.getMustBlockIds()).contains(sisters.getId());

        declareAttackersAndPrepareBlockers(List.of(0));
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }

    @Test
    @DisplayName("The green ability cannot target a noncreature permanent")
    void greenAbilityRejectsNoncreaturePermanent() {
        addCreatureReady(player1, new SistersOfStoneDeath());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The black-green ability exiles a creature blocking or blocked by Sisters of Stone Death")
    void blackGreenAbilityExilesCombatCreature() {
        Permanent sisters = addCreatureReady(player1, new SistersOfStoneDeath());
        Permanent blocker = addCreatureReady(player2, new Watchwolf());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, 1, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(sisters.getId()))
                .containsExactly(blocker.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    @DisplayName("The black-green ability exiles a creature blocked by Sisters of Stone Death")
    void blackGreenAbilityExilesCreatureWhenSistersBlocks() {
        Permanent sisters = addCreatureReady(player1, new SistersOfStoneDeath());
        Permanent attacker = addCreatureReady(player2, new Watchwolf());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, 1, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(sisters.getId()))
                .containsExactly(attacker.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(attacker);
    }

    @Test
    @DisplayName("The exile ability cannot target a creature outside combat with Sisters of Stone Death")
    void blackGreenAbilityRejectsCreatureOutsideCombat() {
        Permanent sisters = addCreatureReady(player1, new SistersOfStoneDeath());
        Permanent blocker = addCreatureReady(player2, new Watchwolf());
        Permanent bystander = addCreatureReady(player2, new Watchwolf());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bystander.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker, bystander);
        assertThat(gd.getCardsExiledByPermanent(sisters.getId())).isEmpty();
    }

    @Test
    @DisplayName("The black ability returns a creature card exiled with Sisters of Stone Death under your control")
    void blackAbilityReturnsExiledCreature() {
        Permanent sisters = addCreatureReady(player1, new SistersOfStoneDeath());
        Card exiledCreature = new Watchwolf();
        gd.addToExile(player2.getId(), exiledCreature, sisters.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(exiledCreature.getId()));
        assertThat(gd.getCardsExiledByPermanent(sisters.getId())).isEmpty();
    }

    @Test
    @DisplayName("The black ability does not return a noncreature card exiled with Sisters of Stone Death")
    void blackAbilityDoesNotReturnNoncreatureCard() {
        Permanent sisters = addCreatureReady(player1, new SistersOfStoneDeath());
        Card exiledLand = new Forest();
        gd.addToExile(player2.getId(), exiledLand, sisters.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(exiledLand.getId()));
        assertThat(gd.getCardsExiledByPermanent(sisters.getId()))
                .containsExactly(exiledLand);
    }

    @Test
    void tappedCreatureIsNotRequiredToBlock() {
        addCreatureReady(player1, new SistersOfStoneDeath());
        Permanent blocker = addCreatureReady(player2, new Watchwolf());
        blocker.tap();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
    }

    @Test
    void returnAbilityChoosesExactlyOneOfSeveralExiledCreatures() {
        Permanent sisters = addCreatureReady(player1, new SistersOfStoneDeath());
        Card first = new Watchwolf();
        Card chosen = new Watchwolf();
        Card land = new Forest();
        gd.addToExile(player2.getId(), first, sisters.getId());
        gd.addToExile(player2.getId(), chosen, sisters.getId());
        gd.addToExile(player2.getId(), land, sisters.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(chosen.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(chosen.getId()));
        assertThat(gd.getCardsExiledByPermanent(sisters.getId())).containsExactly(first, land);
    }

    @Test
    void returnAbilityCannotReturnCardsExiledWithAnotherSisters() {
        addCreatureReady(player1, new SistersOfStoneDeath());
        Permanent otherSisters = addCreatureReady(player2, new SistersOfStoneDeath());
        Card exiled = new Watchwolf();
        gd.addToExile(player1.getId(), exiled, otherSisters.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(otherSisters.getId())).containsExactly(exiled);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(exiled.getId()));
    }

    @Test
    void exileAbilityDoesNotResolveWhenSistersLeavesCombatBeforeResolution() {
        Permanent sisters = addCreatureReady(player1, new SistersOfStoneDeath());
        Permanent blocker = addCreatureReady(player2, new Watchwolf());
        harness.setHand(player2, List.of(new Putrefy()));
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, 1, null, blocker.getId());

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castInstant(player2, 0, sisters.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sisters of Stone Death");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(gd.getCardsExiledByPermanent(sisters.getId())).isEmpty();
    }
}
