package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.ExpeditionHealer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AngelOfUnity.class, ExpeditionHealer.class, GrizzlyBears.class})
class AngelOfUnityTest extends BaseCardTest {

    @Test
    void perpetuallyBoostsAChosenPartyCreatureFromItsEtbTrigger() {
        ExpeditionHealer healerToBoost = new ExpeditionHealer();
        UUID healerToBoostId = healerToBoost.getId();
        harness.setHand(player1, List.of(new AngelOfUnity(), healerToBoost, new ExpeditionHealer()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PerpetualPowerToughnessChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.castCreature(player1, 1);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PerpetualPowerToughnessChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent permanent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(candidate -> candidate.getCard().getId().equals(healerToBoostId))
                .findFirst().orElseThrow();
        assertThat(permanent.getPowerModifier()).isEqualTo(2);
        assertThat(permanent.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    void doesNotPromptWhenTheHandHasNoPartyCreature() {
        harness.setHand(player1, List.of(new AngelOfUnity(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
