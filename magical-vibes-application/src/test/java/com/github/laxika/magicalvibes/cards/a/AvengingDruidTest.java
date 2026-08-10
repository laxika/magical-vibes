package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AvengingDruidTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the damage trigger reveals until a land and puts the rest into the graveyard")
    void acceptingDamageTriggerFindsLand() {
        Card shock = new Shock();
        Card forest = new Forest();
        Card island = new Island();
        attackAndResolveTrigger(List.of(shock, forest, island));

        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shock);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island);
    }

    @Test
    @DisplayName("Declining the damage trigger leaves the library unchanged")
    void decliningDamageTriggerDoesNothing() {
        Card shock = new Shock();
        Card forest = new Forest();
        attackAndResolveTrigger(List.of(shock, forest));

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof Forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(shock, forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(shock, forest);
    }

    @Test
    @DisplayName("Accepting with no land puts the entire library into the graveyard")
    void acceptingWithNoLandMillsTheLibrary() {
        Card shock = new Shock();
        Card bears = new GrizzlyBears();
        attackAndResolveTrigger(List.of(shock, bears));

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shock, bears);
    }

    private void attackAndResolveTrigger(List<Card> library) {
        Permanent druid = addCreatureReady(player1, new AvengingDruid());
        druid.setAttacking(true);
        harness.setLibrary(player1, library);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }
}
