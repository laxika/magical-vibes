package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DesertOfTheTrue;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpinewoodsArmadillo.class, DesertOfTheTrue.class, Forest.class, GrizzlyBears.class})
class SpinewoodsArmadilloTest extends BaseCardTest {

    @Test
    @DisplayName("The hand ability discards the card and offers basic lands and Deserts")
    void handAbilityOffersBasicLandsAndDeserts() {
        SpinewoodsArmadillo armadillo = new SpinewoodsArmadillo();
        Forest forest = new Forest();
        DesertOfTheTrue desert = new DesertOfTheTrue();
        harness.setHand(player1, List.of(armadillo));
        harness.setLibrary(player1, List.of(forest, desert, new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(armadillo);
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(forest, desert);
    }

    @Test
    @DisplayName("Choosing a basic land or Desert puts it into hand and gains 3 life")
    void choosingLandGainsLife() {
        SpinewoodsArmadillo armadillo = new SpinewoodsArmadillo();
        Card desert = new DesertOfTheTrue();
        harness.setHand(player1, List.of(armadillo));
        harness.setLibrary(player1, List.of(new Forest(), desert, new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).contains(desert);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }
}
