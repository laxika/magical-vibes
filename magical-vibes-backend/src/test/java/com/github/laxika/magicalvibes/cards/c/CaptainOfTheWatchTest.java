package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CaptainOfTheWatchTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Captain of the Watch has static boost and ETB token effects")
    void hasCorrectEffects() {
        CaptainOfTheWatch card = new CaptainOfTheWatch();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(StaticBoostEffect.class);
        StaticBoostEffect boost = (StaticBoostEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(boost.powerBoost()).isEqualTo(1);
        assertThat(boost.toughnessBoost()).isEqualTo(1);
        assertThat(boost.grantedKeywords()).containsExactly(Keyword.VIGILANCE);
        assertThat(boost.scope()).isEqualTo(GrantScope.OWN_CREATURES);
        assertThat(boost.filter()).isInstanceOf(PermanentHasAnySubtypePredicate.class);

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(CreateCreatureTokenEffect.class);
        CreateCreatureTokenEffect tokenEffect =
                (CreateCreatureTokenEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(tokenEffect.amount()).isEqualTo(3);
    }

    // ===== ETB: creates three Soldier tokens =====

    @Test
    @DisplayName("ETB creates three 1/1 white Soldier tokens")
    void etbCreatesThreeSoldierTokens() {
        castAndResolveCaptain();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield).hasSize(4); // Captain + 3 tokens
        assertThat(countSoldierTokens(player1)).isEqualTo(3);
    }

    // ===== Static effect: buffs other Soldiers you control =====

    @Test
    @DisplayName("Soldier tokens created by ETB get +1/+1 and vigilance from lord effect")
    void soldierTokensGetBuff() {
        castAndResolveCaptain();

        Permanent token = findSoldierToken(player1);
        // 1/1 base + 1/1 from Captain = 2/2
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Other Soldier creatures you control get +1/+1 and vigilance")
    void buffsOtherSoldiersYouControl() {
        harness.addToBattlefield(player1, new CaptainOfTheWatch());
        harness.addToBattlefield(player1, new EliteVanguard());

        Permanent vanguard = findPermanent(player1, "Elite Vanguard");
        // Elite Vanguard is 2/1 base + 1/1 from Captain = 3/2
        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, vanguard)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, vanguard, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Captain of the Watch does not buff itself")
    void doesNotBuffItself() {
        harness.addToBattlefield(player1, new CaptainOfTheWatch());

        Permanent captain = findPermanent(player1, "Captain of the Watch");
        // 3/3 base, no self-buff
        assertThat(gqs.getEffectivePower(gd, captain)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, captain)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not buff non-Soldier creatures")
    void doesNotBuffNonSoldiers() {
        harness.addToBattlefield(player1, new CaptainOfTheWatch());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Does not buff opponent's Soldier creatures")
    void doesNotBuffOpponentSoldiers() {
        harness.addToBattlefield(player1, new CaptainOfTheWatch());
        harness.addToBattlefield(player2, new EliteVanguard());

        Permanent opponentVanguard = findPermanent(player2, "Elite Vanguard");
        assertThat(gqs.getEffectivePower(gd, opponentVanguard)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentVanguard)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, opponentVanguard, Keyword.VIGILANCE)).isFalse();
    }

    // ===== Bonus removed when Captain leaves =====

    @Test
    @DisplayName("Bonus is removed when Captain of the Watch leaves the battlefield")
    void bonusRemovedWhenCaptainLeaves() {
        harness.addToBattlefield(player1, new CaptainOfTheWatch());
        harness.addToBattlefield(player1, new EliteVanguard());

        Permanent vanguard = findPermanent(player1, "Elite Vanguard");
        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, vanguard, Keyword.VIGILANCE)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Captain of the Watch"));

        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, vanguard)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, vanguard, Keyword.VIGILANCE)).isFalse();
    }

    // ===== Two Captains stack =====

    @Test
    @DisplayName("Two Captains of the Watch buff each other and stack bonuses on Soldiers")
    void twoCaptainsStackBonuses() {
        harness.addToBattlefield(player1, new CaptainOfTheWatch());
        harness.addToBattlefield(player1, new CaptainOfTheWatch());
        harness.addToBattlefield(player1, new EliteVanguard());

        Permanent vanguard = findPermanent(player1, "Elite Vanguard");
        // 2/1 base + 2/2 from two Captains = 4/3
        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, vanguard)).isEqualTo(3);

        // Each Captain buffs the other (both are Soldiers)
        List<Permanent> captains = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Captain of the Watch"))
                .toList();
        assertThat(captains).hasSize(2);
        for (Permanent captain : captains) {
            // 3/3 base + 1/1 from the other Captain = 4/4
            assertThat(gqs.getEffectivePower(gd, captain)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, captain)).isEqualTo(4);
            assertThat(gqs.hasKeyword(gd, captain, Keyword.VIGILANCE)).isTrue();
        }
    }

    // ===== Helpers =====

    private void castAndResolveCaptain() {
        harness.setHand(player1, List.of(new CaptainOfTheWatch()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger
    }

    private int countSoldierTokens(Player player) {
        return (int) gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Soldier"))
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.SOLDIER))
                .count();
    }

    private Permanent findSoldierToken(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Soldier"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No Soldier token found"));
    }

    private Permanent findPermanent(Player player, String cardName) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(cardName))
                .findFirst().orElseThrow();
    }
}
