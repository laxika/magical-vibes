package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TrueConviction.class, GrizzlyBears.class, Opalescence.class})
class TrueConvictionTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting True Conviction puts it on the stack as an enchantment spell")
    void castingPutsOnStack() {
        TrueConviction conviction = new TrueConviction();
        harness.setHand(player1, List.of(conviction));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getCard()).isSameAs(conviction);
    }

    // ===== Grants keywords to own creatures =====

    @Test
    @DisplayName("Own creatures gain double strike")
    void ownCreaturesGainDoubleStrike() {
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new TrueConviction());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Own creatures gain lifelink")
    void ownCreaturesGainLifelink() {
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new TrueConviction());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isTrue();
    }

    // ===== Does not affect opponent creatures =====

    @Test
    @DisplayName("Opponent creatures do not gain double strike")
    void opponentCreaturesDoNotGainDoubleStrike() {
        Permanent opponentBears = addReadyCreature(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new TrueConviction());

        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Opponent creatures do not gain lifelink")
    void opponentCreaturesDoNotGainLifelink() {
        Permanent opponentBears = addReadyCreature(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new TrueConviction());

        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.LIFELINK)).isFalse();
    }

    // ===== Keywords removed when enchantment leaves =====

    @Test
    @DisplayName("Keywords are lost when True Conviction leaves the battlefield")
    void keywordsLostWhenRemoved() {
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new TrueConviction());
        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("True Conviction"));

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isFalse();
    }

    // ===== Combat: double strike + lifelink =====

    @Test
    @DisplayName("Creature with granted double strike and lifelink gains life on unblocked combat")
    void doubleStrikeAndLifelinkInCombat() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new TrueConviction());
        bears.setAttacking(true);

        resolveCombat();

        // Grizzly Bears is 2/2 with double strike: deals 2 first strike + 2 normal = 4 total
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        // Lifelink: controller gains 4 life (2 + 2)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    @Test
    @DisplayName("Creatures entering after True Conviction gain both keywords")
    void laterCreaturesGainBothKeywords() {
        harness.addToBattlefield(player1, new TrueConviction());

        Permanent bears = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("True Conviction does not grant keywords to itself as a noncreature")
    void noncreatureDoesNotGainKeywords() {
        Permanent conviction = harness.addToBattlefieldAndReturn(player1, new TrueConviction());

        assertThat(gqs.hasKeyword(gd, conviction, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, conviction, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("True Conviction grants both keywords to itself when it becomes a creature")
    void animatedTrueConvictionGainsItsOwnKeywords() {
        Permanent conviction = harness.addToBattlefieldAndReturn(player1, new TrueConviction());
        harness.addToBattlefield(player1, new Opalescence());

        assertThat(gqs.isCreature(gd, conviction)).isTrue();
        assertThat(gqs.hasKeyword(gd, conviction, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, conviction, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Multiple True Convictions do not multiply damage or lifelink life gain")
    void multipleCopiesDoNotMultiplyCombatBenefits() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new TrueConviction());
        harness.addToBattlefield(player1, new TrueConviction());
        bears.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    @Test
    @DisplayName("Removing one True Conviction leaves the other copy's grants active")
    void removingOneCopyPreservesKeywords() {
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new TrueConviction());
        harness.addToBattlefield(player1, new TrueConviction());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isTrue();
        gd.playerBattlefields.get(player1.getId()).remove(first);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isTrue();
    }

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
