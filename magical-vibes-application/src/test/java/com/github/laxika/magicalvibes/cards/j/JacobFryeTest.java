package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.a.AssassinInitiate;
import com.github.laxika.magicalvibes.cards.c.ChainAssassination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JacobFrye.class, AssassinInitiate.class, ChainAssassination.class, GrizzlyBears.class})
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
        harness.handleMayAbilityChosen(player1, true);
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
}
