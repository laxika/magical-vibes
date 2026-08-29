package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SanctifierOfSoulsTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 until end of turn when another creature you control enters")
    void getsBoostWhenAllyCreatureEnters() {
        harness.addToBattlefield(player1, new SanctifierOfSouls());
        Permanent sanctifier = gd.playerBattlefields.get(player1.getId()).getFirst();

        castGrizzlyBears(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, sanctifier)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sanctifier)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not trigger when an opponent's creature enters")
    void noBoostWhenOpponentCreatureEnters() {
        harness.addToBattlefield(player1, new SanctifierOfSouls());
        Permanent sanctifier = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        castGrizzlyBears(player2);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, sanctifier)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sanctifier)).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new SanctifierOfSouls());
        Permanent sanctifier = gd.playerBattlefields.get(player1.getId()).getFirst();

        castGrizzlyBears(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, sanctifier)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, sanctifier)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sanctifier)).isEqualTo(3);
    }

    @Test
    @DisplayName("Exiles a creature card and creates an untapped 1/1 white Spirit with flying")
    void exilesCreatureAndCreatesSpiritToken() {
        harness.addToBattlefield(player1, new SanctifierOfSouls());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));

        harness.passBothPriorities();

        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(spirit.getCard().getPower()).isEqualTo(1);
        assertThat(spirit.getCard().getToughness()).isEqualTo(1);
        assertThat(spirit.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(spirit.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);
        assertThat(spirit.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(spirit.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without a creature card in the graveyard")
    void cannotActivateWithoutCreatureInGraveyard() {
        harness.addToBattlefield(player1, new SanctifierOfSouls());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    private void castGrizzlyBears(Player player) {
        harness.setHand(player, List.of(new GrizzlyBears()));
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.castCreature(player, 0);
    }
}
