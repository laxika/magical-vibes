package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AjaniCallerOfThePrideTest extends BaseCardTest {

    @Nested
    @DisplayName("+1: +1/+1 counter on up to one target creature")
    class PlusOne {

        @Test
        @DisplayName("Puts a +1/+1 counter on the target creature")
        void putsCounterOnTarget() {
            Permanent ajani = addAjani(player1, 4);
            Permanent target = addCreature(player1, "GrizzlyBears", 2, 2);

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(ajani);
            harness.activateAbility(player1, idx, 0, null, target.getId());
            harness.passBothPriorities();

            assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
            assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
            assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        }

        @Test
        @DisplayName("Can be activated with no target")
        void canActivateWithNoTarget() {
            Permanent ajani = addAjani(player1, 4);

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(ajani);
            harness.activateAbility(player1, idx, 0, null, (UUID) null);
            harness.passBothPriorities();

            assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("-3: flying and double strike until end of turn")
    class MinusThree {

        @Test
        @DisplayName("Grants flying and double strike, which wear off at end of turn")
        void grantsKeywordsUntilEndOfTurn() {
            Permanent ajani = addAjani(player1, 4);
            Permanent target = addCreature(player1, "GrizzlyBears", 2, 2);

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(ajani);
            harness.activateAbility(player1, idx, 1, null, target.getId());
            harness.passBothPriorities();

            assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
            assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
            assertThat(gqs.hasKeyword(gd, target, Keyword.DOUBLE_STRIKE)).isTrue();

            harness.forceStep(TurnStep.END_STEP);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
            assertThat(gqs.hasKeyword(gd, target, Keyword.DOUBLE_STRIKE)).isFalse();
        }

        @Test
        @DisplayName("Cannot be activated with insufficient loyalty")
        void cannotActivateWithInsufficientLoyalty() {
            Permanent ajani = addAjani(player1, 2);
            Permanent target = addCreature(player1, "GrizzlyBears", 2, 2);

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(ajani);
            assertThatThrownBy(() -> harness.activateAbility(player1, idx, 1, null, target.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("-8: X 2/2 white Cats where X is your life total")
    class MinusEight {

        @Test
        @DisplayName("Creates one Cat token per point of life")
        void createsCatsEqualToLifeTotal() {
            Permanent ajani = addAjani(player1, 8);
            gd.playerLifeTotals.put(player1.getId(), 5);

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(ajani);
            harness.activateAbility(player1, idx, 2, null, (UUID) null);
            harness.passBothPriorities();

            long cats = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().isToken() && p.getCard().getSubtypes().contains(CardSubtype.CAT))
                    .count();
            assertThat(cats).isEqualTo(5);

            Permanent cat = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().isToken() && p.getCard().getSubtypes().contains(CardSubtype.CAT))
                    .findFirst().orElseThrow();
            assertThat(gqs.getEffectivePower(gd, cat)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, cat)).isEqualTo(2);
        }
    }

    private Permanent addAjani(Player player, int loyalty) {
        Permanent perm = new Permanent(new AjaniCallerOfThePride());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private Permanent addCreature(Player player, String name, int power, int toughness) {
        Card card = new Card() {};
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(power);
        card.setToughness(toughness);
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
