package com.github.laxika.magicalvibes.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ManaCost}, focused on correctly handling multi-{X} costs
 * such as White Sun's Zenith ({X}{X}{X}{W}), where the chosen X is multiplied by
 * the number of {X} symbols to determine the actual generic mana that must be paid.
 */
class ManaCostTest {

    @Nested
    @DisplayName("X-symbol counting")
    class XSymbolCounting {

        @Test
        void costWithoutXReportsZeroXSymbols() {
            ManaCost cost = new ManaCost("{2}{W}{W}");
            assertThat(cost.hasX()).isFalse();
            assertThat(cost.getXSymbolCount()).isZero();
        }

        @Test
        void singleXReportsOneXSymbol() {
            ManaCost cost = new ManaCost("{X}{R}");
            assertThat(cost.hasX()).isTrue();
            assertThat(cost.getXSymbolCount()).isEqualTo(1);
        }

        @Test
        void doubleXReportsTwoXSymbols() {
            ManaCost cost = new ManaCost("{X}{X}{R}");
            assertThat(cost.hasX()).isTrue();
            assertThat(cost.getXSymbolCount()).isEqualTo(2);
        }

        @Test
        void whiteSunsZenithReportsThreeXSymbols() {
            ManaCost cost = new ManaCost("{X}{X}{X}{W}");
            assertThat(cost.hasX()).isTrue();
            assertThat(cost.getXSymbolCount()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("canPay for X costs")
    class CanPayForXCosts {

        @Test
        void singleXRequiresOneManaPerX() {
            ManaCost cost = new ManaCost("{X}{R}");
            ManaPool pool = new ManaPool();
            pool.add(ManaColor.RED, 4); // 3 for X=3 plus 1 for the {R}
            assertThat(cost.canPay(pool, 3)).isTrue();
        }

        @Test
        void whiteSunsZenithPayingX3RequiresTenMana() {
            // {X}{X}{X}{W} at X=3 costs 9 generic + 1 white = 10 total.
            ManaCost cost = new ManaCost("{X}{X}{X}{W}");
            ManaPool pool = new ManaPool();
            pool.add(ManaColor.WHITE, 10);
            assertThat(cost.canPay(pool, 3)).isTrue();
        }

        @Test
        void whiteSunsZenithPayingX3WithNineManaIsInsufficient() {
            // Regression: prior to the fix this returned true because only
            // xValue (not xValue * 3) was added to the generic requirement.
            ManaCost cost = new ManaCost("{X}{X}{X}{W}");
            ManaPool pool = new ManaPool();
            pool.add(ManaColor.WHITE, 9);
            assertThat(cost.canPay(pool, 3)).isFalse();
        }

        @Test
        void doubleXPayingX2RequiresFourManaPlusColoredCost() {
            ManaCost cost = new ManaCost("{X}{X}{R}");
            ManaPool pool = new ManaPool();
            pool.add(ManaColor.RED, 5); // 4 for X=2 plus 1 for the {R}
            assertThat(cost.canPay(pool, 2)).isTrue();

            ManaPool underfunded = new ManaPool();
            underfunded.add(ManaColor.RED, 4);
            assertThat(cost.canPay(underfunded, 2)).isFalse();
        }
    }

    @Nested
    @DisplayName("pay drains the correct amount")
    class PayDrainsCorrectAmount {

        @Test
        void whiteSunsZenithPayingX3DrainsTenMana() {
            ManaCost cost = new ManaCost("{X}{X}{X}{W}");
            ManaPool pool = new ManaPool();
            pool.add(ManaColor.WHITE, 10);

            cost.pay(pool, 3);

            assertThat(pool.getTotal()).isZero();
        }

        @Test
        void singleXPayingX3DrainsFourMana() {
            ManaCost cost = new ManaCost("{X}{R}");
            ManaPool pool = new ManaPool();
            pool.add(ManaColor.RED, 10);

            cost.pay(pool, 3);

            // 1 colored + 3 generic consumed, 6 remain.
            assertThat(pool.getTotal()).isEqualTo(6);
        }

        @Test
        void doubleXPayingX2DrainsFiveMana() {
            ManaCost cost = new ManaCost("{X}{X}{R}");
            ManaPool pool = new ManaPool();
            pool.add(ManaColor.RED, 10);

            cost.pay(pool, 2);

            // 1 colored + 4 generic consumed, 5 remain.
            assertThat(pool.getTotal()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("calculateMaxX respects X-symbol count")
    class CalculateMaxXRespectsXSymbolCount {

        @Test
        void costWithoutXReturnsZero() {
            ManaCost cost = new ManaCost("{2}{W}");
            ManaPool pool = new ManaPool();
            pool.add(ManaColor.WHITE, 10);
            assertThat(cost.calculateMaxX(pool)).isZero();
        }

        @Test
        void singleXMaxXIsRemainingMana() {
            ManaCost cost = new ManaCost("{X}{R}");
            ManaPool pool = new ManaPool();
            pool.add(ManaColor.RED, 6);
            // 1 mana goes to {R}, 5 remain for X.
            assertThat(cost.calculateMaxX(pool)).isEqualTo(5);
        }

        @Test
        void whiteSunsZenithWithTenWhiteMaxXIsThree() {
            // Prior to the fix this returned 9 (because X was counted once).
            ManaCost cost = new ManaCost("{X}{X}{X}{W}");
            ManaPool pool = new ManaPool();
            pool.add(ManaColor.WHITE, 10);
            assertThat(cost.calculateMaxX(pool)).isEqualTo(3);
        }

        @Test
        void doubleXWithFiveRedMaxXIsTwo() {
            // {X}{X}{R} with 5 red: 1 for {R}, 4 remain / 2 = maxX=2.
            ManaCost cost = new ManaCost("{X}{X}{R}");
            ManaPool pool = new ManaPool();
            pool.add(ManaColor.RED, 5);
            assertThat(cost.calculateMaxX(pool)).isEqualTo(2);
        }

        @Test
        void tripleXWithSevenColorlessMaxXIsTwo() {
            // {X}{X}{X}{1} with 7 colorless: 1 generic + 6 for X, 6/3 = 2.
            ManaCost cost = new ManaCost("{X}{X}{X}{1}");
            ManaPool pool = new ManaPool();
            pool.add(ManaColor.COLORLESS, 7);
            assertThat(cost.calculateMaxX(pool)).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("flashback multi-X")
    class FlashbackMultiX {

        @Test
        void flashbackDoubleXPayingX2DrainsFiveFromRegularPool() {
            ManaCost cost = new ManaCost("{X}{X}{R}");
            ManaPool pool = new ManaPool();
            pool.add(ManaColor.RED, 6);

            assertThat(cost.canPayFlashback(pool, 2)).isTrue();
            cost.payFlashback(pool, 2);
            assertThat(pool.getTotal()).isEqualTo(1);
        }

        @Test
        void flashbackDoubleXPayingX2WithFourTotalIsInsufficient() {
            ManaCost cost = new ManaCost("{X}{X}{R}");
            ManaPool pool = new ManaPool();
            pool.add(ManaColor.RED, 4);
            assertThat(cost.canPayFlashback(pool, 2)).isFalse();
        }
    }

    @Nested
    @DisplayName("xColorRestriction multi-X")
    class XColorRestrictionMultiX {

        @Test
        void xColorRestrictedDoubleXPayingX2DrainsFourOfRestrictedColor() {
            ManaCost cost = new ManaCost("{X}{X}");
            ManaPool pool = new ManaPool();
            pool.add(ManaColor.BLACK, 4);

            assertThat(cost.canPay(pool, 2, ManaColor.BLACK, 0)).isTrue();
            cost.pay(pool, 2, ManaColor.BLACK, 0);
            assertThat(pool.getTotal()).isZero();
        }

        @Test
        void xColorRestrictedDoubleXPayingX2WithThreeBlackIsInsufficient() {
            ManaCost cost = new ManaCost("{X}{X}");
            ManaPool pool = new ManaPool();
            pool.add(ManaColor.BLACK, 3);
            assertThat(cost.canPay(pool, 2, ManaColor.BLACK, 0)).isFalse();
        }

        @Test
        void xColorRestrictedDoubleXCalculateMaxXHalvesAvailableMana() {
            // Prior to the fix this returned the full restricted total.
            ManaCost cost = new ManaCost("{X}{X}");
            ManaPool pool = new ManaPool();
            pool.add(ManaColor.BLACK, 6);
            assertThat(cost.calculateMaxX(pool, ManaColor.BLACK, 0)).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("hybrid mana")
    class HybridMana {

        @Test
        void colorHybridManaValueCountsAsOne() {
            // {1}{W/B} — generic 1 + one color-hybrid symbol = mana value 2.
            assertThat(new ManaCost("{1}{W/B}").getManaValue()).isEqualTo(2);
        }

        @Test
        void monocoloredHybridManaValueUsesGenericSide() {
            // CR 202.3f: the mana value of {2/W} is 2.
            assertThat(new ManaCost("{2/W}").getManaValue()).isEqualTo(2);
        }

        @Test
        void colorHybridPaidWithEitherColor() {
            ManaCost cost = new ManaCost("{1}{W/B}");

            ManaPool white = new ManaPool();
            white.add(ManaColor.WHITE, 2);
            assertThat(cost.canPay(white, 0)).isTrue();

            ManaPool black = new ManaPool();
            black.add(ManaColor.WHITE, 1);
            black.add(ManaColor.BLACK, 1);
            assertThat(cost.canPay(black, 0)).isTrue();
        }

        @Test
        void colorHybridCannotBePaidWithUnlistedColor() {
            // {W/B} must be paid with white or black — red can only cover the {1}.
            ManaCost cost = new ManaCost("{1}{W/B}");
            ManaPool pool = new ManaPool();
            pool.add(ManaColor.RED, 2);
            assertThat(cost.canPay(pool, 0)).isFalse();
        }

        @Test
        void colorHybridDrainsExactlyTheCost() {
            ManaCost cost = new ManaCost("{1}{W/B}");
            ManaPool pool = new ManaPool();
            pool.add(ManaColor.WHITE, 1);
            pool.add(ManaColor.BLACK, 1);

            cost.pay(pool, 0);

            // One mana for {W/B}, one for {1}: pool fully drained.
            assertThat(pool.getTotal()).isZero();
        }

        @Test
        void monocoloredHybridPaidWithGenericWhenColorUnavailable() {
            // {2/W} with no white: pay the 2-generic alternative.
            ManaCost cost = new ManaCost("{2/W}");
            ManaPool pool = new ManaPool();
            pool.add(ManaColor.COLORLESS, 2);
            assertThat(cost.canPay(pool, 0)).isTrue();
            cost.pay(pool, 0);
            assertThat(pool.getTotal()).isZero();
        }

        @Test
        void monocoloredHybridPaidWithSingleColoredMana() {
            // {2/W} with one white: pay the cheaper colored side.
            ManaCost cost = new ManaCost("{2/W}");
            ManaPool pool = new ManaPool();
            pool.add(ManaColor.WHITE, 1);
            assertThat(cost.canPay(pool, 0)).isTrue();
            cost.pay(pool, 0);
            assertThat(pool.getTotal()).isZero();
        }

        @Test
        @DisplayName("Hybrid pip cannot reuse mana already needed for a fixed colored pip")
        void hybridPipCannotReuseManaClaimedByColoredPip() {
            // {U}{U/R}{R} with exactly U + R + G: the U and R are consumed by the fixed pips,
            // leaving only G, which cannot pay the {U/R} hybrid. Must be unpayable.
            ManaCost cost = new ManaCost("{U}{U/R}{R}");

            ManaPool pool = new ManaPool();
            pool.add(ManaColor.BLUE, 1);
            pool.add(ManaColor.RED, 1);
            pool.add(ManaColor.GREEN, 1);

            assertThat(cost.canPay(pool, 0)).isFalse();
        }

        @Test
        @DisplayName("Paying a cost with a fixed and a hybrid pip spends exactly the right mana")
        void payingHybridCostSpendsTheRightMana() {
            ManaCost cost = new ManaCost("{U}{U/R}{R}");

            ManaPool pool = new ManaPool();
            pool.add(ManaColor.BLUE, 2);
            pool.add(ManaColor.RED, 1);

            assertThat(cost.canPay(pool, 0)).isTrue();
            cost.pay(pool, 0);

            // {U} from a blue, {U/R} from the other blue, {R} from the red — pool fully drained.
            assertThat(pool.getTotal()).isZero();
        }

        @Test
        @DisplayName("Two hybrid pips competing for a single color are both satisfied")
        void competingHybridPipsAreBothSatisfied() {
            // {U/G}{U/R} with U + G: a naive assignment of U to the first pip would strand the
            // second; a correct assignment gives G to the first pip and U to the second.
            ManaCost cost = new ManaCost("{U/G}{U/R}");

            ManaPool pool = new ManaPool();
            pool.add(ManaColor.BLUE, 1);
            pool.add(ManaColor.GREEN, 1);

            assertThat(cost.canPay(pool, 0)).isTrue();
            cost.pay(pool, 0);
            assertThat(pool.getTotal()).isZero();
        }
    }
}
