package com.github.laxika.magicalvibes.cards.t;

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

@CardUsed({TheFalconAirshipRestored.class, GrizzlyBears.class})
class TheFalconAirshipRestoredTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage may sacrifice The Falcon and return a creature card")
    void combatDamageMaySacrificeAndReturnCreature() {
        Permanent falcon = addReadyFalcon();
        addCreatureReady(player1, new GrizzlyBears());
        Card returned = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(returned));
        crewFalcon();
        assertThat(gqs.isCreature(gd, falcon)).isTrue();

        falcon.setAttacking(true);
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(returned.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "The Falcon, Airship Restored");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the combat-damage sacrifice keeps The Falcon on the battlefield")
    void combatDamageSacrificeCanBeDeclined() {
        Permanent falcon = addReadyFalcon();
        addCreatureReady(player1, new GrizzlyBears());
        crewFalcon();

        falcon.setAttacking(true);
        resolveCombat();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "The Falcon, Airship Restored");
    }

    @Test
    @DisplayName("The graveyard ability returns The Falcon tapped")
    void graveyardAbilityReturnsSelfTapped() {
        harness.setGraveyard(player1, List.of(new TheFalconAirshipRestored()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "The Falcon, Airship Restored");
        assertThat(returned).isNotNull();
        assertThat(returned.isTapped()).isTrue();
    }

    private Permanent addReadyFalcon() {
        Permanent falcon = new Permanent(new TheFalconAirshipRestored());
        falcon.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(falcon);
        return falcon;
    }

    private void crewFalcon() {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
