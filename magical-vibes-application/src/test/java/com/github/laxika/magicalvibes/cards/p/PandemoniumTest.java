package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.r.RecklessOgre;
import com.github.laxika.magicalvibes.cards.w.WallOfNets;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Pandemonium.class, RecklessOgre.class, WallOfNets.class})
class PandemoniumTest extends BaseCardTest {

    @Test
    void enteringCreatureControllerMayHaveItDealItsPowerToAnyTarget() {
        harness.addToBattlefield(player1, new Pandemonium());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new RecklessOgre()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        PendingInteraction.MayAbilityChoice mayChoice =
                harness.getGameData().interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(mayChoice).isNotNull();
        assertThat(mayChoice.playerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        assertThat(harness.getGameData().interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    void enteringCreatureMayDealItsPowerToAnotherCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WallOfNets());
        harness.addToBattlefield(player1, new Pandemonium());
        harness.setHand(player1, List.of(new RecklessOgre()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.handleMayAbilityChosen(player1, true);
        assertThat(harness.getGameData().interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void opponentCreatureControllerMakesTheChoice() {
        harness.addToBattlefield(player1, new Pandemonium());
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new RecklessOgre()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        PendingInteraction.MayAbilityChoice mayChoice =
                harness.getGameData().interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(mayChoice).isNotNull();
        assertThat(mayChoice.playerId()).isEqualTo(player2.getId());

        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, player1.getId());
        resolveAllTriggers();

        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    void decliningTheMayAbilityDealsNoDamage() {
        harness.addToBattlefield(player1, new Pandemonium());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new RecklessOgre()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
