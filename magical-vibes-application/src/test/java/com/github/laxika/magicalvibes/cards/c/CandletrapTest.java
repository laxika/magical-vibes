package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Candletrap.class, CrawWurm.class, GrizzlyBears.class, HillGiant.class, LightningBolt.class})
class CandletrapTest extends BaseCardTest {

    @Test
    @DisplayName("Candletrap gives the enchanted creature defender and prevents its combat damage")
    void defenderAndCombatDamagePrevention() {
        Permanent enchanted = addCreatureReady(player1, new HillGiant());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attachCandletrap(player1, enchanted);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, enchanted, Keyword.DEFENDER)).isTrue();
        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(enchanted.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Candletrap does not prevent noncombat damage from the enchanted creature")
    void noncombatDamageStillApplies() {
        Permanent enchanted = addCreatureReady(player1, new CrawWurm());
        attachCandletrap(player1, enchanted);

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, enchanted.getId());
        harness.passBothPriorities();

        assertThat(enchanted.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Coven sacrifice exiles the enchanted creature")
    void covenSacrificeExilesEnchantedCreature() {
        Permanent enchanted = addCreatureReady(player2, new GrizzlyBears());
        Permanent aura = attachCandletrap(player1, enchanted);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player1, new CrawWurm());

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(enchanted);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(enchanted.getCard());
        harness.assertInGraveyard(player1, "Candletrap");
    }

    @Test
    @DisplayName("Coven ability cannot be activated without three different powers")
    void covenRequiresDifferentPowers() {
        Permanent enchanted = addCreatureReady(player2, new GrizzlyBears());
        Permanent aura = attachCandletrap(player1, enchanted);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("different powers");
    }

    private Permanent attachCandletrap(Player controller, Permanent creature) {
        Permanent aura = new Permanent(new Candletrap());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
        return aura;
    }
}
