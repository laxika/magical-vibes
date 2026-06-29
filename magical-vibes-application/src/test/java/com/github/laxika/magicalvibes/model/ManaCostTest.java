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
}
