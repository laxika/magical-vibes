package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({DennickPiousApprentice.class, DennickPiousApparition.class, Disentomb.class,
        GrizzlyBears.class, Shock.class})
class DennickPiousApprenticeTest extends BaseCardTest {

    @Test
    @DisplayName("Disturb casts Dennick from the graveyard transformed")
    void disturbEntersTransformed() {
        Permanent dennick = castWithDisturb();

        assertThat(dennick.isTransformed()).isTrue();
        assertThat(dennick.getCard().getName()).isEqualTo("Dennick, Pious Apparition");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Dennick's back face is exiled instead of going to the graveyard")
    void backFaceIsExiledInsteadOfGraveyard() {
        Permanent dennick = castWithDisturb();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, dennick));

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(exiled -> exiled.card().getId()))
                .contains(dennick.getOriginalCard().getId());
    }

    @Test
    @DisplayName("Dennick's front face prevents graveyard targets")
    void frontFacePreventsGraveyardTargets() {
        Card creature = new GrizzlyBears();
        harness.addToBattlefield(player1, new DennickPiousApprentice());
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new Disentomb()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Back face investigates for an opponent creature card once each turn")
    void investigatesForOpponentCreatureCardOnceEachTurn() {
        Permanent dennick = castWithDisturb();
        Permanent firstBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, firstBear.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);

        harness.castInstant(player1, 0, secondBear.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(dennick.isTransformed()).isTrue();
    }

    private Permanent castWithDisturb() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new DennickPiousApprentice()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }
}
