package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(NamoraTheSeaQueen.class)
class NamoraTheSeaQueenTest extends BaseCardTest {

    @Test
    @DisplayName("Power-up is discounted during the entry turn and creates two Merfolk")
    void powerUpIsDiscountedAndCreatesTwoMerfolkDuringEntryTurn() {
        Permanent namora = harness.enterBattlefieldAndReturn(player1, new NamoraTheSeaQueen());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(namora.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLUE);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.MERFOLK);
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Power-up costs its full activation cost after the entry turn")
    void powerUpIsNotDiscountedAfterEntryTurn() {
        Permanent namora = addCreatureReady(player1, new NamoraTheSeaQueen());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(namora.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Power-up can be activated only once")
    void powerUpCanBeActivatedOnlyOnce() {
        addCreatureReady(player1, new NamoraTheSeaQueen());
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    @Test
    @DisplayName("An unsuccessful payment does not consume the power-up activation")
    void insufficientEntryTurnManaDoesNotConsumeActivation() {
        Permanent namora = harness.enterBattlefieldAndReturn(player1, new NamoraTheSeaQueen());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(namora.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(countPermanents(player1, "Merfolk")).isEqualTo(2);
    }

    @Test
    @DisplayName("The full power-up cost requires blue mana after the entry turn")
    void fullCostCannotBePaidWithOnlyColorlessMana() {
        addCreatureReady(player1, new NamoraTheSeaQueen());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Merfolk")).isZero();
    }

    @Test
    @DisplayName("Power-up can be activated while Namora is tapped and summoning sick")
    void powerUpDoesNotRequireTappingOrHaste() {
        Permanent namora = harness.enterBattlefieldAndReturn(player1, new NamoraTheSeaQueen());
        namora.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(namora.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(namora.isTapped()).isTrue();
        assertThat(countPermanents(player1, "Merfolk")).isEqualTo(2);
    }

    @Test
    @DisplayName("Power-up cannot be activated again while its first activation is on the stack")
    void powerUpLimitAppliesBeforeResolution() {
        harness.enterBattlefieldAndReturn(player1, new NamoraTheSeaQueen());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Merfolk")).isEqualTo(2);
    }

    @Test
    @DisplayName("Power-up still creates Merfolk if Namora leaves before resolution")
    void createsTokensWhenSourceHasLeftBattlefield() {
        Permanent namora = harness.enterBattlefieldAndReturn(player1, new NamoraTheSeaQueen());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).remove(namora);
        gd.playerGraveyards.get(player1.getId()).add(namora.getCard());
        harness.passBothPriorities();

        assertThat(namora.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(countPermanents(player1, "Merfolk")).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }
}
