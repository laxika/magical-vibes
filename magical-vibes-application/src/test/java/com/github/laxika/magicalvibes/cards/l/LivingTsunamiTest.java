package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LivingTsunamiTest extends BaseCardTest {

    @Test
    @DisplayName("Auto-sacrifices when its controller has no land")
    void autoSacrificesWithoutLand() {
        addTsunami();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Living Tsunami");
        harness.assertInGraveyard(player1, "Living Tsunami");
    }

    @Test
    @DisplayName("Returning a land keeps Living Tsunami")
    void returningLandKeepsTsunami() {
        Permanent tsunami = addTsunami();
        harness.addToBattlefield(player1, new Island());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(tsunami);
        harness.assertNotOnBattlefield(player1, "Island");
        assertThat(gd.playerHands.get(player1.getId()).stream()
                .anyMatch(card -> card.getName().equals("Island"))).isTrue();
    }

    @Test
    @DisplayName("A tapped land can be returned")
    void returningTappedLandKeepsTsunami() {
        Permanent tsunami = addTsunami();
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        island.tap();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(tsunami);
        harness.assertNotOnBattlefield(player1, "Island");
    }

    @Test
    @DisplayName("Declining to return a land sacrifices Living Tsunami")
    void decliningSacrificesTsunami() {
        addTsunami();
        harness.addToBattlefield(player1, new Plains());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Living Tsunami");
        harness.assertInGraveyard(player1, "Living Tsunami");
        harness.assertOnBattlefield(player1, "Plains");
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        Permanent tsunami = addTsunami();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(tsunami);
    }

    private Permanent addTsunami() {
        Permanent permanent = new Permanent(new LivingTsunami());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }
}
