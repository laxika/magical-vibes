package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DireFleetInterloper;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FathomFleetCaptain.class, DireFleetInterloper.class})
class FathomFleetCaptainTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with another nontoken Pirate triggers may-pay and creates 2/2 Pirate token with menace")
    void attackWithAnotherPirateCreatesToken() {
        addCreatureReady(player1, new FathomFleetCaptain());
        addPirateCreature(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        declareAttackers(player1, List.of(0));

        // Trigger is on the stack; resolve it to get the may-pay prompt
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        // Accept the may-pay
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        // Should have a 2/2 black Pirate token with menace
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Pirate")
                        && p.getCard().isToken()
                        && p.getCard().getPower() == 2
                        && p.getCard().getToughness() == 2
                        && p.getCard().getColor() == CardColor.BLACK
                        && p.getCard().getSubtypes().contains(CardSubtype.PIRATE)
                        && p.getCard().getKeywords().contains(Keyword.MENACE));
    }

    @Test
    @DisplayName("Declining may-pay does not create token")
    void declineDoesNotCreateToken() {
        addCreatureReady(player1, new FathomFleetCaptain());
        addPirateCreature(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        declareAttackers(player1, List.of(0));

        // Resolve trigger to get the may-pay prompt
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);

        // No Pirate token created
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Pirate") && p.getCard().isToken());
    }

    @Test
    @DisplayName("Attacking without another Pirate does not trigger ability")
    void attackWithoutAnotherPirateDoesNotTrigger() {
        addCreatureReady(player1, new FathomFleetCaptain());
        // No other Pirate on the battlefield
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        declareAttackers(player1, List.of(0));

        // No triggered ability on the stack
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Pirate tokens do not satisfy the 'another nontoken Pirate' condition")
    void pirateTokensDoNotSatisfyCondition() {
        addCreatureReady(player1, new FathomFleetCaptain());
        addPirateToken(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        declareAttackers(player1, List.of(0));

        // No triggered ability on the stack because the only other Pirate is a token
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Nontoken Pirate satisfies condition even when Pirate tokens also present")
    void nontokenPirateSatisfiesConditionWithTokensPresent() {
        addCreatureReady(player1, new FathomFleetCaptain());
        addPirateCreature(player1);
        addPirateToken(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        declareAttackers(player1, List.of(0));

        // Trigger should be on the stack
        assertThat(gd.stack).isNotEmpty();

        // Resolve trigger to get the may-pay prompt
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting with insufficient mana treats as decline")
    void cannotPayTreatsAsDecline() {
        addCreatureReady(player1, new FathomFleetCaptain());
        addPirateCreature(player1);
        // No mana added

        declareAttackers(player1, List.of(0));

        // Resolve trigger to get the may-pay prompt
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        // No token created (insufficient mana)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Pirate") && p.getCard().isToken());
    }

    @Test
    void opposingPirateDoesNotSatisfyCondition() {
        addCreatureReady(player1, new FathomFleetCaptain());
        addPirateCreature(player2);

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction())
                .isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    void losingOtherPirateBeforeResolutionPreventsPaymentAndToken() {
        addCreatureReady(player1, new FathomFleetCaptain());
        Permanent otherPirate = addCreatureReady(player1, new DireFleetInterloper());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        declareAttackers(player1, List.of(0));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(otherPirate);
        gd.playerGraveyards.get(player1.getId()).add(otherPirate.getCard());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction())
                .isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(countPermanents(player1, "Pirate")).isZero();
    }

    @Test
    void removingCaptainDoesNotRemoveItsAttackTrigger() {
        Permanent captain = addCreatureReady(player1, new FathomFleetCaptain());
        addPirateCreature(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        declareAttackers(player1, List.of(0));
        gd.playerBattlefields.get(player1.getId()).remove(captain);
        gd.playerGraveyards.get(player1.getId()).add(captain.getCard());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Pirate")).isEqualTo(1);
        assertThat(countPermanents(player2, "Pirate")).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        Permanent token = findPermanent(player1, "Pirate");
        assertThat(token.isTapped()).isFalse();
        assertThat(token.isAttacking()).isFalse();
    }

    private void addPirateCreature(Player player) {
        addCreatureReady(player, new DireFleetInterloper());
    }

    private void addPirateToken(Player player) {
        Card pirate = new Card();
        pirate.setName("Pirate");
        pirate.setType(CardType.CREATURE);
        pirate.setSubtypes(List.of(CardSubtype.PIRATE));
        pirate.setPower(2);
        pirate.setToughness(2);
        pirate.setColor(CardColor.BLACK);
        pirate.setToken(true);
        addCreatureReady(player, pirate);
    }
}
