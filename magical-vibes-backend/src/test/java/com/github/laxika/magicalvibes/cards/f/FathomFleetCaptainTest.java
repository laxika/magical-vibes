package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ControlsAnotherSubtypeConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class FathomFleetCaptainTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has ControlsAnotherSubtypeConditionalEffect(PIRATE, nontokenOnly=true) wrapping MayPayManaEffect wrapping CreateTokenEffect on ON_ATTACK")
    void hasCorrectStructure() {
        FathomFleetCaptain card = new FathomFleetCaptain();

        assertThat(card.getEffects(EffectSlot.ON_ATTACK)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ATTACK).getFirst())
                .isInstanceOf(ControlsAnotherSubtypeConditionalEffect.class);

        ControlsAnotherSubtypeConditionalEffect conditional =
                (ControlsAnotherSubtypeConditionalEffect) card.getEffects(EffectSlot.ON_ATTACK).getFirst();
        assertThat(conditional.subtypes()).isEqualTo(Set.of(CardSubtype.PIRATE));
        assertThat(conditional.nontokenOnly()).isTrue();
        assertThat(conditional.wrapped()).isInstanceOf(MayPayManaEffect.class);

        MayPayManaEffect mayPay = (MayPayManaEffect) conditional.wrapped();
        assertThat(mayPay.manaCost()).isEqualTo("{2}");
        assertThat(mayPay.wrapped()).isInstanceOf(CreateTokenEffect.class);

        CreateTokenEffect token = (CreateTokenEffect) mayPay.wrapped();
        assertThat(token.tokenName()).isEqualTo("Pirate");
        assertThat(token.power()).isEqualTo(2);
        assertThat(token.toughness()).isEqualTo(2);
        assertThat(token.color()).isEqualTo(CardColor.BLACK);
        assertThat(token.subtypes()).contains(CardSubtype.PIRATE);
        assertThat(token.keywords()).contains(Keyword.MENACE);
    }

    // ===== Trigger fires and creates token when condition met =====

    @Test
    @DisplayName("Attacking with another nontoken Pirate triggers may-pay and creates 2/2 Pirate token with menace")
    void attackWithAnotherPirateCreatesToken() {
        addCreatureReady(player1, new FathomFleetCaptain());
        addPirateCreature(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        declareAttackers(player1, List.of(0));

        // Trigger is on the stack; resolve it to get the may-pay prompt
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);

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

    // ===== Decline does not create token =====

    @Test
    @DisplayName("Declining may-pay does not create token")
    void declineDoesNotCreateToken() {
        addCreatureReady(player1, new FathomFleetCaptain());
        addPirateCreature(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        declareAttackers(player1, List.of(0));

        // Resolve trigger to get the may-pay prompt
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);

        harness.handleMayAbilityChosen(player1, false);

        // No Pirate token created
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Pirate") && p.getCard().isToken());
    }

    // ===== No other Pirate — trigger does not fire =====

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

    // ===== Token Pirates do not satisfy the condition =====

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

    // ===== Nontoken Pirate satisfies but token Pirate does not =====

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

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
    }

    // ===== Cannot pay — no token created =====

    @Test
    @DisplayName("Accepting with insufficient mana treats as decline")
    void cannotPayTreatsAsDecline() {
        addCreatureReady(player1, new FathomFleetCaptain());
        addPirateCreature(player1);
        // No mana added

        declareAttackers(player1, List.of(0));

        // Resolve trigger to get the may-pay prompt
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);

        harness.handleMayAbilityChosen(player1, true);

        // No token created (insufficient mana)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Pirate") && p.getCard().isToken());
    }

    // ===== Helper methods =====

    private Permanent addCreatureReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void addPirateCreature(Player player) {
        Card pirate = new Card();
        pirate.setName("Pirate Creature");
        pirate.setType(CardType.CREATURE);
        pirate.setSubtypes(List.of(CardSubtype.HUMAN, CardSubtype.PIRATE));
        pirate.setPower(2);
        pirate.setToughness(2);
        pirate.setManaCost("{1}{B}");
        pirate.setColor(CardColor.BLACK);
        Permanent perm = new Permanent(pirate);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
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
        Permanent perm = new Permanent(pirate);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);
        gs.declareAttackers(gd, player, attackerIndices);
    }

    private void resolveAllTriggers() {
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
