package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.a.AssassinInitiate;
import com.github.laxika.magicalvibes.cards.c.ChainAssassination;
import com.github.laxika.magicalvibes.cards.e.EvieFrye;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JacobFrye.class, AssassinInitiate.class, ChainAssassination.class, GrizzlyBears.class, EvieFrye.class})
class JacobFryeTest extends BaseCardTest {

    @Test
    @DisplayName("An Assassin's combat damage exiles and copies an Assassin card")
    void copiesAssassinCardAfterCombatDamage() {
        harness.addToBattlefield(player1, new JacobFrye());
        Permanent attacker = addCreatureReady(player1, new AssassinInitiate());
        AssassinInitiate graveyardAssassin = new AssassinInitiate();
        harness.setGraveyard(player1, List.of(graveyardAssassin));

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(graveyardAssassin.getId()));
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(graveyardAssassin.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Assassin Initiate"));
    }

    @Test
    @DisplayName("The graveyard target accepts Assassin and freerunning cards only")
    void filtersGraveyardTargets() {
        harness.addToBattlefield(player1, new JacobFrye());
        Permanent attacker = addCreatureReady(player1, new AssassinInitiate());
        Card assassin = new AssassinInitiate();
        Card freerunning = new ChainAssassination();
        Card unrelated = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(assassin, freerunning, unrelated));

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        resolveCombat();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(assassin.getId(), freerunning.getId());
    }

    @Test
    @DisplayName("Non-Assassin combat damage does not trigger Jacob Frye")
    void nonAssassinDoesNotTrigger() {
        harness.addToBattlefield(player1, new JacobFrye());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new AssassinInitiate()));

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void copyCannotBeCastWithoutMana() {
        harness.addToBattlefield(player1, new JacobFrye());
        Permanent attacker = addCreatureReady(player1, new AssassinInitiate());
        Card target = new AssassinInitiate();
        harness.setGraveyard(player1, List.of(target));

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        resolveCombat();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Assassin Initiate")).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(target);
    }

    @Test
    void decliningCopyStillExilesOriginal() {
        harness.addToBattlefield(player1, new JacobFrye());
        Permanent attacker = addCreatureReady(player1, new AssassinInitiate());
        Card target = new AssassinInitiate();
        harness.setGraveyard(player1, List.of(target));

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        resolveCombat();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(target);
        assertThat(countPermanents(player1, "Assassin Initiate")).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Assassin Initiate");
    }

    @Test
    void multipleAssassinsTriggerOnceAndMayChooseNoTarget() {
        harness.addToBattlefield(player1, new JacobFrye());
        Permanent first = addCreatureReady(player1, new AssassinInitiate());
        Permanent second = addCreatureReady(player1, new AssassinInitiate());
        Card target = new AssassinInitiate();
        harness.setGraveyard(player1, List.of(target));

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(first),
                gd.playerBattlefields.get(player1.getId()).indexOf(second)));
        resolveCombat();
        harness.handleMultipleCardsChosen(player1, List.of());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(target);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void partnerSearchIsChosenByTargetedPlayer() {
        Card evie = new EvieFrye();
        harness.setLibrary(player2, List.of(evie));
        harness.castFromHand(player1, new JacobFrye(), "{2}{B}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(player1.getId(), player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).contains(evie);
        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(evie);
    }

    @Test
    void partnerSearchCanBeDeclined() {
        Card evie = new EvieFrye();
        harness.setLibrary(player2, List.of(evie));
        harness.castFromHand(player1, new JacobFrye(), "{2}{B}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(evie);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(evie);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void jacobsOwnDamageOnlyOffersCardsFromItsControllersGraveyard() {
        Permanent jacob = addCreatureReady(player1, new JacobFrye());
        Card ownCard = new AssassinInitiate();
        Card opponentsCard = new AssassinInitiate();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentsCard));

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(jacob)));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactly(ownCard.getId());
    }

    @Test
    void vanishedGraveyardTargetDoesNotCreateCopy() {
        harness.addToBattlefield(player1, new JacobFrye());
        Permanent attacker = addCreatureReady(player1, new AssassinInitiate());
        Card target = new AssassinInitiate();
        harness.setGraveyard(player1, List.of(target));

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        resolveCombat();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(countPermanents(player1, "Assassin Initiate")).isEqualTo(1);
    }
}
