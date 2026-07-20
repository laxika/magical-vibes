package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RegalCaracalTest extends BaseCardTest {

    // ===== ETB: creates two Cat tokens with lifelink =====

    @Test
    @DisplayName("ETB creates two 1/1 white Cat tokens with lifelink")
    void etbCreatesTwoCatTokens() {
        castAndResolveCaracal();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield).hasSize(3); // Caracal + 2 tokens
        assertThat(countCatTokens(player1)).isEqualTo(2);

        Permanent token = findCatToken(player1);
        assertThat(gqs.hasKeyword(gd, token, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Cat tokens created by ETB get +1/+1 from the lord effect")
    void catTokensGetBuff() {
        castAndResolveCaracal();

        Permanent token = findCatToken(player1);
        // 1/1 base + 1/1 from Caracal = 2/2
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, token, Keyword.LIFELINK)).isTrue();
    }

    // ===== Static effect: buffs other Cats you control =====

    @Test
    @DisplayName("Other Cat creatures you control get +1/+1 and lifelink")
    void buffsOtherCatsYouControl() {
        harness.addToBattlefield(player1, new RegalCaracal());
        harness.addToBattlefield(player1, new RegalCaracal());

        // Each Caracal is a Cat: 3/3 base + 1/1 from the other Caracal = 4/4 with lifelink.
        for (Permanent caracal : findCaracals(player1)) {
            assertThat(gqs.getEffectivePower(gd, caracal)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, caracal)).isEqualTo(4);
            assertThat(gqs.hasKeyword(gd, caracal, Keyword.LIFELINK)).isTrue();
        }
    }

    @Test
    @DisplayName("Regal Caracal does not buff itself")
    void doesNotBuffItself() {
        harness.addToBattlefield(player1, new RegalCaracal());

        Permanent caracal = findPermanent(player1, "Regal Caracal");
        // 3/3 base, no self-buff, no lifelink.
        assertThat(gqs.getEffectivePower(gd, caracal)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, caracal)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, caracal, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Does not buff non-Cat creatures")
    void doesNotBuffNonCats() {
        harness.addToBattlefield(player1, new RegalCaracal());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Does not buff opponent's Cat creatures")
    void doesNotBuffOpponentCats() {
        harness.addToBattlefield(player1, new RegalCaracal());
        harness.addToBattlefield(player2, new RegalCaracal());

        // Opponent's Caracal is a Cat but only its controller's Caracal never buffs it (and this one
        // does not buff itself), so it stays 3/3 with no lifelink.
        Permanent opponentCaracal = findPermanent(player2, "Regal Caracal");
        assertThat(gqs.getEffectivePower(gd, opponentCaracal)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponentCaracal)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, opponentCaracal, Keyword.LIFELINK)).isFalse();
    }

    // ===== Bonus removed when source leaves =====

    @Test
    @DisplayName("Bonus is removed when the buffing Caracal leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new RegalCaracal());
        harness.addToBattlefield(player1, new RegalCaracal());

        Permanent caracal = findCaracals(player1).get(0);
        assertThat(gqs.getEffectivePower(gd, caracal)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, caracal, Keyword.LIFELINK)).isTrue();

        // Remove the other Caracal (the one supplying the buff to this one).
        Permanent other = findCaracals(player1).get(1);
        gd.playerBattlefields.get(player1.getId()).remove(other);

        assertThat(gqs.getEffectivePower(gd, caracal)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, caracal)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, caracal, Keyword.LIFELINK)).isFalse();
    }

    // ===== Helpers =====

    private void castAndResolveCaracal() {
        harness.setHand(player1, List.of(new RegalCaracal()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger
    }

    private List<Permanent> findCaracals(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Regal Caracal"))
                .toList();
    }

    private int countCatTokens(Player player) {
        return (int) gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Cat"))
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.CAT))
                .count();
    }

    private Permanent findCatToken(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Cat"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No Cat token found"));
    }

}
