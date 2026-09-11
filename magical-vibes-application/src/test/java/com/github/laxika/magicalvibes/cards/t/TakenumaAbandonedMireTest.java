package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TakenumaAbandonedMire.class, Forest.class, GrizzlyBears.class})
class TakenumaAbandonedMireTest extends BaseCardTest {

    @Test
    @DisplayName("Adds black mana")
    void addsBlackMana() {
        harness.addToBattlefield(player1, new TakenumaAbandonedMire());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Channel mills three cards and returns a creature card to hand")
    void channelMillsAndReturnsCreatureWithLegendaryCostReduction() {
        Card target = new GrizzlyBears();
        GrizzlyBears legendaryCreature = new GrizzlyBears();
        legendaryCreature.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        harness.addToBattlefield(player1, legendaryCreature);
        harness.setGraveyard(player1, List.of(target));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new TakenumaAbandonedMire()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbilityWithGraveyardTargets(player1, 0, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(target);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(target);
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Takenuma, Abandoned Mire");
    }

    @Test
    @DisplayName("Channel cannot target a noncreature, nonplaneswalker card")
    void channelCannotTargetNonCreatureOrPlaneswalkerCard() {
        Card target = new Forest();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new TakenumaAbandonedMire()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateHandAbilityWithGraveyardTargets(
                player1, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Takenuma, Abandoned Mire");
    }
}
