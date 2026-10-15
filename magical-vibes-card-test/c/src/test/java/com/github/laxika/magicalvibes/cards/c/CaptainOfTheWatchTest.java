package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CaptainOfTheWatch.class, EliteVanguard.class, RuneclawBear.class})
class CaptainOfTheWatchTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates three 1/1 white Soldier tokens")
    void etbCreatesThreeSoldierTokens() {
        castAndResolveCaptain();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield).hasSize(4); // Captain + 3 tokens
        assertThat(countPermanents(player1, "Soldier")).isEqualTo(3);
    }

    @Test
    @DisplayName("Soldier tokens created by ETB get +1/+1 and vigilance from lord effect")
    void soldierTokensGetBuff() {
        castAndResolveCaptain();

        Permanent token = findPermanent(player1, "Soldier");
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
        harness.addToBattlefield(player1, new RuneclawBear());

        Permanent bears = findPermanent(player1, "Runeclaw Bear");
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
        List<Permanent> captains = findPermanents(player1, "Captain of the Watch");
        assertThat(captains).hasSize(2);
        for (Permanent captain : captains) {
            // 3/3 base + 1/1 from the other Captain = 4/4
            assertThat(gqs.getEffectivePower(gd, captain)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, captain)).isEqualTo(4);
            assertThat(gqs.hasKeyword(gd, captain, Keyword.VIGILANCE)).isTrue();
        }
    }

    @Test
    @DisplayName("Captain and Soldiers granted vigilance do not tap to attack")
    void captainAndSoldiersAttackWithoutTapping() {
        Permanent captain = addCreatureReady(player1, new CaptainOfTheWatch());
        Permanent vanguard = addCreatureReady(player1, new EliteVanguard());

        declareAttackers(List.of(0, 1));
        resolveCombat();

        assertThat(captain.isTapped()).isFalse();
        assertThat(vanguard.isTapped()).isFalse();
        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("ETB still creates unboosted tokens if Captain leaves before the trigger resolves")
    void etbResolvesAfterCaptainLeaves() {
        harness.setHand(player1, List.of(new CaptainOfTheWatch()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Soldier")).isZero();

        Permanent captain = findPermanent(player1, "Captain of the Watch");
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, captain);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Captain of the Watch");
        assertThat(findPermanents(player1, "Soldier")).hasSize(3).allSatisfy(token -> {
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SOLDIER);
            assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isFalse();
        });
    }

    private void castAndResolveCaptain() {
        harness.setHand(player1, List.of(new CaptainOfTheWatch()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }

}
