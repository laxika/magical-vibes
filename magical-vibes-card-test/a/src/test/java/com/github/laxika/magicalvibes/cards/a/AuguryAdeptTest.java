package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SafeholdElite;
import com.github.laxika.magicalvibes.cards.w.WiltLeafCavaliers;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AuguryAdept.class, Forest.class, SafeholdElite.class, WiltLeafCavaliers.class})
class AuguryAdeptTest extends BaseCardTest {

    private Permanent addAttackingAdept() {
        Permanent perm = addCreatureReady(player1, new AuguryAdept());
        perm.setAttacking(true);
        return perm;
    }

    private void resolveCombatAndTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // through combat damage
        harness.passBothPriorities(); // resolve the triggered ability
    }

    @Test
    @DisplayName("Combat damage to a player reveals top card into hand and gains life equal to its mana value")
    void revealsAndGainsLife() {
        addAttackingAdept();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Card topCard = new SafeholdElite(); // MV 2
        gd.playerDecks.get(player1.getId()).addFirst(topCard);

        resolveCombatAndTrigger();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18); // took 2 combat damage
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(topCard.getId()));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22); // gained MV 2 life
    }

    @Test
    @DisplayName("Revealing a land (mana value 0) still puts it into hand but gains no life")
    void revealingLandGainsNoLife() {
        addAttackingAdept();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Card topCard = new Forest(); // MV 0
        gd.playerDecks.get(player1.getId()).addFirst(topCard);

        resolveCombatAndTrigger();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(topCard.getId()));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not trigger when blocked and dealing no combat damage to a player")
    void noTriggerWhenBlocked() {
        addAttackingAdept();
        harness.setLife(player1, 20);
        Card topCard = new SafeholdElite();
        gd.playerDecks.get(player1.getId()).addFirst(topCard);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        // Wilt-Leaf Cavaliers (3/4) blocks the 2/2 Adept — no damage reaches player2.
        Permanent blocker = addCreatureReady(player2, new WiltLeafCavaliers());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombatAndTrigger();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(topCard.getId()));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Hybrid mana symbols count once each and only the top card is revealed")
    void revealsOnlyTopCardAndGainsHybridManaValue() {
        addAttackingAdept();
        harness.setLife(player1, 20);
        Card topCard = new AuguryAdept();
        Card nextCard = new Forest();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.setHand(player1, List.of());

        resolveCombatAndTrigger();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
        harness.assertLife(player1, 23);
        assertThat(gameLogContains("reveals Augury Adept")).isTrue();
    }

    @Test
    @DisplayName("An empty library does not cause a failed draw or life gain")
    void emptyLibraryDoesNothing() {
        addAttackingAdept();
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of());

        resolveCombatAndTrigger();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 20);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(com.github.laxika.magicalvibes.model.GameStatus.FINISHED);
    }

    @Test
    @DisplayName("The trigger resolves even after Augury Adept leaves the battlefield")
    void triggerResolvesWithoutSource() {
        Permanent adept = addAttackingAdept();
        harness.setLife(player1, 20);
        Card topCard = new AuguryAdept();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, () -> harness.passBothPriorities());
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(adept);
        gd.playerGraveyards.get(player1.getId()).add(adept.getCard());

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("The card revealed is the top card at resolution, not at combat damage")
    void usesLibraryAtResolution() {
        addAttackingAdept();
        harness.setLife(player1, 20);
        Card originalTop = new Forest();
        Card newTop = new AuguryAdept();
        harness.setLibrary(player1, List.of(originalTop));
        harness.setHand(player1, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, () -> harness.passBothPriorities());
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 20);
        harness.setLibrary(player1, List.of(newTop, originalTop));

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(newTop);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(originalTop);
        harness.assertLife(player1, 23);
    }
}
