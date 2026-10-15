package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CabarettiInitiate;
import com.github.laxika.magicalvibes.cards.m.MaskedBandits;
import com.github.laxika.magicalvibes.cards.s.SlipOutTheBack;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DealGoneBad.class, MaskedBandits.class, CabarettiInitiate.class, SlipOutTheBack.class})
class DealGoneBadTest extends BaseCardTest {

    @Test
    @DisplayName("gives a creature -3/-3 and mills three cards from a target player's library")
    void resolvesBothEffects() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new MaskedBandits());
        int librarySize = gd.playerDecks.get(player2.getId()).size();

        cast(bear.getId(), player2.getId());

        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySize - 3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("the -3/-3 effect expires at cleanup")
    void debuffExpiresAtCleanup() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new MaskedBandits());

        cast(bear.getId(), player2.getId());

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getEffectivePower()).isEqualTo(5);
        assertThat(bear.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    void requiresCreatureAndPlayerTarget() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new MaskedBandits());
        harness.setHand(player1, List.of(new DealGoneBad()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(bear.getId(), bear.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canTargetOwnCreatureAndMillSelf() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MaskedBandits());
        Card first = new CabarettiInitiate();
        Card second = new MaskedBandits();
        Card third = new DealGoneBad();
        Card fourth = new CabarettiInitiate();
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        int opponentLibrarySize = gd.playerDecks.get(player2.getId()).size();

        cast(creature.getId(), player1.getId());

        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourth);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second, third).hasSize(4);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opponentLibrarySize);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void millsOnlyAvailableCardsFromShortLibrary() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MaskedBandits());
        Card first = new CabarettiInitiate();
        Card second = new DealGoneBad();
        harness.setLibrary(player2, List.of(first, second));

        cast(creature.getId(), player2.getId());

        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(first, second);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void emptyLibraryDoesNotPreventCreatureDebuff() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new MaskedBandits());
        harness.setLibrary(player2, List.of());

        cast(creature.getId(), player2.getId());

        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void creatureDiesFromZeroToughnessAndPlayerStillMills() {
        CabarettiInitiate creatureCard = new CabarettiInitiate();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, creatureCard);
        Card first = new DealGoneBad();
        Card second = new MaskedBandits();
        Card third = new CabarettiInitiate();
        harness.setLibrary(player2, List.of(first, second, third));

        cast(creature.getId(), player2.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(first, second, third, creatureCard).hasSize(4);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void stillMillsWhenCreatureTargetLeavesBeforeResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new MaskedBandits());
        int librarySize = gd.playerDecks.get(player2.getId()).size();
        harness.setHand(player1, List.of(new DealGoneBad()));
        addMana();
        harness.castInstant(player1, 0, List.of(creature.getId(), player2.getId()));

        harness.getPermanentRemovalService().removePermanentToHand(gd, creature);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySize - 3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player2.getId())).contains(creature.getCard());
    }

    @Test
    void phasedOutCreatureIsUnaffectedButPlayerStillMills() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new MaskedBandits());
        int librarySize = gd.playerDecks.get(player2.getId()).size();
        harness.setHand(player1, List.of(new DealGoneBad()));
        addMana();
        harness.castInstant(player1, 0, List.of(creature.getId(), player2.getId()));
        harness.setHand(player2, List.of(new SlipOutTheBack()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySize - 3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
    }

    private void cast(java.util.UUID creatureTargetId, java.util.UUID playerTargetId) {
        harness.setHand(player1, List.of(new DealGoneBad()));
        addMana();
        harness.castAndResolveInstant(player1, 0, List.of(creatureTargetId, playerTargetId));
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
