package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AuraOfSilence;
import com.github.laxika.magicalvibes.cards.n.NyxbornShieldmate;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GriffinDreamfinder.class, AuraOfSilence.class, GrizzlyBears.class, NyxbornShieldmate.class})
class GriffinDreamfinderTest extends BaseCardTest {

    private void castGriffinDreamfinder() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new GriffinDreamfinder(), "{3}{W}{W}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB returns a targeted enchantment card from graveyard to hand")
    void etbReturnsEnchantmentToHand() {
        AuraOfSilence aura = new AuraOfSilence();
        harness.setGraveyard(player1, List.of(aura));

        castGriffinDreamfinder();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(aura.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Aura of Silence");
        harness.assertNotInGraveyard(player1, "Aura of Silence");
    }

    @Test
    @DisplayName("A nonenchantment card in the graveyard is not a legal target")
    void nonEnchantmentIsNotTargetable() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        castGriffinDreamfinder();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("An enchantment creature is a legal graveyard target")
    void returnsEnchantmentCreature() {
        NyxbornShieldmate shieldmate = new NyxbornShieldmate();
        harness.setGraveyard(player1, List.of(shieldmate));

        castGriffinDreamfinder();
        harness.handleMultipleCardsChosen(player1, List.of(shieldmate.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Nyxborn Shieldmate");
        harness.assertNotInGraveyard(player1, "Nyxborn Shieldmate");
        harness.assertNotOnBattlefield(player1, "Nyxborn Shieldmate");
    }

    @Test
    @DisplayName("An enchantment in only the opponent's graveyard cannot be returned")
    void cannotReturnOpponentsEnchantment() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new NyxbornShieldmate()));

        castGriffinDreamfinder();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Griffin Dreamfinder");
        harness.assertInGraveyard(player2, "Nyxborn Shieldmate");
        harness.assertNotInHand(player1, "Nyxborn Shieldmate");
    }

    @Test
    @DisplayName("A target leaving the graveyard does not cause another enchantment to be returned")
    void doesNotRetargetWhenTargetLeavesGraveyard() {
        NyxbornShieldmate target = new NyxbornShieldmate();
        AuraOfSilence other = new AuraOfSilence();
        harness.setGraveyard(player1, List.of(target, other));

        castGriffinDreamfinder();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of(other));
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotInHand(player1, "Nyxborn Shieldmate");
        harness.assertNotInHand(player1, "Aura of Silence");
        harness.assertInGraveyard(player1, "Aura of Silence");
    }
}
