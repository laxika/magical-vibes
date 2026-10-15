package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({WeighDown.class, AirElemental.class, Forest.class, GrizzlyBears.class, Shock.class})
class WeighDownTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature card as an additional cost and gives the target creature -3/-3")
    void exilesCreatureAndShrinksTarget() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new WeighDown()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstantWithGraveyardExile(player1, 0, target.getId(), 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(1);

        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("The -3/-3 effect wears off at end of turn")
    void shrinkWearsOffAtEndOfTurn() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new WeighDown()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstantWithGraveyardExile(player1, 0, target.getId(), 0);
        harness.passBothPriorities();
        assertThat(target.getEffectiveToughness()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot cast without a creature card in the graveyard")
    void cannotCastWithoutCreatureCardInGraveyard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new WeighDown()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstantWithGraveyardExile(player1, 0, target.getId(), 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot exile a noncreature card as the additional cost")
    void cannotExileNoncreatureCard() {
        harness.setGraveyard(player1, List.of(new Shock()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new WeighDown()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstantWithGraveyardExile(player1, 0, target.getId(), 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new WeighDown()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstantWithGraveyardExile(
                player1, 0, harness.getPermanentId(player2, "Forest"), 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A creature reduced to negative toughness goes to its owner's graveyard")
    void killsSmallCreature() {
        GrizzlyBears cost = new GrizzlyBears();
        GrizzlyBears victim = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(cost));
        Permanent target = harness.addToBattlefieldAndReturn(player2, victim);
        harness.setHand(player1, List.of(new WeighDown()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstantWithGraveyardExile(player1, 0, target.getId(), 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(victim);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(cost);
    }

    @Test
    @DisplayName("Can target your own creature and exile the selected creature from a mixed graveyard")
    void targetsOwnCreatureAndExilesSelectedCard() {
        Shock otherCard = new Shock();
        GrizzlyBears cost = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(otherCard, cost));
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        harness.setHand(player1, List.of(new WeighDown()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstantWithGraveyardExile(player1, 0, target.getId(), 1);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(otherCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(cost);
        harness.passBothPriorities();
        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's graveyard cannot pay the additional cost")
    void cannotExileOpponentsCreature() {
        GrizzlyBears opponentsCard = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(opponentsCard));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new WeighDown()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstantWithGraveyardExile(player1, 0, target.getId(), 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentsCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
