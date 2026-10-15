package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TwinmawStormbrood.class, GrizzlyBears.class, ShivanDragon.class})
class TwinmawStormbroodTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gains 5 life")
    void etbGainsLife() {
        harness.setHand(player1, List.of(new TwinmawStormbrood()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(25);
        harness.assertOnBattlefield(player1, "Twinmaw Stormbrood");
    }

    @Test
    @DisplayName("Omen deals 5 damage to a creature without flying and shuffles into its owner's library")
    void omenDamagesNonFlyingCreatureAndShuffles() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        TwinmawStormbrood card = new TwinmawStormbrood();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).contains(card);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(card);
    }

    @Test
    @DisplayName("Omen cannot target a creature with flying")
    void omenRejectsFlyingCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ShivanDragon());
        harness.setHand(player1, List.of(new TwinmawStormbrood()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature without flying");
    }

    @Test
    void omenDealsExactlyFiveDamageToOwnCreatureWithoutGainingLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        TwinmawStormbrood card = new TwinmawStormbrood();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(target.getMarkedDamage()).isEqualTo(5);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.playerDecks.get(player1.getId())).containsOnlyOnce(card);
        harness.assertNotOnBattlefield(player1, "Twinmaw Stormbrood");
        harness.assertNotInGraveyard(player1, "Twinmaw Stormbrood");
    }

    @Test
    void omenDoesNotResolveOrShuffleWhenTargetGainsFlying() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        TwinmawStormbrood card = new TwinmawStormbrood();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, target.getId());
        target.getGrantedKeywords().add(Keyword.FLYING);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(card);
    }

    @Test
    void omenDoesNotShuffleWhenTargetLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        TwinmawStormbrood card = new TwinmawStormbrood();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setGraveyard(player2, List.of(target.getCard()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(card);
    }

    @Test
    void omenCannotTargetAPlayer() {
        harness.setHand(player1, List.of(new TwinmawStormbrood()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void omenDoesNotResolveWhenTargetGainsProtectionFromRed() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        TwinmawStormbrood card = new TwinmawStormbrood();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, target.getId());
        target.getProtectionFromColorsUntilEndOfTurn().add(CardColor.RED);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(card);
    }

    @Test
    void omenStillResolvesWhenTargetGainsProtectionFromWhite() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        TwinmawStormbrood card = new TwinmawStormbrood();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, target.getId());
        target.getProtectionFromColorsUntilEndOfTurn().add(CardColor.WHITE);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).containsOnlyOnce(card);
        harness.assertNotInGraveyard(player1, "Twinmaw Stormbrood");
    }
}
