package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.h.Hivestone;
import com.github.laxika.magicalvibes.cards.p.PsionicSliver;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LooterIlKor.class, AshcoatBear.class, Hivestone.class, PsionicSliver.class})
class LooterIlKorTest extends BaseCardTest {

    @Test
    @DisplayName("Draws and then discards when it deals combat damage to an opponent")
    void drawsThenDiscardsAfterCombatDamage() {
        Permanent looter = addCreatureReady(player1, new LooterIlKor());
        AshcoatBear handCard = new AshcoatBear();
        PsionicSliver drawnCard = new PsionicSliver();
        harness.setHand(player1, List.of(handCard));
        harness.setLibrary(player1, List.of(drawnCard));

        looter.setAttacking(true);
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(handCard);
    }

    @Test
    @DisplayName("Triggers on noncombat damage to an opponent")
    void triggersOnNoncombatDamage() {
        addCreatureReady(player1, new LooterIlKor());
        addCreatureReady(player1, new Hivestone());
        addCreatureReady(player1, new PsionicSliver());
        harness.setHand(player1, List.of(new AshcoatBear()));
        harness.setLibrary(player1, List.of(new AshcoatBear()));

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Shadow prevents a non-shadow creature from blocking Looter il-Kor")
    void cannotBeBlockedByNonShadowCreature() {
        addCreatureReady(player1, new LooterIlKor());
        addCreatureReady(player2, new AshcoatBear());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Shadow prevents Looter il-Kor from blocking a non-shadow creature")
    void cannotBlockNonShadowCreature() {
        addCreatureReady(player1, new AshcoatBear());
        addCreatureReady(player2, new LooterIlKor());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not trigger when it deals damage to its controller")
    void doesNotTriggerWhenItDamagesItsController() {
        addCreatureReady(player1, new LooterIlKor());
        addCreatureReady(player1, new Hivestone());
        addCreatureReady(player1, new PsionicSliver());
        harness.setHand(player1, List.of(new AshcoatBear()));

        harness.activateAbility(player1, 0, 0, null, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Looter il-Kor");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("May discard the card just drawn")
    void canDiscardTheDrawnCard() {
        Permanent looter = addCreatureReady(player1, new LooterIlKor());
        AshcoatBear handCard = new AshcoatBear();
        PsionicSliver drawnCard = new PsionicSliver();
        harness.setHand(player1, List.of(handCard));
        harness.setLibrary(player1, List.of(drawnCard));

        looter.setAttacking(true);
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(handCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("With an empty hand, must discard the card drawn")
    void discardsDrawnCardWithEmptyHand() {
        Permanent looter = addCreatureReady(player1, new LooterIlKor());
        AshcoatBear drawnCard = new AshcoatBear();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));

        looter.setAttacking(true);
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Can block another shadow creature without triggering from creature damage")
    void canBlockShadowCreatureWithoutLooting() {
        addCreatureReady(player1, new LooterIlKor());
        addCreatureReady(player2, new LooterIlKor());
        AshcoatBear handCard = new AshcoatBear();
        harness.setHand(player1, List.of(handCard));
        harness.setHand(player2, List.of());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Looter il-Kor");
        harness.assertInGraveyard(player2, "Looter il-Kor");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(handCard);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
