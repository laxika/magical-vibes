package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WingShards.class, GrizzlyBears.class})
class WingShardsTest extends BaseCardTest {

    @Test
    @DisplayName("Target player sacrifices an attacking creature, not a nonattacking creature")
    void sacrificesOnlyAnAttackingCreature() {
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondAttacker = addCreatureReady(player2, new GrizzlyBears());
        Permanent nonattacker = addCreatureReady(player2, new GrizzlyBears());
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
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Storm copies Wing Shards for each spell cast before it")
    void stormCreatesAdditionalSacrificeChoices() {
        Permanent firstAttacker = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondAttacker = addCreatureReady(player2, new GrizzlyBears());
        gd.recordSpellCast(player1.getId(), new GrizzlyBears());

        castWingShards(player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player2, List.of(firstAttacker.getId()));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    private void castWingShards(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new WingShards()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        declareAttackers(player2, List.of(0, 1));
        harness.castInstant(player1, 0, targetPlayerId);
    }
}
