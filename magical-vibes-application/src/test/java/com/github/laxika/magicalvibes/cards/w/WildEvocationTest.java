package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.Disentomb;
import com.github.laxika.magicalvibes.cards.f.Fling;
import com.github.laxika.magicalvibes.cards.p.Plummet;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Silence;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.GameLogSegment;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WildEvocation.class, Forest.class, RuneclawBear.class, LightningBolt.class,
        Plummet.class, Fling.class, Disentomb.class, SerraAngel.class,
        Silence.class, WhispersilkCloak.class})
class WildEvocationTest extends BaseCardTest {

    @Test
    @DisplayName("Reveals a land and puts it onto the battlefield during controller's upkeep")
    void revealsLandAndPutsOntoBattlefield() {
        harness.addToBattlefield(player1, new WildEvocation());
        Card forest = new Forest();
        harness.setHand(player1, List.of(forest));

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        // Forest should be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().hasType(CardType.LAND));
        // Hand should be empty
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Reveals a land during opponent's upkeep and puts it onto opponent's battlefield")
    void revealsLandDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new WildEvocation());
        Card forest = new Forest();
        harness.setHand(player2, List.of(forest));

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger

        // Forest should be on player2's battlefield, not player1's
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().hasType(CardType.LAND));
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Reveals a creature card and casts it without paying mana cost")
    void revealsCreatureAndCastsIt() {
        harness.addToBattlefield(player1, new WildEvocation());
        Card bears = new RuneclawBear();
        harness.setHand(player1, List.of(bears));

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger — creature goes on stack

        // Runeclaw Bear should be on the stack
        assertThat(gd.stack)
                .anyMatch(se -> se.getCard().getName().equals("Runeclaw Bear")
                        && se.getEntryType() == StackEntryType.CREATURE_SPELL);
        // Hand should be empty
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Opponent's creature is cast during opponent's upkeep with opponent as controller")
    void opponentsCastDuringTheirUpkeep() {
        harness.addToBattlefield(player1, new WildEvocation());
        Card bears = new RuneclawBear();
        harness.setHand(player2, List.of(bears));

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger

        // Spell should be controlled by player2 (the active player)
        assertThat(gd.stack)
                .anyMatch(se -> se.getCard().getName().equals("Runeclaw Bear")
                        && se.getControllerId().equals(player2.getId()));
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cast creature resolves and enters the battlefield")
    void castCreatureResolves() {
        harness.addToBattlefield(player1, new WildEvocation());
        Card bears = new RuneclawBear();
        harness.setHand(player1, List.of(bears));

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger — creature goes on stack
        harness.passBothPriorities(); // resolve creature spell

        // Runeclaw Bear should be on the battlefield
        harness.assertOnBattlefield(player1, "Runeclaw Bear");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Reveals a targeted spell and prompts for target choice")
    void revealsTargetedSpellAndPromptsForTarget() {
        harness.addToBattlefield(player1, new WildEvocation());
        harness.addToBattlefield(player2, new RuneclawBear());

        Card bolt = new LightningBolt();
        harness.setHand(player1, List.of(bolt));

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        // Should prompt player1 for target
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.HandCastSpellTarget.class);
        // Hand should be empty (card was removed for casting)
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Choosing a target for a targeted spell puts it on the stack")
    void choosingTargetPutsSpellOnStack() {
        harness.addToBattlefield(player1, new WildEvocation());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());

        Card bolt = new LightningBolt();
        harness.setHand(player1, List.of(bolt));

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.pendingEffectResolutionEntry).isNotNull();
        harness.clearMessages();

        // Choose the creature as target
        harness.handlePermanentChosen(player1, creature.getId());

        assertThat(gd.stack)
                .anyMatch(se -> se.getCard().getName().equals("Lightning Bolt")
                        && se.getTargetId().equals(creature.getId()));
        assertThat(gd.pendingEffectResolutionEntry).isNull();
        assertThat(harness.getConn1().getMessagesContaining("\"type\":\"GAME_STATE\"")).hasSize(1);
        assertThat(harness.getConn2().getMessagesContaining("\"type\":\"GAME_STATE\"")).hasSize(1);
        GameLogEntry targetLog = gd.gameLog.stream()
                .filter(entry -> entry.plainText()
                        .equals("Lightning Bolt targets Runeclaw Bear."))
                .findFirst()
                .orElseThrow();
        assertThat(targetLog.segments().getFirst())
                .isInstanceOf(GameLogSegment.CardSegment.class);
    }

    @Test
    @DisplayName("Targeted spell with no valid targets stays in hand")
    void targetedSpellNoValidTargetsStaysInHand() {
        harness.addToBattlefield(player1, new WildEvocation());
        Card plummet = new Plummet();
        harness.setHand(player1, List.of(plummet));

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(plummet);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Does nothing when active player's hand is empty")
    void doesNothingWithEmptyHand() {
        harness.addToBattlefield(player1, new WildEvocation());
        harness.setHand(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        // Nothing should happen — no spell on stack, no permanent choice
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Does nothing during opponent's upkeep when opponent's hand is empty")
    void doesNothingWhenOpponentHandEmpty() {
        harness.addToBattlefield(player1, new WildEvocation());
        harness.setHand(player2, List.of());

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Non-targeted spell cast increments spellsCastThisTurn counter")
    void spellCastIncrementCounter() {
        harness.addToBattlefield(player1, new WildEvocation());
        Card bears = new RuneclawBear();
        harness.setHand(player1, List.of(bears));
        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.getSpellsCastThisTurnCount(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("A revealed spell stays in hand when Silence prohibits casting it")
    void cannotCastWhileSilenced() {
        harness.addToBattlefield(player1, new WildEvocation());
        Card bear = new RuneclawBear();
        harness.setHand(player1, List.of(bear));
        advanceToUpkeep(player1);

        harness.castFromHand(player2, new Silence(), "{W}");
        harness.passBothPriorities();
        assertThat(gd.playersSilencedThisTurn).contains(player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bear);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getSpellsCastThisTurnCount(player1.getId())).isZero();
    }

    @Test
    @DisplayName("Fling stays in hand when there is no creature to pay its additional cost")
    void cannotCastWithoutMandatorySacrifice() {
        harness.addToBattlefield(player1, new WildEvocation());
        Card fling = new Fling();
        harness.setHand(player1, List.of(fling));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(fling);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Fling pays its mandatory sacrifice cost and deals the sacrificed creature's power")
    void paysMandatorySacrificeWhenAble() {
        harness.addToBattlefield(player1, new WildEvocation());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new Fling()));
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, bear.getId());

        harness.assertNotOnBattlefield(player1, "Runeclaw Bear");
        harness.assertInGraveyard(player1, "Runeclaw Bear");
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A revealed Lightning Bolt can target a player and resolves without mana payment")
    void targetedSpellResolvesAgainstPlayer() {
        harness.addToBattlefield(player1, new WildEvocation());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player1, "Lightning Bolt");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A creature with shroud is not a legal target for the revealed Plummet")
    void cannotCastWhenOnlyMatchingCreatureHasShroud() {
        harness.addToBattlefield(player1, new WildEvocation());
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        Permanent cloak = harness.addToBattlefieldAndReturn(player2, new WhispersilkCloak());
        cloak.setAttachedTo(angel.getId());
        Card plummet = new Plummet();
        harness.setHand(player1, List.of(plummet));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(plummet);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A revealed Disentomb can target a creature card in the active player's graveyard")
    void castsSpellWithGraveyardTarget() {
        harness.addToBattlefield(player1, new WildEvocation());
        Card bear = new RuneclawBear();
        harness.setGraveyard(player1, List.of(bear));
        Card disentomb = new Disentomb();
        harness.setHand(player1, List.of(disentomb));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, bear.getId());
        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == disentomb
                && bear.getId().equals(entry.getTargetId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(bear);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bear);
    }

    @Test
    @DisplayName("Exactly one randomly chosen land is revealed and put onto the battlefield")
    void playsOnlyOneCardFromMultipleCardHand() {
        harness.addToBattlefield(player1, new WildEvocation());
        Card first = new Forest();
        Card second = new Forest();
        harness.setHand(player1, List.of(first, second));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() == first || permanent.getCard() == second)
                .hasSize(1);
        assertThat(gd.stack).isEmpty();
    }
}
