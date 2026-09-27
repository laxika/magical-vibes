package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GoblinSpy;
import com.github.laxika.magicalvibes.cards.n.NomadicElf;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RubyLeech.class, GoblinSpy.class, NomadicElf.class})
class RubyLeechTest extends BaseCardTest {

    @Nested
    @DisplayName("Red spells you cast cost more")
    class OwnRedSpellsTaxed {

        @Test
        @DisplayName("A red spell cannot be cast without the extra mana")
        void redSpellCostsMore() {
            harness.addToBattlefield(player1, new RubyLeech());

            assertThatThrownBy(() -> harness.castFromHand(player1, new GoblinSpy(), "{R}"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }

        @Test
        @DisplayName("A red spell is castable with one extra mana")
        void redSpellCastableWithTax() {
            harness.addToBattlefield(player1, new RubyLeech());

            harness.castFromHand(player1, new GoblinSpy(), "{R}{R}");

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("The additional red mana cannot be paid with colorless mana")
        void redSpellNeedsRedTax() {
            harness.addToBattlefield(player1, new RubyLeech());

            assertThatThrownBy(() -> harness.castFromHand(player1, new GoblinSpy(), "{1}{R}"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }
    }

    @Nested
    @DisplayName("Only the controller's red spells are taxed")
    class OpponentAndNonRedNotTaxed {

        @Test
        @DisplayName("A non-red spell cast by the controller is not taxed")
        void nonRedSpellNotAffected() {
            harness.addToBattlefield(player1, new RubyLeech());

            harness.castFromHand(player1, new NomadicElf(), "{1}{G}");

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("An opponent's red spell is not taxed")
        void opponentRedSpellNotAffected() {
            harness.addToBattlefield(player1, new RubyLeech());

            harness.forceActivePlayer(player2);

            harness.castFromHand(player2, new GoblinSpy(), "{R}");

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(0);
        }
    }

    @Test
    @DisplayName("First strike defeats a 2/2 blocker before it deals combat damage")
    void firstStrikeKillsBlockerBeforeItDealsCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new RubyLeech());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new NomadicElf());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertOnBattlefield(player1, "Ruby Leech");
        harness.assertInGraveyard(player2, "Nomadic Elf");
    }
}
