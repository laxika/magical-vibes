package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DiabolicEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TyrannicalPitlord.class, DiabolicEdict.class, GrizzlyBears.class})
class TyrannicalPitlordTest extends BaseCardTest {

    @Test
    void chosenCreatureGetsBoostAndFlying() {
        Permanent chosen = addCreatureReady(player1, new GrizzlyBears());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());

        harness.castFromHand(player1, new TyrannicalPitlord(), "{4}{B}{B}");
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, chosen.getId());

        assertThat(gqs.getEffectivePower(gd, chosen)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, chosen)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, chosen, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isFalse();
    }

    @Test
    void leavingBattlefieldSacrificesChosenCreature() {
        Permanent chosen = addCreatureReady(player1, new GrizzlyBears());
        harness.castFromHand(player1, new TyrannicalPitlord(), "{4}{B}{B}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosen.getId());
        Permanent pitlord = findPermanent(player1, "Tyrannical Pitlord");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DiabolicEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, pitlord.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Tyrannical Pitlord");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void entersWithoutChoiceWhenYouControlNoOtherCreature() {
        harness.castFromHand(player1, new TyrannicalPitlord(), "{4}{B}{B}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction() instanceof PendingInteraction.PermanentChoice).isFalse();
        harness.assertOnBattlefield(player1, "Tyrannical Pitlord");
    }
}
