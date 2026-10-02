package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinFireleaper.class, GrizzlyBears.class, Shock.class})
class GoblinFireleaperTest extends BaseCardTest {

    @Test
    @DisplayName("{1}{R} gives Goblin Fireleaper +1/+0 until end of turn")
    void activatedAbilityBoostsPower() {
        Permanent fireleaper = addCreatureReady(player1, new GoblinFireleaper());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(fireleaper.getEffectivePower()).isEqualTo(2);
        assertThat(fireleaper.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("When Goblin Fireleaper dies, it deals damage equal to its power to an opponent's creature")
    void deathTriggerDealsItsCurrentPowerToOpponentCreature() {
        Permanent fireleaper = addCreatureReady(player1, new GoblinFireleaper());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.castInstant(player1, 0, fireleaper.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validIds()).contains(opponentCreature.getId())
                .doesNotContain(ownCreature.getId());
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Goblin Fireleaper");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);
    }
}
