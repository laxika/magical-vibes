package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Aetherplasm.class, GrizzlyBears.class})
class AetherplasmTest extends BaseCardTest {

    @Test
    @DisplayName("Declining the first may choice leaves Aetherplasm blocking")
    void decliningReturnKeepsAetherplasmBlocking() {
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        Permanent aetherplasm = addReadyCreature(player2, new Aetherplasm());

        declareBlock(attacker, aetherplasm);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(aetherplasm);
        assertThat(aetherplasm.isBlocking()).isTrue();
        assertThat(aetherplasm.getBlockingTargetIds()).containsExactly(attacker.getId());
    }

    @Test
    @DisplayName("Returning Aetherplasm allows declining the replacement creature")
    void returningAetherplasmCanDeclineReplacement() {
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        Permanent aetherplasm = addReadyCreature(player2, new Aetherplasm());
        Card replacementCard = new GrizzlyBears();
        harness.setHand(player2, List.of(replacementCard));

        declareBlock(attacker, aetherplasm);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        harness.handleCardChosen(player2, -1);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(aetherplasm);
        assertThat(gd.playerHands.get(player2.getId())).containsExactlyInAnyOrder(aetherplasm.getCard(), replacementCard);
        assertThat(attacker.isBlockedWithoutBlockers()).isTrue();
    }

    @Test
    @DisplayName("A creature chosen from hand enters blocking the same attacker")
    void replacementCreatureEntersBlocking() {
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        Permanent aetherplasm = addReadyCreature(player2, new Aetherplasm());
        Card replacementCard = new GrizzlyBears();
        harness.setHand(player2, List.of(replacementCard));

        declareBlock(attacker, aetherplasm);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        Permanent replacement = findPermanent(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(aetherplasm);
        assertThat(replacement).isNotNull();
        assertThat(replacement.isBlocking()).isTrue();
        assertThat(replacement.getBlockingTargetIds()).containsExactly(attacker.getId());
        assertThat(attacker.isBlockedWithoutBlockers()).isFalse();
    }

    private void declareBlock(Permanent attacker, Permanent blocker) {
        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
