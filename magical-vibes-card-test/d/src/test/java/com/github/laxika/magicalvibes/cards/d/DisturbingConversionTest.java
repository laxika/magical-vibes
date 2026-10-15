package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DisturbingConversion.class, Forest.class, DregRecycler.class})
class DisturbingConversionTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, each player mills two cards")
    void eachPlayerMillsTwoCards() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new DregRecycler());
        int player1DeckBefore = gd.playerDecks.get(player1.getId()).size();
        int player2DeckBefore = gd.playerDecks.get(player2.getId()).size();

        castAura(player1, bears.getId());

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(player1DeckBefore - 2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(player2DeckBefore - 2);
    }

    @Test
    @DisplayName("Enchanted creature gets -1/-0 for each card in its controller's graveyard")
    void debuffUsesEnchantedCreatureControllersGraveyard() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new DregRecycler());
        harness.setGraveyard(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setGraveyard(player2, List.of(new Forest()));

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new DisturbingConversion());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Debuff updates when the enchanted creature controller's graveyard changes")
    void debuffUpdatesDynamically() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new DregRecycler());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new DisturbingConversion());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);

        harness.setGraveyard(player1, List.of(new Forest(), new Forest()));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new DisturbingConversion()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Milling waits for the enter trigger to resolve")
    void millingIsASeparateTriggeredAbility() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DregRecycler());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());
        harness.setHand(player1, List.of(new DisturbingConversion()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, creature.getId());

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Disturbing Conversion");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gqs.getEffectivePower(gd, creature)).isZero();
    }

    @Test
    @DisplayName("Flash allows casting during the opponent's combat phase")
    void canCastDuringOpponentsCombat() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DregRecycler());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        castAura(player1, creature.getId());

        harness.assertOnBattlefield(player1, "Disturbing Conversion");
        assertThat(gqs.getEffectivePower(gd, creature)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Each player mills only the cards remaining in their library")
    void millsShortAndEmptyLibraries() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DregRecycler());
        Forest lastCard = new Forest();
        harness.setLibrary(player1, List.of(lastCard));
        harness.setLibrary(player2, List.of());
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());

        castAura(player1, creature.getId());

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(lastCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Power can become negative without reducing toughness")
    void powerCanBecomeNegative() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DregRecycler());
        harness.setGraveyard(player2, List.of(new Forest(), new Forest(), new Forest()));

        castAura(player1, creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(-3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Dreg Recycler");
    }

    @Test
    @DisplayName("Removing graveyard cards immediately reduces the power penalty")
    void debuffShrinksWhenGraveyardEmpties() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DregRecycler());
        castAura(player1, creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isZero();

        harness.setGraveyard(player2, List.of());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    private void castAura(com.github.laxika.magicalvibes.model.Player player, java.util.UUID targetId) {
        harness.setHand(player, List.of(new DisturbingConversion()));
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
