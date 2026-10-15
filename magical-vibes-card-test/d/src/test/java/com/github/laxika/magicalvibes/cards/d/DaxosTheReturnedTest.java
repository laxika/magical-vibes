package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.Bitterblossom;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DaxosTheReturned.class, Bitterblossom.class, SolRing.class})
class DaxosTheReturnedTest extends BaseCardTest {

    @Test
    void gainsExperienceWhenControllerCastsAnEnchantment() {
        harness.addToBattlefield(player1, new DaxosTheReturned());
        harness.castFromHand(player1, new Bitterblossom(), "{1}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerExperienceCounters).containsEntry(player1.getId(), 1);
    }

    @Test
    void createsSpiritEnchantmentTokenWhosePowerAndToughnessFollowExperience() {
        harness.addToBattlefield(player1, new DaxosTheReturned());
        gd.playerExperienceCounters.put(player1.getId(), 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(spirit.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(spirit.getCard().getColors()).containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
        assertThat(spirit.getCard().getAdditionalTypes()).contains(CardType.ENCHANTMENT);
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(3);

        gd.playerExperienceCounters.put(player1.getId(), 5);
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(5);
    }

    @Test
    void experienceTriggerResolvesBeforeEnchantmentAndSurvivesDaxosLeaving() {
        harness.addToBattlefield(player1, new DaxosTheReturned());
        harness.castFromHand(player1, new Bitterblossom(), "{1}{B}");

        assertThat(gd.playerExperienceCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.stack).hasSize(2);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerExperienceCounters).containsEntry(player1.getId(), 1);
        assertThat(gd.stack).hasSize(1);
        assertThat(countPermanents(player1, "Bitterblossom")).isZero();
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Bitterblossom")).isEqualTo(1);
    }

    @Test
    void opponentsEnchantmentDoesNotGrantExperience() {
        harness.addToBattlefield(player1, new DaxosTheReturned());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new Bitterblossom(), "{1}{B}");
        harness.passBothPriorities();

        assertThat(gd.playerExperienceCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.playerExperienceCounters.getOrDefault(player2.getId(), 0)).isZero();
        assertThat(countPermanents(player2, "Bitterblossom")).isEqualTo(1);
    }

    @Test
    void nonEnchantmentSpellDoesNotGrantExperience() {
        harness.addToBattlefield(player1, new DaxosTheReturned());
        harness.castFromHand(player1, new SolRing(), "{1}");
        harness.passBothPriorities();

        assertThat(gd.playerExperienceCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(countPermanents(player1, "Sol Ring")).isEqualTo(1);
    }

    @Test
    void creatingEnchantmentTokenDoesNotGrantExperience() {
        harness.addToBattlefield(player1, new DaxosTheReturned());
        gd.playerExperienceCounters.put(player1.getId(), 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
        assertThat(gd.playerExperienceCounters).containsEntry(player1.getId(), 2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void spiritDiesWithNoExperienceCounters() {
        harness.addToBattlefield(player1, new DaxosTheReturned());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Spirit")).isZero();
        assertThat(countPermanents(player1, "Daxos the Returned")).isEqualTo(1);
    }

    @Test
    void tokenAbilitySurvivesSourceLeavingAndUsesExperienceAtResolution() {
        harness.addToBattlefield(player1, new DaxosTheReturned());
        gd.playerExperienceCounters.put(player1.getId(), 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).clear();
        gd.playerExperienceCounters.put(player1.getId(), 4);
        harness.passBothPriorities();

        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(4);
        gd.playerExperienceCounters.put(player1.getId(), 6);
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(6);
    }

    @Test
    void spiritUsesItsCurrentControllersExperienceCounters() {
        harness.addToBattlefield(player1, new DaxosTheReturned());
        gd.playerExperienceCounters.put(player1.getId(), 2);
        gd.playerExperienceCounters.put(player2.getId(), 5);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent spirit = findPermanent(player1, "Spirit");
        gd.playerBattlefields.get(player1.getId()).remove(spirit);
        gd.playerBattlefields.get(player2.getId()).add(spirit);

        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(5);
        gd.playerExperienceCounters.put(player1.getId(), 8);
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(5);
    }
}
