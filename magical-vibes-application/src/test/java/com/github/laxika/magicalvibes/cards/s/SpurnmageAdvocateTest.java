package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AvenWarcraft;
import com.github.laxika.magicalvibes.cards.b.BattlewiseAven;
import com.github.laxika.magicalvibes.cards.g.GiantWarthog;
import com.github.laxika.magicalvibes.cards.g.GuidedStrike;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AvenWarcraft.class, BattlewiseAven.class, GiantWarthog.class, GuidedStrike.class, SpurnmageAdvocate.class})
class SpurnmageAdvocateTest extends BaseCardTest {

    @Test
    @DisplayName("Returns two cards from an opponent's graveyard and destroys an attacking creature")
    void returnsCardsAndDestroysAttacker() {
        Permanent advocate = addReadyAdvocate();
        Card first = new GiantWarthog();
        Card second = new GiantWarthog();
        Permanent attacker = addAttacker();
        harness.setGraveyard(player2, List.of(first, second));

        harness.activateAbilityWithMultiTargets(player1, index(advocate), 0,
                List.of(first.getId(), second.getId(), attacker.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getId)
                .contains(first.getId(), second.getId());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(attacker.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(attacker.getCard().getId()));
        assertThat(advocate.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Rejects a card from the controller's graveyard as an opponent target")
    void rejectsCardFromControllersGraveyard() {
        Permanent advocate = addReadyAdvocate();
        Card ownCard = new GiantWarthog();
        Card opponentCard = new GiantWarthog();
        Permanent attacker = addAttacker();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentCard));

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, index(advocate), 0,
                List.of(ownCard.getId(), opponentCard.getId(), attacker.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(advocate.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Requires two distinct cards in an opponent's graveyard")
    void requiresTwoOpponentGraveyardCards() {
        Permanent advocate = addReadyAdvocate();
        Card onlyCard = new GiantWarthog();
        Permanent attacker = addAttacker();
        harness.setGraveyard(player2, List.of(onlyCard));

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, index(advocate), 0,
                List.of(onlyCard.getId(), onlyCard.getId(), attacker.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(advocate.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Resolves the remaining effects when one graveyard target leaves before resolution")
    void resolvesWithOneGraveyardTargetGone() {
        Permanent advocate = addReadyAdvocate();
        Card first = new GiantWarthog();
        Card second = new GiantWarthog();
        Permanent attacker = addAttacker();
        harness.setGraveyard(player2, List.of(first, second));

        harness.activateAbilityWithMultiTargets(player1, index(advocate), 0,
                List.of(first.getId(), second.getId(), attacker.getId()));
        harness.setGraveyard(player2, List.of(second));
        harness.setHand(player2, List.of(first));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getId)
                .contains(first.getId(), second.getId());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(attacker.getId()));
    }

    @Test
    @DisplayName("Rejects a nonattacking creature as the destruction target")
    void rejectsNonattackingCreature() {
        Permanent advocate = addReadyAdvocate();
        Card first = new GiantWarthog();
        Card second = new GiantWarthog();
        Permanent bystander = harness.addToBattlefieldAndReturn(player2, new GiantWarthog());
        harness.setGraveyard(player2, List.of(first, second));

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, index(advocate), 0,
                List.of(first.getId(), second.getId(), bystander.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyAdvocate() {
        return addCreatureReady(player1, new SpurnmageAdvocate());
    }

    private Permanent addAttacker() {
        Permanent attacker = addCreatureReady(player2, new GiantWarthog());
        attacker.setAttacking(true);
        return attacker;
    }

    private int index(Permanent advocate) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(advocate);
    }

    @Test
    @DisplayName("Rejects cards from its controller's graveyard")
    void rejectsCardsFromControllerGraveyard() {
        Permanent advocate = addReadyAdvocate();
        Card first = new AvenWarcraft();
        Card second = new GuidedStrike();
        Permanent attacker = addAttackerForJudReview();
        harness.setGraveyard(player1, List.of(first, second));

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, index(advocate), 0,
                List.of(first.getId(), second.getId(), attacker.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addAttackerForJudReview() {
        Permanent attacker = addCreatureReady(player2, new BattlewiseAven());
        attacker.setAttacking(true);
        return attacker;
    }
}
