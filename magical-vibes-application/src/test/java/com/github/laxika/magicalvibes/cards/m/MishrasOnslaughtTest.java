package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.ArgothianSprite;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MishrasOnslaught.class, ArgothianSprite.class})
class MishrasOnslaughtTest extends BaseCardTest {

    @Test
    @DisplayName("Token mode creates two 1/1 colorless Soldier artifact creature tokens")
    void tokenModeCreatesSoldiers() {
        cast(0);

        List<Permanent> soldiers = findPermanents(player1, "Soldier");
        assertThat(soldiers).hasSize(2);
        assertThat(soldiers).allSatisfy(soldier -> {
            assertThat(soldier.getCard().hasType(CardType.ARTIFACT)).isTrue();
            assertThat(soldier.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(soldier.getCard().getSubtypes()).containsExactly(CardSubtype.SOLDIER);
            assertThat(soldier.getCard().getColors()).isEmpty();
            assertThat(soldier.getCard().isToken()).isTrue();
            assertThat(soldier.isTapped()).isFalse();
            assertThat(soldier.getEffectivePower()).isEqualTo(1);
            assertThat(soldier.getEffectiveToughness()).isEqualTo(1);
        });
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Boost mode affects only your creatures until end of turn")
    void boostModeAffectsOwnCreaturesUntilEndOfTurn() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new ArgothianSprite());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new ArgothianSprite());

        cast(1);

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(2);
        assertThat(findPermanents(player1, "Soldier")).isEmpty();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost mode affects creatures present on resolution, including earlier Soldier tokens")
    void boostIncludesTokensAlreadyOnBattlefield() {
        cast(0);
        cast(1);

        assertThat(findPermanents(player1, "Soldier")).hasSize(2).allSatisfy(soldier -> {
            assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Creatures entering after boost resolution do not receive the boost")
    void laterTokensDoNotReceiveBoost() {
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new ArgothianSprite());
        cast(1);
        cast(0);

        assertThat(gqs.getEffectivePower(gd, existing)).isEqualTo(4);
        assertThat(findPermanents(player1, "Soldier")).hasSize(2).allSatisfy(soldier -> {
            assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Boost mode can resolve with no creatures and does not affect later creatures")
    void boostWithEmptyBattlefield() {
        cast(1);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        cast(0);

        assertThat(findPermanents(player1, "Soldier")).hasSize(2).allSatisfy(soldier ->
                assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(1));
    }

    private void cast(int mode) {
        harness.setHand(player1, List.of(new MishrasOnslaught()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castModalInstant(player1, 0, mode, List.of());
        harness.passBothPriorities();
    }
}
