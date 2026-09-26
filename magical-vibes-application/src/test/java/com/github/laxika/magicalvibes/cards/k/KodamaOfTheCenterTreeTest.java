package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GoblinCohort;
import com.github.laxika.magicalvibes.cards.h.HerosDemise;
import com.github.laxika.magicalvibes.cards.t.Tallowisp;
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

@CardUsed({KodamaOfTheCenterTree.class, KamiOfFalseHope.class, Tallowisp.class,
        GoblinCohort.class, HerosDemise.class})
class KodamaOfTheCenterTreeTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness equal the number of Spirits controlled")
    void powerAndToughnessTrackControlledSpirits() {
        Permanent kodama = harness.addToBattlefieldAndReturn(player1, new KodamaOfTheCenterTree());
        assertThat(gqs.getEffectivePower(gd, kodama)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, kodama)).isEqualTo(1);

        harness.addToBattlefield(player1, new KamiOfFalseHope());
        harness.addToBattlefield(player1, new GoblinCohort());
        harness.addToBattlefield(player2, new KamiOfFalseHope());

        assertThat(gqs.getEffectivePower(gd, kodama)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, kodama)).isEqualTo(2);
    }

    @Test
    @DisplayName("Soulshift X includes Kodama and returns a Spirit within the value fixed at death")
    void soulshiftUsesSpiritCountIncludingKodama() {
        Permanent kodama = harness.addToBattlefieldAndReturn(player1, new KodamaOfTheCenterTree());
        harness.addToBattlefield(player1, new KamiOfFalseHope());
        Card eligibleSpirit = new Tallowisp();
        Card ineligibleNonSpirit = new GoblinCohort();
        harness.setGraveyard(player1, List.of(eligibleSpirit, ineligibleNonSpirit));

        harness.setHand(player1, List.of(new HerosDemise()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, kodama.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).contains(eligibleSpirit.getId());
        assertThat(choice.validCardIds()).doesNotContain(ineligibleNonSpirit.getId());

        harness.handleMultipleCardsChosen(player1, List.of(eligibleSpirit.getId()));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Tallowisp");
    }

    @Test
    @DisplayName("Soulshift may be declined after choosing its target")
    void soulshiftMayBeDeclinedAfterChoosingTarget() {
        Permanent kodama = harness.addToBattlefieldAndReturn(player1, new KodamaOfTheCenterTree());
        harness.addToBattlefield(player1, new KamiOfFalseHope());
        Card eligibleSpirit = new Tallowisp();
        harness.setGraveyard(player1, List.of(eligibleSpirit));

        harness.setHand(player1, List.of(new HerosDemise()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, kodama.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(eligibleSpirit.getId()));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Tallowisp");
    }
}
