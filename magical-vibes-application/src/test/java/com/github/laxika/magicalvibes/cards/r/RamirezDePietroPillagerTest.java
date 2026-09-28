package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FathomFleetFirebrand;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RamirezDePietroPillager.class, FathomFleetFirebrand.class, GrizzlyBears.class})
class RamirezDePietroPillagerTest extends BaseCardTest {

    @Test
    @DisplayName("When Ramirez enters, you lose 2 life and create two Treasures")
    void enterTheBattlefieldTrigger() {
        int lifeBefore = gd.getLife(player1.getId());
        harness.setHand(player1, List.of(new RamirezDePietroPillager()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 2);
        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }

    @Test
    @DisplayName("One or more Pirates dealing combat damage exiles one card and grants lasting cast permission")
    void pirateCombatDamageExilesOneCard() {
        Permanent ramirez = addCreatureReady(player1, new RamirezDePietroPillager());
        addAttackingPirate();
        addAttackingPirate();

        Card topCard = new GrizzlyBears();
        topCard.setOwnerId(player2.getId());
        harness.setLibrary(player2, List.of(topCard));

        resolveCombatAndTrigger();

        assertThat(gd.getCardsExiledByPermanent(ramirez.getId())).containsExactly(topCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).doesNotContain(topCard.getId());

        gd.playerBattlefields.get(player1.getId()).remove(ramirez);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Combat damage from a non-Pirate does not trigger Ramirez")
    void nonPirateCombatDamageDoesNotTrigger() {
        Permanent ramirez = addCreatureReady(player1, new RamirezDePietroPillager());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setAttacking(true);
        bears.setAttackTarget(player2.getId());

        Card topCard = new GrizzlyBears();
        topCard.setOwnerId(player2.getId());
        harness.setLibrary(player2, List.of(topCard));

        resolveCombat();

        assertThat(gd.getCardsExiledByPermanent(ramirez.getId())).isEmpty();
    }

    private Permanent addAttackingPirate() {
        Permanent pirate = addCreatureReady(player1, new FathomFleetFirebrand());
        pirate.setAttacking(true);
        pirate.setAttackTarget(player2.getId());
        return pirate;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
