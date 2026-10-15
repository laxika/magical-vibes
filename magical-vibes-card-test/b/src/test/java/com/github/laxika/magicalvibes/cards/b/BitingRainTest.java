package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.c.Catalog;
import com.github.laxika.magicalvibes.cards.d.DevilthornFox;
import com.github.laxika.magicalvibes.cards.h.HulkingDevil;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BitingRain.class, AvatarOfMight.class, Catalog.class, DevilthornFox.class, HulkingDevil.class})
class BitingRainTest extends BaseCardTest {

    @Test
    @DisplayName("Gives every creature -2/-2")
    void weakensAllCreatures() {
        Permanent ownCreature = addCreatureReady(player1, new AvatarOfMight());
        Permanent opposingCreature = addCreatureReady(player2, new AvatarOfMight());
        castBitingRain();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(6);
    }

    @Test
    @DisplayName("The -2/-2 wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent opposingCreature = addCreatureReady(player2, new AvatarOfMight());
        castBitingRain();

        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(8);
    }

    @Test
    @DisplayName("Creatures with zero or negative toughness die on both battlefields")
    void killsSmallCreaturesOnBothSides() {
        addCreatureReady(player1, new DevilthornFox());
        addCreatureReady(player2, new HulkingDevil());

        castBitingRain();

        harness.assertNotOnBattlefield(player1, "Devilthorn Fox");
        harness.assertInGraveyard(player1, "Devilthorn Fox");
        harness.assertNotOnBattlefield(player2, "Hulking Devil");
        harness.assertInGraveyard(player2, "Hulking Devil");
    }

    @Test
    @DisplayName("Creatures entering after resolution are unaffected")
    void doesNotAffectLaterCreatures() {
        castBitingRain();

        harness.castFromHand(player1, new HulkingDevil(), "{3}{R}");
        harness.passBothPriorities();

        Permanent devil = findPermanent(player1, "Hulking Devil");
        assertThat(devil).isNotNull();
        assertThat(gqs.getEffectivePower(gd, devil)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, devil)).isEqualTo(2);
    }

    @Test
    @DisplayName("Madness casts the sorcery during the opponent's end step for {2}{B}")
    void castsForMadnessOutsideSorceryTiming() {
        addCreatureReady(player2, new HulkingDevil());
        BitingRain rain = discardViaCatalog();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(rain);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Hulking Devil");
        harness.assertInGraveyard(player2, "Hulking Devil");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(rain);
    }

    @Test
    @DisplayName("Declining madness moves the discarded card to the graveyard without weakening creatures")
    void decliningMadnessDoesNotResolveSpell() {
        Permanent devil = addCreatureReady(player2, new HulkingDevil());
        BitingRain rain = discardViaCatalog();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(rain);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(rain);
        harness.assertOnBattlefield(player2, "Hulking Devil");
        assertThat(gqs.getEffectivePower(gd, devil)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, devil)).isEqualTo(2);
    }

    private BitingRain discardViaCatalog() {
        BitingRain rain = new BitingRain();
        harness.setHand(player1, List.of(new Catalog(), rain));
        harness.setLibrary(player1, List.of(new DevilthornFox(), new HulkingDevil()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(rain);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(rain);
        return rain;
    }

    private void castBitingRain() {
        harness.castFromHand(player1, new BitingRain(), "{2}{B}{B}");
        harness.passBothPriorities();
    }
}
