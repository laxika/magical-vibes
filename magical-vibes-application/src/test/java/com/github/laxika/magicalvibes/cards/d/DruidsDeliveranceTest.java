package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DruidsDeliveranceTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage that would be dealt to the caster is prevented")
    void preventsCombatDamageToController() {
        harness.forceActivePlayer(player1);
        addAttacker(player1, player2);
        harness.setHand(player2, List.of(new DruidsDeliverance()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        int lifeBefore = gd.getLife(player2.getId());

        harness.castInstant(player2, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Populate copies the only creature token its controller controls")
    void populateCopiesTheOnlyCreatureToken() {
        harness.addToBattlefield(player2, soldierToken());
        harness.setHand(player2, List.of(new DruidsDeliverance()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(countOf(player2, "Soldier Token")).isEqualTo(2);
    }

    @Test
    @DisplayName("Populate does nothing without a creature token")
    void populateDoesNothingWithoutACreatureToken() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new DruidsDeliverance()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(countOf(player2, "Grizzly Bears")).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
    }

    private Permanent addAttacker(Player attackerController, Player defender) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        perm.setAttacking(true);
        perm.setAttackTarget(defender.getId());
        gd.playerBattlefields.get(attackerController.getId()).add(perm);
        return perm;
    }

    private long countOf(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> name.equals(p.getCard().getName()))
                .count();
    }

    private static Card soldierToken() {
        Card card = new Card();
        card.setName("Soldier Token");
        card.setType(CardType.CREATURE);
        card.setManaCost("");
        card.setColor(CardColor.WHITE);
        card.setPower(1);
        card.setToughness(1);
        card.setToken(true);
        return card;
    }
}
