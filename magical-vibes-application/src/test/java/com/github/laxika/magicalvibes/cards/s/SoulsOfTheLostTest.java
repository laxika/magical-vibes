package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoulsOfTheLost.class, GrizzlyBears.class, Shock.class, Swamp.class})
class SoulsOfTheLostTest extends BaseCardTest {

    @Test
    @DisplayName("Can discard a card as its additional cost")
    void canDiscardAsAdditionalCost() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new SoulsOfTheLost(), new Shock()));
        addSoulsMana();

        harness.castSorceryWithDiscard(player1, 0, 1);
        harness.passBothPriorities();

        Permanent souls = findSouls(player1);
        assertThat(souls).isNotNull();
        assertThat(gqs.getEffectivePower(gd, souls)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, souls)).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Grizzly Bears", "Shock");
    }

    @Test
    @DisplayName("Can sacrifice a permanent as its additional cost")
    void canSacrificeAsAdditionalCost() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SoulsOfTheLost()));
        addSoulsMana();

        harness.castSorceryWithSacrifice(player1, 0, bears.getId());
        harness.passBothPriorities();

        Permanent souls = findSouls(player1);
        assertThat(souls).isNotNull();
        assertThat(gqs.getEffectivePower(gd, souls)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, souls)).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Grizzly Bears");
    }

    @Test
    void discardedPermanentCountsImmediately() {
        harness.setHand(player1, List.of(new SoulsOfTheLost(), new Swamp()));
        addSoulsMana();

        harness.castSorceryWithDiscard(player1, 0, 1);
        harness.assertInGraveyard(player1, "Swamp");
        harness.passBothPriorities();

        Permanent souls = findSouls(player1);
        assertThat(gqs.getEffectivePower(gd, souls)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, souls)).isEqualTo(2);
    }

    @Test
    void canSacrificeLand() {
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        harness.setHand(player1, List.of(new SoulsOfTheLost()));
        addSoulsMana();

        harness.castSorceryWithSacrifice(player1, 0, swamp.getId());
        harness.assertNotOnBattlefield(player1, "Swamp");
        harness.assertInGraveyard(player1, "Swamp");
        harness.passBothPriorities();

        Permanent souls = findSouls(player1);
        assertThat(gqs.getEffectivePower(gd, souls)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, souls)).isEqualTo(2);
    }

    @Test
    void countsOnlyOwnPermanentCardsAndUpdatesWithGraveyard() {
        Permanent souls = harness.addToBattlefieldAndReturn(player1, new SoulsOfTheLost());
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setGraveyard(player2, List.of(new SoulsOfTheLost(), new Swamp()));

        assertThat(gqs.getEffectivePower(gd, souls)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, souls)).isEqualTo(1);

        harness.setGraveyard(player1, List.of(new Shock(), new Swamp(), new SoulsOfTheLost()));
        assertThat(gqs.getEffectivePower(gd, souls)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, souls)).isEqualTo(3);

        harness.setGraveyard(player1, List.of());
        assertThat(gqs.getEffectivePower(gd, souls)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, souls)).isEqualTo(1);
    }

    @Test
    void characteristicAbilityWorksInGraveyardAndCountsItself() {
        SoulsOfTheLost souls = new SoulsOfTheLost();
        harness.setGraveyard(player1, List.of(souls, new Swamp()));
        harness.setGraveyard(player2, List.of(new Swamp()));

        assertThat(gqs.getEffectiveCardPower(gd, souls)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardToughness(gd, souls)).isEqualTo(3);
    }

    @Test
    void cannotCastWithoutPayingAdditionalCost() {
        harness.setHand(player1, List.of(new SoulsOfTheLost()));
        addSoulsMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Souls of the Lost");
    }

    @Test
    void cannotSacrificeOpponentsPermanent() {
        Permanent swamp = harness.addToBattlefieldAndReturn(player2, new Swamp());
        harness.setHand(player1, List.of(new SoulsOfTheLost()));
        addSoulsMana();

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, swamp.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Swamp");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotDiscardTheSpellToPayItsOwnCost() {
        harness.setHand(player1, List.of(new SoulsOfTheLost()));
        addSoulsMana();

        assertThatThrownBy(() -> harness.castSorceryWithDiscard(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    private void addSoulsMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private Permanent findSouls(Player player) {
        return gqs.findPermanentById(gd, harness.getPermanentId(player, "Souls of the Lost"));
    }
}
