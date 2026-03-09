package com.github.laxika.magicalvibes.service.turn;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JinGitaxiasCoreAugur;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.u.Upwelling;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TurnCleanupServiceTest extends BaseCardTest {

    private Permanent addAndGetPermanent(com.github.laxika.magicalvibes.model.Player player,
                                         com.github.laxika.magicalvibes.model.Card card) {
        harness.addToBattlefield(player, card);
        List<Permanent> bf = gd.playerBattlefields.get(player.getId());
        return bf.get(bf.size() - 1);
    }

    @Nested
    @DisplayName("resetEndOfTurnModifiers")
    class ResetEndOfTurnModifiers {

        @Test
        @DisplayName("Clears power and toughness modifiers on permanents")
        void clearsPowerToughnessModifiers() {
            Permanent perm = addAndGetPermanent(player1, new GrizzlyBears());
            perm.setPowerModifier(3);
            perm.setToughnessModifier(2);

            TurnCleanupService svc = new TurnCleanupService(null);
            svc.resetEndOfTurnModifiers(gd);

            assertThat(perm.getPowerModifier()).isZero();
            assertThat(perm.getToughnessModifier()).isZero();
        }

        @Test
        @DisplayName("Clears damage prevention and regeneration shields on permanents")
        void clearsDamagePreventionAndRegenerationShields() {
            Permanent perm = addAndGetPermanent(player1, new GrizzlyBears());
            perm.setDamagePreventionShield(5);
            perm.setRegenerationShield(2);

            TurnCleanupService svc = new TurnCleanupService(null);
            svc.resetEndOfTurnModifiers(gd);

            assertThat(perm.getDamagePreventionShield()).isZero();
            assertThat(perm.getRegenerationShield()).isZero();
        }

        @Test
        @DisplayName("Clears global damage prevention flags")
        void clearsGlobalDamagePreventionFlags() {
            gd.globalDamagePreventionShield = 10;
            gd.preventAllCombatDamage = true;
            gd.allPermanentsEnterTappedThisTurn = true;

            TurnCleanupService svc = new TurnCleanupService(null);
            svc.resetEndOfTurnModifiers(gd);

            assertThat(gd.globalDamagePreventionShield).isZero();
            assertThat(gd.preventAllCombatDamage).isFalse();
            assertThat(gd.allPermanentsEnterTappedThisTurn).isFalse();
        }

        @Test
        @DisplayName("Does not affect permanents without modifiers")
        void doesNotAffectUnmodifiedPermanents() {
            Permanent perm = addAndGetPermanent(player1, new GrizzlyBears());

            TurnCleanupService svc = new TurnCleanupService(null);
            svc.resetEndOfTurnModifiers(gd);

            assertThat(perm.getPowerModifier()).isZero();
            assertThat(perm.getToughnessModifier()).isZero();
        }
    }

    @Nested
    @DisplayName("drainManaPools")
    class DrainManaPools {

        @Test
        @DisplayName("Empties all players' mana pools")
        void emptiesAllManaPools() {
            harness.addMana(player1, ManaColor.RED, 3);
            harness.addMana(player2, ManaColor.BLUE, 2);

            TurnCleanupService svc = new TurnCleanupService(null);
            svc.drainManaPools(gd);

            ManaPool pool1 = gd.playerManaPools.get(player1.getId());
            ManaPool pool2 = gd.playerManaPools.get(player2.getId());
            assertThat(pool1.getTotal()).isZero();
            assertThat(pool2.getTotal()).isZero();
        }

        @Test
        @DisplayName("Does not drain mana pools when Upwelling is on the battlefield")
        void doesNotDrainWhenUpwellingPresent() {
            harness.addMana(player1, ManaColor.GREEN, 5);
            harness.addToBattlefield(player2, new Upwelling());

            TurnCleanupService svc = new TurnCleanupService(null);
            svc.drainManaPools(gd);

            ManaPool pool1 = gd.playerManaPools.get(player1.getId());
            assertThat(pool1.get(ManaColor.GREEN)).isEqualTo(5);
        }

        @Test
        @DisplayName("Does nothing when mana pools are already empty")
        void doesNothingWhenAlreadyEmpty() {
            TurnCleanupService svc = new TurnCleanupService(null);
            svc.drainManaPools(gd);

            ManaPool pool1 = gd.playerManaPools.get(player1.getId());
            assertThat(pool1.getTotal()).isZero();
        }
    }

    @Nested
    @DisplayName("getMaxHandSize")
    class GetMaxHandSize {

        @Test
        @DisplayName("Returns 7 by default")
        void returnsSevenByDefault() {
            TurnCleanupService svc = new TurnCleanupService(null);

            assertThat(svc.getMaxHandSize(gd, player1.getId())).isEqualTo(7);
        }

        @Test
        @DisplayName("Reduces hand size when opponent controls Jin-Gitaxias")
        void reducedByOpponentJinGitaxias() {
            harness.addToBattlefield(player2, new JinGitaxiasCoreAugur());

            TurnCleanupService svc = new TurnCleanupService(null);

            assertThat(svc.getMaxHandSize(gd, player1.getId())).isEqualTo(0);
        }

        @Test
        @DisplayName("Does not reduce own controller's hand size")
        void doesNotReduceOwnHandSize() {
            harness.addToBattlefield(player1, new JinGitaxiasCoreAugur());

            TurnCleanupService svc = new TurnCleanupService(null);

            assertThat(svc.getMaxHandSize(gd, player1.getId())).isEqualTo(7);
        }
    }

    @Nested
    @DisplayName("hasNoMaximumHandSize")
    class HasNoMaximumHandSize {

        @Test
        @DisplayName("Returns false by default")
        void returnsFalseByDefault() {
            TurnCleanupService svc = new TurnCleanupService(null);

            assertThat(svc.hasNoMaximumHandSize(gd, player1.getId())).isFalse();
        }

        @Test
        @DisplayName("Returns true when player controls Spellbook")
        void returnsTrueWithSpellbook() {
            harness.addToBattlefield(player1, new Spellbook());

            TurnCleanupService svc = new TurnCleanupService(null);

            assertThat(svc.hasNoMaximumHandSize(gd, player1.getId())).isTrue();
        }

        @Test
        @DisplayName("Returns true when player is in playersWithNoMaximumHandSize set")
        void returnsTrueWhenInGlobalSet() {
            gd.playersWithNoMaximumHandSize.add(player1.getId());

            TurnCleanupService svc = new TurnCleanupService(null);

            assertThat(svc.hasNoMaximumHandSize(gd, player1.getId())).isTrue();
        }

        @Test
        @DisplayName("Opponent's Spellbook does not affect this player")
        void opponentSpellbookDoesNotAffect() {
            harness.addToBattlefield(player2, new Spellbook());

            TurnCleanupService svc = new TurnCleanupService(null);

            assertThat(svc.hasNoMaximumHandSize(gd, player1.getId())).isFalse();
        }
    }
}
