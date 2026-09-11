package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BoseijuWhoEndures.class, BreedingPool.class, Forest.class, GrizzlyBears.class})
class BoseijuWhoEnduresTest extends BaseCardTest {

    @Test
    @DisplayName("Adds green mana")
    void addsGreenMana() {
        harness.addToBattlefield(player1, new BoseijuWhoEndures());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Channel destroys an opponent's nonbasic land and searches for a land with a basic land type")
    void channelDestroysAndSearches() {
        GrizzlyBears legendaryCreature = new GrizzlyBears();
        legendaryCreature.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        harness.addToBattlefield(player1, legendaryCreature);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BreedingPool());
        harness.setHand(player1, List.of(new BoseijuWhoEndures()));
        harness.setLibrary(player2, List.of(new BreedingPool(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Boseiju, Who Endures");
        harness.assertInGraveyard(player2, "Breeding Pool");

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards())
                .extracting(Card::getName)
                .containsExactly("Breeding Pool", "Forest");

        harness.getGameService().handleInteractionAnswer(
                gd, player2, new InteractionAnswer.LibraryCardChosen(1));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof Forest);
    }

    @Test
    @DisplayName("Channel cannot target a basic land")
    void cannotTargetBasicLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new BoseijuWhoEndures()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact, enchantment, or nonbasic land");
    }
}
