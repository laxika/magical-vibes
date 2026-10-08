package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CandlegroveWitch;
import com.github.laxika.magicalvibes.cards.u.UnrulyMob;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WakeToSlaughter.class, CandlegroveWitch.class, UnrulyMob.class})
class WakeToSlaughterTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent chooses the card for hand and the other returns with haste")
    void opponentChoosesCardForHand() {
        Card handCard = new CandlegroveWitch();
        Card battlefieldCard = new UnrulyMob();
        castWithTargets(handCard, battlefieldCard);

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.cardPool()).containsExactly(handCard, battlefieldCard);

        harness.handleGraveyardCardChosen(player2, 0);

        harness.assertInHand(player1, "Candlegrove Witch");
        harness.assertNotInGraveyard(player1, "Candlegrove Witch");
        harness.assertNotInGraveyard(player1, "Unruly Mob");
        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().equals(battlefieldCard))
                .findFirst()
                .orElseThrow();
        assertThat(returned.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.getDelayedActions(com.github.laxika.magicalvibes.model.action.DelayedPermanentAction.class))
                .anyMatch(action -> action.permanentId().equals(returned.getId()));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Unruly Mob");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.equals(battlefieldCard));
    }

    @Test
    @DisplayName("With one target, it returns to hand without an opponent choice")
    void oneTargetReturnsToHand() {
        Card creature = new CandlegroveWitch();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new WakeToSlaughter()));
        addMana();

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player1, "Candlegrove Witch");
        harness.assertNotOnBattlefield(player1, "Candlegrove Witch");
    }

    @Test
    @DisplayName("The opponent may choose the second target for hand")
    void opponentCanChooseSecondTarget() {
        Card first = new CandlegroveWitch();
        Card second = new UnrulyMob();
        castWithTargets(first, second);

        harness.handleGraveyardCardChosen(player2, 1);

        harness.assertInHand(player1, "Unruly Mob");
        harness.assertOnBattlefield(player1, "Candlegrove Witch");
        harness.assertNotOnBattlefield(player2, "Candlegrove Witch");
        Permanent returned = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(returned.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Choosing zero targets leaves the graveyard creatures untouched")
    void zeroTargets() {
        Card creature = new CandlegroveWitch();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new WakeToSlaughter()));
        addMana();

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Candlegrove Witch");
        harness.assertInGraveyard(player1, "Wake to Slaughter");
        harness.assertNotOnBattlefield(player1, "Candlegrove Witch");
        harness.assertNotInHand(player1, "Candlegrove Witch");
    }

    @Test
    @DisplayName("When one of two targets leaves the graveyard, the survivor returns to hand")
    void onlyRemainingLegalTargetReturnsToHand() {
        Card survivor = new CandlegroveWitch();
        Card removed = new UnrulyMob();
        harness.setGraveyard(player1, List.of(survivor, removed));
        harness.setHand(player1, List.of(new WakeToSlaughter()));
        addMana();
        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(survivor.getId(), removed.getId()));

        harness.setGraveyard(player1, List.of(survivor));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player1, "Candlegrove Witch");
        harness.assertNotOnBattlefield(player1, "Candlegrove Witch");
        harness.assertNotOnBattlefield(player1, "Unruly Mob");
    }

    @Test
    @DisplayName("Flashback performs the opponent choice and exiles the spell")
    void flashbackReturnsCreaturesAndExilesSpell() {
        Card spell = new WakeToSlaughter();
        Card handCard = new CandlegroveWitch();
        Card battlefieldCard = new UnrulyMob();
        harness.setGraveyard(player1, List.of(spell, handCard, battlefieldCard));
        addMana();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFlashback(player1, 0, List.of(handCard.getId(), battlefieldCard.getId()));
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player2, 0);

        harness.assertInHand(player1, "Candlegrove Witch");
        harness.assertOnBattlefield(player1, "Unruly Mob");
        harness.assertNotInGraveyard(player1, "Wake to Slaughter");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().hasKeyword(Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Unruly Mob");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(battlefieldCard);
    }

    @Test
    @DisplayName("Only creature cards in the controller's graveyard are offered as targets")
    void targetPoolExcludesNoncreaturesAndOpponentsCards() {
        Card creature = new CandlegroveWitch();
        Card noncreature = new WakeToSlaughter();
        harness.setGraveyard(player1, List.of(creature, noncreature));
        harness.setGraveyard(player2, List.of(new UnrulyMob()));
        harness.setHand(player1, List.of(new WakeToSlaughter()));
        addMana();

        harness.castSorcery(player1, 0, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.cards()).containsExactly(creature);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Candlegrove Witch");
        harness.assertInGraveyard(player2, "Unruly Mob");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(noncreature);
    }

    @Test
    @DisplayName("When all targets leave the graveyard, no creature is returned")
    void allTargetsIllegal() {
        Card first = new CandlegroveWitch();
        Card second = new UnrulyMob();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new WakeToSlaughter()));
        addMana();
        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));

        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotInHand(player1, "Candlegrove Witch");
        harness.assertNotInHand(player1, "Unruly Mob");
        harness.assertNotOnBattlefield(player1, "Candlegrove Witch");
        harness.assertNotOnBattlefield(player1, "Unruly Mob");
        harness.assertInGraveyard(player1, "Wake to Slaughter");
    }

    private void castWithTargets(Card first, Card second) {
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new WakeToSlaughter()));
        addMana();

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
