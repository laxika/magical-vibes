package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DisruptingScepter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GrayOgre;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KinTreeSeverance.class, GrayOgre.class, GrizzlyBears.class, HillGiant.class, DisruptingScepter.class})
class KinTreeSeveranceTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles target permanent with mana value 3 or greater")
    void exilesTargetPermanentWithHighManaValue() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new KinTreeSeverance()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castAndResolveInstant(player1, 0, target.getId());

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

        harness.castAndResolveInstant(player1, 0, target.getId());

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

    @Test
    void exilesNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DisruptingScepter());
        harness.setHand(player1, List.of(new KinTreeSeverance()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(target.getCard());
    }

    @Test
    void canExileOwnPermanentWithColoredHybridPayment() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrayOgre());
        harness.setHand(player1, List.of(new KinTreeSeverance()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target.getCard());
    }

    @Test
    void cannotTargetFaceDownPermanentWithExpensiveFrontFace() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        target.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.setHand(player1, List.of(new KinTreeSeverance()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana value 3 or greater");
    }

    @Test
    void doesNotExileTargetThatTurnsFaceDownBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new KinTreeSeverance()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castInstant(player1, 0, target.getId());
        target.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(target.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof KinTreeSeverance);
        assertThat(gd.stack).isEmpty();
    }
}
