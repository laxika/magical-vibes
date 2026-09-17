package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.s.SilverKnight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WingShards.class, SilverKnight.class})
class WingShardsTest extends BaseCardTest {

    @Test
    @DisplayName("Target player sacrifices an attacking creature, not a nonattacking creature")
    void sacrificesOnlyAnAttackingCreature() {
        Permanent attacker = addCreatureReady(player2, new SilverKnight());
        Permanent secondAttacker = addCreatureReady(player2, new SilverKnight());
        Permanent nonattacker = addCreatureReady(player2, new SilverKnight());
        castWingShards(player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(attacker.getId(), secondAttacker.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(attacker.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .containsExactly(secondAttacker, nonattacker);
        harness.assertInGraveyard(player2, "Silver Knight");
    }

    @Test
    @DisplayName("Storm copies Wing Shards for each spell cast before it")
    void stormCreatesAdditionalSacrificeChoices() {
        Permanent firstAttacker = addCreatureReady(player2, new SilverKnight());
        Permanent secondAttacker = addCreatureReady(player2, new SilverKnight());
        gd.recordSpellCast(player1.getId(), new SilverKnight());

        castWingShards(player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player2, List.of(firstAttacker.getId()));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does nothing when the target player controls no attacking creatures")
    void doesNothingWithoutAnAttackingCreature() {
        Permanent nonattacker = addCreatureReady(player2, new SilverKnight());
        castWingShards(player2.getId(), player2, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(nonattacker);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Wing Shards");
    }

    @Test
    @DisplayName("A Storm copy may be retargeted to another player")
    void stormCopyMayChooseNewTarget() {
        addCreatureReady(player1, new SilverKnight());
        Permanent nonattacker = addCreatureReady(player2, new SilverKnight());
        gd.recordSpellCast(player1.getId(), new SilverKnight());

        castWingShards(player2.getId(), player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(nonattacker);
        harness.assertInGraveyard(player1, "Silver Knight");
    }

    private void castWingShards(UUID targetPlayerId) {
        castWingShards(targetPlayerId, player2, List.of(0, 1));
    }

    private void castWingShards(UUID targetPlayerId, Player attackingPlayer, List<Integer> attackerIndices) {
        harness.setHand(player1, List.of(new WingShards()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        declareAttackers(attackingPlayer, attackerIndices);
        harness.castInstant(player1, 0, targetPlayerId);
    }
}
