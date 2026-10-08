package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Panharmonicon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SmirkingSpelljacker.class, GrizzlyBears.class, Panharmonicon.class})
class SmirkingSpelljackerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and exiles a spell an opponent controls")
    void entersAndExilesOpponentsSpell() {
        GrizzlyBears spell = castOpponentSpellAndSpelljacker();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(spell.getId());
        harness.handlePermanentChosen(player1, spell.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(spell.getId());
    }

    @Test
    @DisplayName("Cannot target a spell controlled by its controller")
    void cannotTargetOwnSpell() {
        SmirkingSpelljacker spelljacker = new SmirkingSpelljacker();
        SmirkingSpelljacker ownSpell = new SmirkingSpelljacker();
        SmirkingSpelljacker opponentSpell = new SmirkingSpelljacker();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(opponentSpell));
        harness.addMana(player2, com.github.laxika.magicalvibes.model.ManaColor.BLUE, 5);
        harness.setHand(player1, List.of(spelljacker, ownSpell));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLUE, 10);

        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        harness.castCreature(player1, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == ownSpell);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(opponentSpell.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownSpell.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, opponentSpell.getId());
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Attacking offers the exiled spell for free")
    void attackingOffersExiledSpellForFree() {
        GrizzlyBears spell = castOpponentSpellAndSpelljacker();
        harness.handlePermanentChosen(player1, spell.getId());
        harness.passBothPriorities();

        Permanent source = findPermanent(player1, "Smirking Spelljacker");
        source.setSummoningSick(false);
        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(spell.getId()));
        assertThat(gd.findExiledCard(spell.getId())).isNull();
    }

    @Test
    @DisplayName("Attacking without an exiled card does not trigger")
    void attackingWithoutExiledCardDoesNotTrigger() {
        addCreatureReady(player1, new SmirkingSpelljacker());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("Smirking Spelljacker's attack ability triggers.")).isFalse();
    }

    @Test
    @DisplayName("Declining the free cast leaves the card available for a later attack")
    void decliningAllowsCastingOnLaterAttack() {
        GrizzlyBears spell = castOpponentSpellAndSpelljacker();
        harness.handlePermanentChosen(player1, spell.getId());
        harness.passBothPriorities();
        Permanent source = findPermanent(player1, "Smirking Spelljacker");
        source.setSummoningSick(false);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.findExiledCard(spell.getId())).isNotNull();
        source.untap();
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.findExiledCard(spell.getId())).isNull();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Leaving the battlefield does not return the exiled spell")
    void leavingDoesNotReturnExiledSpell() {
        GrizzlyBears spell = castOpponentSpellAndSpelljacker();
        harness.handlePermanentChosen(player1, spell.getId());
        harness.passBothPriorities();
        Permanent source = findPermanent(player1, "Smirking Spelljacker");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, source));
        resolveAllTriggers();

        assertThat(gd.findExiledCard(spell.getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An attack trigger can cast its linked card after Spelljacker leaves")
    void attackTriggerSurvivesSourceLeaving() {
        GrizzlyBears spell = castOpponentSpellAndSpelljacker();
        harness.handlePermanentChosen(player1, spell.getId());
        harness.passBothPriorities();
        Permanent source = findPermanent(player1, "Smirking Spelljacker");
        source.setSummoningSick(false);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, source));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.findExiledCard(spell.getId())).isNull();
    }

    @Test
    @CardUsed({SmirkingSpelljacker.class, Panharmonicon.class})
    @DisplayName("A single attack allows casting every card exiled by doubled entry triggers")
    void doubledEntryAllowsCastingBothExiledCards() {
        harness.addToBattlefield(player1, new Panharmonicon());
        SmirkingSpelljacker firstSpell = new SmirkingSpelljacker();
        SmirkingSpelljacker secondSpell = new SmirkingSpelljacker();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(firstSpell, secondSpell));
        harness.addMana(player2, com.github.laxika.magicalvibes.model.ManaColor.BLUE, 10);
        harness.setHand(player1, List.of(new SmirkingSpelljacker()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLUE, 5);

        harness.castCreature(player2, 0);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, firstSpell.getId());
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, secondSpell.getId());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(firstSpell.getId(), secondSpell.getId());
        Permanent source = findPermanent(player1, "Smirking Spelljacker");
        source.setSummoningSick(false);
        declareAttackers(List.of(1));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(permanent -> permanent.getCard().getId())
                .contains(firstSpell.getId(), secondSpell.getId());
    }

    private GrizzlyBears castOpponentSpellAndSpelljacker() {
        GrizzlyBears spell = new GrizzlyBears();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new SmirkingSpelljacker()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLUE, 5);

        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return spell;
    }
}
