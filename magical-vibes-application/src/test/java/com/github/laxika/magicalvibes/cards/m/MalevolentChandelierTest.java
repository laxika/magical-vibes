package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MalevolentChandelier.class, GrizzlyBears.class})
class MalevolentChandelierTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a target card from your graveyard on the bottom of its owner's library")
    void putsOwnGraveyardCardOnLibraryBottom() {
        Card target = new GrizzlyBears();
        Card existingTop = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.setLibrary(player1, List.of(existingTop));
        Permanent chandelier = addReadyChandelier();

        activate(chandelier, target);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(existingTop, target);
    }

    @Test
    @DisplayName("Puts a target card from an opponent's graveyard on the bottom of its owner's library")
    void putsOpponentGraveyardCardOnOwnerLibraryBottom() {
        Card target = new GrizzlyBears();
        Card existingTop = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        harness.setLibrary(player2, List.of(existingTop));
        Permanent chandelier = addReadyChandelier();

        activate(chandelier, target);

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(existingTop, target);
    }

    @Test
    @DisplayName("Can only be activated as a sorcery")
    void requiresSorcerySpeed() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        Permanent chandelier = addReadyChandelier();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(chandelier),
                0,
                target.getId(),
                Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyChandelier() {
        Permanent chandelier = harness.addToBattlefieldAndReturn(player1, new MalevolentChandelier());
        chandelier.setSummoningSick(false);
        return chandelier;
    }

    private void activate(Permanent chandelier, Card target) {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(chandelier),
                0,
                target.getId(),
                Zone.GRAVEYARD);
        harness.passBothPriorities();
    }
}
