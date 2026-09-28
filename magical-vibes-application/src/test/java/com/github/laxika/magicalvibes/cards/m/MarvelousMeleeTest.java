package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarvelousMelee.class, Forest.class, GrizzlyBears.class, HillGiant.class})
class MarvelousMeleeTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 6 damage to target creature")
    void dealsSixDamageToTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new MarvelousMelee()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Basic landcycling searches for a basic land")
    void basicLandcyclingSearchesForBasicLand() {
        harness.setHand(player1, List.of(new MarvelousMelee()));
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Marvelous Melee");
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Forest");

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Forest");
    }
}
