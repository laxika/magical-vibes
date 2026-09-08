package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoggWarMarshal.class, WrathOfGod.class})
class MoggWarMarshalTest extends BaseCardTest {

    @Test
    @DisplayName("Entering creates a Goblin, and declining echo creates another when it dies")
    void entersAndDiesCreateGoblins() {
        castAndResolveMoggWarMarshal();

        assertThat(goblinTokens()).hasSize(1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mogg War Marshal");
        assertThat(goblinTokens()).hasSize(2);
    }

    @Test
    @DisplayName("A Mogg War Marshal death trigger creates a Goblin")
    void deathCreatesGoblin() {
        harness.addToBattlefield(player1, new MoggWarMarshal());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(goblinTokens()).hasSize(1);
    }

    private void castAndResolveMoggWarMarshal() {
        harness.setHand(player1, List.of(new MoggWarMarshal()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Mogg War Marshal");
    }

    private List<Permanent> goblinTokens() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .filter(p -> p.getCard().getName().equals("Goblin"))
                .toList();
    }
}
