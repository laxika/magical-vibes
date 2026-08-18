package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KatakiWarsWageTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {1} keeps your artifact on the battlefield")
    void paysToKeepArtifact() {
        Permanent relic = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        harness.addToBattlefield(player1, new KatakiWarsWage());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(relic);
    }

    @Test
    @DisplayName("Declining the payment sacrifices your artifact but not Kataki")
    void declinesAndSacrificesArtifact() {
        Permanent relic = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        harness.addToBattlefield(player1, new KatakiWarsWage());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(relic);
        harness.assertInGraveyard(player1, "Darksteel Relic");
        harness.assertOnBattlefield(player1, "Kataki, War's Wage");
    }

    @Test
    @DisplayName("Kataki affects artifacts controlled by an opponent")
    void affectsOpponentsArtifact() {
        harness.addToBattlefield(player1, new KatakiWarsWage());
        Permanent relic = harness.addToBattlefieldAndReturn(player2, new DarksteelRelic());

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(relic);
        harness.assertInGraveyard(player2, "Darksteel Relic");
    }
}
