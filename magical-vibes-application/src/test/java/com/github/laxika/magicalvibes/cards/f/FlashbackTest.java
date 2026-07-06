package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.GrantFlashbackToTargetGraveyardCardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FlashbackTest extends BaseCardTest {

    

    @Test
    @DisplayName("Resolving grants flashback to target instant in graveyard")
    void grantsFlashbackToTargetInstant() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.setHand(player1, List.of(new Flashback()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, shock.getId());
        harness.passBothPriorities();

        assertThat(gd.cardsGrantedFlashbackUntilEndOfTurn).contains(shock.getId());
    }

    @Test
    @DisplayName("Cannot target creature card in graveyard")
    void cannotTargetCreatureInGraveyard() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new Flashback()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Granted flashback allows casting the targeted spell")
    void grantedFlashbackAllowsCasting() {
        Shock shock = new Shock();
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(shock));
        harness.setHand(player1, List.of(new Flashback()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, shock.getId());
        harness.passBothPriorities();

        harness.castFlashback(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(2);
    }
}
