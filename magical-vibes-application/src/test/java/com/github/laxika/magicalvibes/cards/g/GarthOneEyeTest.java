package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BlackLotus;
import com.github.laxika.magicalvibes.cards.b.Braingeyser;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.r.Regrowth;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.cards.t.Terror;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GarthOneEye.class, Disenchant.class, Braingeyser.class, Terror.class,
        ShivanDragon.class, Regrowth.class, BlackLotus.class})
class GarthOneEyeTest extends BaseCardTest {

    @Test
    void choosesAnUnchosenCardAndOffersItsCopyForNormalCost() {
        Permanent garth = addCreatureReady(player1, new GarthOneEye());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyInAnyOrder(
                "Disenchant", "Braingeyser", "Terror", "Shivan Dragon", "Regrowth", "Black Lotus");

        harness.handleListChoice(player1, "Shivan Dragon");
        assertThat(garth.getChosenModeLabels()).containsExactly("Shivan Dragon");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Shivan Dragon");
    }

    @Test
    void doesNotOfferANameThatWasAlreadyChosen() {
        Permanent garth = addCreatureReady(player1, new GarthOneEye());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Black Lotus");
        harness.handleMayAbilityChosen(player1, false);

        garth.untap();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).containsExactlyInAnyOrder(
                "Disenchant", "Braingeyser", "Terror", "Shivan Dragon", "Regrowth");
        assertThat(choice.options()).doesNotContain("Black Lotus");
    }
}
