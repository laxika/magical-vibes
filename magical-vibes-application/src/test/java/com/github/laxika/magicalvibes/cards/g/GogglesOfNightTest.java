package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GogglesOfNight.class, GrizzlyBears.class})
class GogglesOfNightTest extends BaseCardTest {

    @Test
    @DisplayName("Equip attaches Goggles of Night to a creature you control")
    void equipAttachesToCreatureYouControl() {
        Permanent goggles = addGogglesReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(goggles.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Combat damage from the equipped creature scries, then draws a card")
    void combatDamageScriesThenDraws() {
        Card scryedCard = new GrizzlyBears();
        Card drawnCard = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(scryedCard, drawnCard));

        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent goggles = addGogglesReady(player1);
        goggles.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(scryedCard);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(scryedCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Goggles of Night do not trigger when the equipped creature is blocked")
    void blockedCreatureDoesNotTrigger() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent goggles = addGogglesReady(player1);
        goggles.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    private Permanent addGogglesReady(Player player) {
        Permanent goggles = new Permanent(new GogglesOfNight());
        goggles.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(goggles);
        return goggles;
    }
}
