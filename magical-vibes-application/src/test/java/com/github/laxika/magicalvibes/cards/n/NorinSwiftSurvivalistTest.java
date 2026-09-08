package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NorinSwiftSurvivalist.class, GrizzlyBears.class})
class NorinSwiftSurvivalistTest extends BaseCardTest {

    @Test
    @DisplayName("Norin cannot block")
    void cannotBlock() {
        Permanent norin = addReadyCreature(player1, new NorinSwiftSurvivalist());

        assertThat(bls.canBlock(gd, norin)).isFalse();
    }

    @Test
    @DisplayName("A blocked creature may be exiled and played this turn")
    void exilesBlockedCreatureAndAllowsItToBePlayed() {
        harness.addToBattlefield(player1, new NorinSwiftSurvivalist());
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
        UUID attackerCardId = attacker.getOriginalCard().getId();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(card -> card.getId().equals(attackerCardId));
        assertThat(gd.exilePlayPermissions).containsEntry(attackerCardId, player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(attackerCardId);
        assertThat(blocker.getMarkedDamage()).isZero();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, attackerCardId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getOriginalCard().getId().equals(attackerCardId));
    }

    @Test
    @DisplayName("Declining Norin's ability leaves the blocked creature in combat")
    void decliningDoesNotExileBlockedCreature() {
        harness.addToBattlefield(player1, new NorinSwiftSurvivalist());
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        addReadyCreature(player2, new GrizzlyBears());
        UUID attackerCardId = attacker.getOriginalCard().getId();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).noneMatch(card -> card.getId().equals(attackerCardId));
        assertThat(gd.exilePlayPermissions).doesNotContainKey(attackerCardId);
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
