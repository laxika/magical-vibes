package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GrayOgre;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KinTreeSeverance.class, GrayOgre.class, GrizzlyBears.class, HillGiant.class})
class KinTreeSeveranceTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles target permanent with mana value 3 or greater")
    void exilesTargetPermanentWithHighManaValue() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new KinTreeSeverance()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
    }

    @Test
    @DisplayName("Can target a permanent with mana value exactly 3 or greater")
    void acceptsManaValueBoundary() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrayOgre());
        harness.setHand(player1, List.of(new KinTreeSeverance()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Cannot target a permanent with mana value less than 3")
    void cannotTargetLowManaValuePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new KinTreeSeverance()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana value 3 or greater");
    }
}
