package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GlassCasket;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TheGreatHenge;
import com.github.laxika.magicalvibes.cards.t.TrappedInTheTower;
import com.github.laxika.magicalvibes.cards.c.CrystalSlipper;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DanceOfTheManse.class, GlassCasket.class, CrystalSlipper.class,
        TrappedInTheTower.class, GrizzlyBears.class, TheGreatHenge.class})
class DanceOfTheManseTest extends BaseCardTest {

    @Test
    @DisplayName("Returns up to X eligible artifact and non-Aura enchantment cards")
    void returnsUpToXEligibleCards() {
        Card glassCasket = new GlassCasket();
        Card crystalSlipper = new CrystalSlipper();
        Card aura = new TrappedInTheTower();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(glassCasket, crystalSlipper, aura, creature));
        harness.setHand(player1, List.of(new DanceOfTheManse()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 2);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(glassCasket.getId(), crystalSlipper.getId());

        harness.handleMultipleCardsChosen(player1, List.of(glassCasket.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getId().equals(glassCasket.getId()))
                .hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(crystalSlipper, aura, creature);
    }

    @Test
    @DisplayName("Makes returned permanents 4/4 creatures when X is at least 6")
    void animatesReturnedPermanentsForLargeX() {
        Card glassCasket = new GlassCasket();
        Card crystalSlipper = new CrystalSlipper();
        Card expensiveArtifact = new TheGreatHenge();
        Card aura = new TrappedInTheTower();
        harness.setGraveyard(player1, List.of(glassCasket, crystalSlipper, expensiveArtifact, aura));
        harness.setHand(player1, List.of(new DanceOfTheManse()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castSorcery(player1, 0, 6);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(glassCasket.getId(), crystalSlipper.getId());
        harness.handleMultipleCardsChosen(player1,
                List.of(glassCasket.getId(), crystalSlipper.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Glass Casket"))).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, findPermanent(player1, "Glass Casket"))).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Crystal Slipper"))).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, findPermanent(player1, "Crystal Slipper"))).isEqualTo(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(expensiveArtifact, aura);
    }
}
