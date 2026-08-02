package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RoguesGlovesTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the may draws a card when equipped creature deals combat damage to a player")
    void acceptingDrawsCard() {
        equipAttackingBears(player1);
        prepareLibraryAndHand();

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Declining the may draws nothing")
    void decliningDrawsNothing() {
        equipAttackingBears(player1);
        prepareLibraryAndHand();

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("No trigger when the equipped creature is blocked and deals no damage to a player")
    void noTriggerWhenBlocked() {
        equipAttackingBears(player1);
        prepareLibraryAndHand();

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        TestCards.mutableCard(blocker).setToughness(10);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("No trigger when an unequipped creature deals combat damage")
    void noTriggerWhenUnequipped() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.setAttacking(true);
        addGlovesReady(player1);
        prepareLibraryAndHand();

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void equipAttackingBears(Player player) {
        Permanent creature = addCreatureReady(player, new GrizzlyBears());
        creature.setAttacking(true);
        addGlovesReady(player).setAttachedTo(creature.getId());
    }

    private Permanent addGlovesReady(Player player) {
        Permanent perm = new Permanent(new RoguesGloves());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void prepareLibraryAndHand() {
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest(), new Forest())));
        harness.setHand(player1, new ArrayList<>());
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
