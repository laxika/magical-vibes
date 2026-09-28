package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DaringBuccaneer;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RamirezDePietroPillager.class, DaringBuccaneer.class, GrizzlyBears.class, Forest.class})
class RamirezDePietroPillagerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two Treasure tokens and makes its controller lose 2 life")
    void entersWithLifeLossAndTwoTreasures() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new RamirezDePietroPillager()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }

    @Test
    @DisplayName("A Pirate dealing combat damage exiles the top card and its cast permission persists after Ramirez leaves")
    void pirateCombatDamageExilesAndAllowsCasting() {
        Permanent ramirez = addAttackingCreature(player1, new RamirezDePietroPillager());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(topCard, new Forest()));

        resolveCombatAndTrigger();

        assertThat(gd.getCardsExiledByPermanent(ramirez.getId())).containsExactly(topCard);
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, ramirez);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(topCard.getId())).isNull();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The trigger batches multiple Pirates dealing combat damage in one damage step")
    void multiplePiratesCauseOnlyOneExile() {
        Permanent ramirez = addAttackingCreature(player1, new RamirezDePietroPillager());
        addAttackingCreature(player1, new DaringBuccaneer());
        Card first = new GrizzlyBears();
        Card second = new Forest();
        harness.setLibrary(player2, List.of(first, second));

        resolveCombatAndTrigger();

        assertThat(gd.getCardsExiledByPermanent(ramirez.getId())).containsExactly(first);
    }

    @Test
    @DisplayName("Combat damage from a non-Pirate does not trigger the ability")
    void nonPirateCombatDamageDoesNotTrigger() {
        Permanent ramirez = addCreatureReady(player1, new RamirezDePietroPillager());
        addAttackingCreature(player1, new GrizzlyBears());
        Card topCard = new Forest();
        harness.setLibrary(player2, List.of(topCard));

        resolveCombatAndTrigger();

        assertThat(gd.getCardsExiledByPermanent(ramirez.getId())).isEmpty();
        assertThat(gd.findExiledCard(topCard.getId())).isNull();
    }

    private Permanent addAttackingCreature(Player player, Card card) {
        Permanent creature = addCreatureReady(player, card);
        creature.setAttacking(true);
        return creature;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
