package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.r.RedwoodTreefolk;
import com.github.laxika.magicalvibes.cards.t.Thunderbolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MistmoonGriffin.class, RedwoodTreefolk.class, Thunderbolt.class})
class MistmoonGriffinTest extends BaseCardTest {

    /** Kills the Griffin so its death trigger resolves. */
    private void thunderboltAndResolveDeathTrigger() {
        Permanent griffin = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new Thunderbolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castModalInstant(player1, 0, 1, List.of(griffin.getId()));
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Dying Griffin is exiled and the top creature card of its controller's graveyard is reanimated")
    void diesExilesItselfAndReanimatesTopCreatureCard() {
        harness.addToBattlefield(player1, new MistmoonGriffin());
        Card griffinCard = gd.playerBattlefields.get(player1.getId()).getFirst().getCard();
        Card bottomCreature = new RedwoodTreefolk();
        Card topCreature = new RedwoodTreefolk();
        // The second Treefolk is the last card put into the graveyard, so it is the top creature card.
        harness.setGraveyard(player1, List.of(bottomCreature, topCreature));

        thunderboltAndResolveDeathTrigger();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(griffinCard.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .extracting(Card::getId)
                .containsExactly(topCreature.getId());
        // Only one creature card comes back. (Thunderbolt is also in the graveyard by now, so this
        // is a contains-check, not an exact match.)
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(bottomCreature.getId())
                .doesNotContain(topCreature.getId());
    }

    @Test
    @DisplayName("Noncreature cards above the top creature card are skipped")
    void skipsNoncreatureCardsAboveTheTopCreatureCard() {
        harness.addToBattlefield(player1, new MistmoonGriffin());
        Card creature = new RedwoodTreefolk();
        Card noncreature = new Thunderbolt();
        harness.setGraveyard(player1, List.of(creature, noncreature));

        thunderboltAndResolveDeathTrigger();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .extracting(Card::getId)
                .containsExactly(creature.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(noncreature.getId())
                .doesNotContain(creature.getId());
    }

    @Test
    @DisplayName("The Griffin never reanimates itself — it is exiled before the top creature card is looked up")
    void doesNotReanimateItself() {
        harness.addToBattlefield(player1, new MistmoonGriffin());
        Card griffinCard = gd.playerBattlefields.get(player1.getId()).getFirst().getCard();
        harness.setGraveyard(player1, List.of());

        thunderboltAndResolveDeathTrigger();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(griffinCard.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The trigger searches its controller's graveyard only")
    void onlyUsesTheControllersGraveyard() {
        harness.addToBattlefield(player1, new MistmoonGriffin());
        Card opponentCreature = new RedwoodTreefolk();
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(opponentCreature));

        thunderboltAndResolveDeathTrigger();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(opponentCreature.getId());
    }
}
