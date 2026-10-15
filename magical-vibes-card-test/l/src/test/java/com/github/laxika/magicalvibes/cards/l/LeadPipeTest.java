package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LeadPipe.class, GrizzlyBears.class, Shock.class})
class LeadPipeTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+0")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent pipe = addPipeReady(player1);
        pipe.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Each opponent loses 1 life when the equipped creature dies")
    void eachOpponentLosesLifeWhenEquippedCreatureDies() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent pipe = addPipeReady(player1);
        pipe.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(pipe.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Sacrificing Lead Pipe draws a card")
    void sacrificingDrawsCard() {
        Permanent pipe = addPipeReady(player1);
        Card drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(pipe);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(pipe.getCard());

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
    }

    @Test
    @DisplayName("Equip resolves for two mana and moves the boost to the new creature")
    void equipMovesBoostToNewCreature() {
        Permanent pipe = addPipeReady(player1);
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, first.getId());
        harness.passBothPriorities();
        assertThat(pipe.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);

        harness.activateAbility(player1, 0, 0, null, second.getId());
        harness.passBothPriorities();
        assertThat(pipe.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
    }

    @Test
    @DisplayName("The Equipment controller's opponent loses life even when they control the equipped creature")
    void opponentControlledEquippedCreatureDies() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent pipe = addPipeReady(player1);
        pipe.setAttachedTo(creature.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(creature.getCard());
    }

    @Test
    @DisplayName("An unequipped creature dying does not cause life loss")
    void unequippedCreatureDeathDoesNotTrigger() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addPipeReady(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, creature.getId());

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature.getCard());
    }

    @Test
    @DisplayName("Sacrificing an attached tapped Pipe draws on the opponent's turn without killing the creature")
    void sacrificingAttachedTappedPipeOnOpponentsTurn() {
        Permanent pipe = addPipeReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        pipe.setAttachedTo(creature.getId());
        pipe.tap();
        Card drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(pipe).contains(creature);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drawnCard);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
    private Permanent addPipeReady(Player player) {
        return addCreatureReady(player, new LeadPipe());
    }
}
