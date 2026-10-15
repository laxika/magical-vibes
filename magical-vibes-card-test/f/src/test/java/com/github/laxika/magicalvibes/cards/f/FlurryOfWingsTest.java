package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzledLeotau;
import com.github.laxika.magicalvibes.cards.t.TalonTrooper;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlurryOfWings.class, GrizzledLeotau.class, TalonTrooper.class})
class FlurryOfWingsTest extends BaseCardTest {

    private long birdSoldierCount(Player player) {
        return countPermanents(player, "Bird Soldier");
    }

    private void markAttacking(Player player, String cardName) {
        Permanent perm = findPermanent(player, cardName);
        perm.setAttacking(true);
    }

    @Test
    @DisplayName("Creates one flying Bird Soldier token per attacking creature, across all players")
    void createsTokenPerAttacker() {
        harness.addToBattlefield(player1, new GrizzledLeotau());
        harness.addToBattlefield(player2, new GrizzledLeotau());
        harness.addToBattlefield(player2, new TalonTrooper());
        markAttacking(player1, "Grizzled Leotau");
        markAttacking(player2, "Grizzled Leotau");
        markAttacking(player2, "Talon Trooper");

        harness.castFromHand(player1, new FlurryOfWings(), "{G}{W}{U}");
        harness.passBothPriorities();

        // X = 3 attacking creatures → 3 tokens, all under the caster's control.
        assertThat(birdSoldierCount(player1)).isEqualTo(3);
        assertThat(birdSoldierCount(player2)).isZero();
    }

    @Test
    @DisplayName("Creates no tokens when there are no attacking creatures")
    void createsNoTokensWithoutAttackers() {
        harness.addToBattlefield(player1, new GrizzledLeotau()); // present but not attacking

        harness.castFromHand(player1, new FlurryOfWings(), "{G}{W}{U}");
        harness.passBothPriorities();

        assertThat(birdSoldierCount(player1)).isZero();
    }

    @Test
    void createsUntappedNonattackingWhiteFlyingBirdSoldiers() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzledLeotau());
        attacker.setAttacking(true);
        harness.castFromHand(player1, new FlurryOfWings(), "{G}{W}{U}");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Bird Soldier")).hasSize(1).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.BIRD, CardSubtype.SOLDIER);
            assertThat(token.getCard().getKeywords()).containsExactly(Keyword.FLYING);
            assertThat(token.isTapped()).isFalse();
            assertThat(token.isAttacking()).isFalse();
        });
    }

    @Test
    void countsOnlyCreaturesStillAttackingAtResolution() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzledLeotau());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new TalonTrooper());
        first.setAttacking(true);
        second.setAttacking(true);
        harness.castFromHand(player1, new FlurryOfWings(), "{G}{W}{U}");
        second.setAttacking(false);
        harness.passBothPriorities();

        assertThat(birdSoldierCount(player1)).isEqualTo(1);
    }

    @Test
    void countsAttackersAddedBeforeResolution() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzledLeotau());
        first.setAttacking(true);
        harness.castFromHand(player1, new FlurryOfWings(), "{G}{W}{U}");
        Permanent additional = harness.addToBattlefieldAndReturn(player1, new TalonTrooper());
        additional.setAttacking(true);
        harness.passBothPriorities();

        assertThat(birdSoldierCount(player1)).isEqualTo(2);
    }
}
