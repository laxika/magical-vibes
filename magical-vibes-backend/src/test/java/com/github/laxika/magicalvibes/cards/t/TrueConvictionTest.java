package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TrueConvictionTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has two static grant keyword effects for double strike and lifelink")
    void hasCorrectEffects() {
        TrueConviction card = new TrueConviction();

        List<GrantKeywordEffect> keywordEffects = card.getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof GrantKeywordEffect)
                .map(e -> (GrantKeywordEffect) e)
                .toList();
        assertThat(keywordEffects).hasSize(2);
        assertThat(keywordEffects).flatExtracting(GrantKeywordEffect::keywords)
                .containsExactlyInAnyOrder(Keyword.DOUBLE_STRIKE, Keyword.LIFELINK);
        assertThat(keywordEffects).allMatch(e -> e.scope() == GrantScope.OWN_CREATURES);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting True Conviction puts it on the stack as an enchantment spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new TrueConviction()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("True Conviction");
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

    // ===== Helpers =====

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
