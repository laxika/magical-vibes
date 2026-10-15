package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.o.OvergrownBattlement;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MomentousFall.class, GiantSpider.class, OvergrownBattlement.class})
class MomentousFallTest extends BaseCardTest {

    @Test
    @DisplayName("Draws cards for power and gains life for toughness of the sacrificed creature")
    void usesSacrificedPowerAndToughnessSeparately() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GiantSpider());

        harness.setHand(player1, List.of(new MomentousFall()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int lifeBefore = gd.getLife(player1.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int libraryBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castInstantWithSacrifice(player1, 0, null, sacrifice.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 4);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore - 1 + 2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore - 2);
        harness.assertInGraveyard(player1, "Giant Spider");
    }

    @Test
    @DisplayName("Cannot cast without a creature to sacrifice")
    void cannotCastWithoutCreatureToSacrifice() {
        harness.setHand(player1, List.of(new MomentousFall()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    void zeroPowerStillGainsLifeFromToughness() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new OvergrownBattlement());
        harness.setHand(player1, List.of(new MomentousFall()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        int lifeBefore = gd.getLife(player1.getId());
        int libraryBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castInstantWithSacrifice(player1, 0, null, sacrifice.getId());
        harness.assertInGraveyard(player1, "Overgrown Battlement");
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore);
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore + 4);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore);
    }

    @Test
    void usesModifiedStatsBeforeSacrifice() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new OvergrownBattlement());
        sacrifice.setPowerModifier(3);
        sacrifice.setToughnessModifier(2);
        harness.setHand(player1, List.of(new MomentousFall()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        int lifeBefore = gd.getLife(player1.getId());
        int libraryBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castInstantWithSacrifice(player1, 0, null, sacrifice.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore + 6);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore - 3);
    }

    @Test
    void negativePowerDrawsNoCardsButStillGainsLife() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new OvergrownBattlement());
        sacrifice.setPowerModifier(-2);
        harness.setHand(player1, List.of(new MomentousFall()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        int lifeBefore = gd.getLife(player1.getId());
        int libraryBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castInstantWithSacrifice(player1, 0, null, sacrifice.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore + 4);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore);
    }

    @Test
    void cannotSacrificeOpponentsCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new OvergrownBattlement());
        harness.addToBattlefield(player1, new OvergrownBattlement());
        harness.setHand(player1, List.of(new MomentousFall()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentCreature);
    }
}
