package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SistersOfStoneDeath.class, GrizzlyBears.class})
class SistersOfStoneDeathTest extends BaseCardTest {

    @Test
    @DisplayName("The green ability forces a target creature to block Sisters of Stone Death")
    void greenAbilityForcesTargetCreatureToBlock() {
        Permanent sisters = addCreatureReady(player1, new SistersOfStoneDeath());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.getMustBlockIds()).contains(sisters.getId());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }

    @Test
    @DisplayName("The black-green ability exiles a creature blocking or blocked by Sisters of Stone Death")
    void blackGreenAbilityExilesCombatCreature() {
        Permanent sisters = addCreatureReady(player1, new SistersOfStoneDeath());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
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
    @DisplayName("The exile ability cannot target a creature outside combat with Sisters of Stone Death")
    void blackGreenAbilityRejectsCreatureOutsideCombat() {
        Permanent sisters = addCreatureReady(player1, new SistersOfStoneDeath());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent bystander = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
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
        Card exiledCreature = new GrizzlyBears();
        gd.addToExile(player2.getId(), exiledCreature, sisters.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(exiledCreature.getId()));
        assertThat(gd.getCardsExiledByPermanent(sisters.getId())).isEmpty();
    }
}
