package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.FoulRebirth;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PromiseOfAclazotz.class, FoulRebirth.class, GrizzlyBears.class, Card.class})
class PromiseOfAclazotzTest extends BaseCardTest {

    @Test
    void frontFaceSacrificesNonDemonAndPopulates() {
        Permanent promise = harness.addToBattlefieldAndReturn(player1, new PromiseOfAclazotz());
        harness.addToBattlefield(player1, tokenCreature("Spirit", CardSubtype.SPIRIT));
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, creature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(promise);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(permanent -> permanent.getCard().isToken()).hasSize(2);
    }

    @Test
    void frontFaceCannotSacrificeDemon() {
        harness.addToBattlefield(player1, new PromiseOfAclazotz());
        Permanent demon = harness.addToBattlefieldAndReturn(player1, demon());

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(demon);
    }

    @Test
    void adventureSacrificesNonDemonAndCreatesVampireDemon() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        PromiseOfAclazotz card = new PromiseOfAclazotz();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Vampire Demon");
        assertThat(token.getEffectivePower()).isEqualTo(4);
        assertThat(token.getEffectiveToughness()).isEqualTo(3);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.VAMPIRE, CardSubtype.DEMON);
        assertThat(token.getCard().hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void decliningSacrificeDoesNotPopulate() {
        harness.addToBattlefield(player1, new PromiseOfAclazotz());
        Permanent token = harness.addToBattlefieldAndReturn(player1, tokenCreature("Spirit", CardSubtype.SPIRIT));

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2).contains(token);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void sacrificingOnlyCreatureTokenLeavesNothingToPopulate() {
        Permanent promise = harness.addToBattlefieldAndReturn(player1, new PromiseOfAclazotz());
        Permanent token = harness.addToBattlefieldAndReturn(player1, tokenCreature("Spirit", CardSubtype.SPIRIT));

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, token.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(promise);
    }

    @Test
    void sacrificeStillHappensWithoutCreatureTokensToPopulate() {
        Permanent promise = harness.addToBattlefieldAndReturn(player1, new PromiseOfAclazotz());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingToken = harness.addToBattlefieldAndReturn(player2, tokenCreature("Spirit", CardSubtype.SPIRIT));

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, creature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(promise);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(opposingToken);
    }

    @Test
    void doesNotTriggerOnOpponentsEndStep() {
        harness.addToBattlefield(player1, new PromiseOfAclazotz());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
    }

    @Test
    void adventureWithoutEligibleCreatureCreatesNoTokenAndStillAllowsEnchantmentCast() {
        Permanent demon = harness.addToBattlefieldAndReturn(player1, demon());
        harness.addToBattlefield(player2, new GrizzlyBears());
        PromiseOfAclazotz card = new PromiseOfAclazotz();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(demon);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Promise of Aclazotz");
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }

    private static Card tokenCreature(String name, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("");
        card.setColor(CardColor.WHITE);
        card.setSubtypes(List.of(subtype));
        card.setPower(1);
        card.setToughness(1);
        card.setToken(true);
        return card;
    }

    private static Card demon() {
        Card card = tokenCreature("Demon Token", CardSubtype.DEMON);
        card.setColor(CardColor.BLACK);
        return card;
    }
}
