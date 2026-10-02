package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.ConvenientTarget;
import com.github.laxika.magicalvibes.cards.m.MakeYourMove;
import com.github.laxika.magicalvibes.cards.n.NervousGardener;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AirtightAlibi.class, ConvenientTarget.class, NervousGardener.class, MakeYourMove.class})
class AirtightAlibiTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps, boosts, grants hexproof, and clears suspicion from the enchanted creature")
    void resolvesEnterTheBattlefieldEffects() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new NervousGardener());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new NervousGardener());
        enchanted.tap();
        enchanted.setSuspected(true);
        other.setSuspected(true);

        harness.setHand(player1, List.of(new AirtightAlibi()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, enchanted.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(enchanted.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, enchanted)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, enchanted, Keyword.HEXPROOF)).isTrue();
        assertThat(enchanted.isSuspected()).isFalse();
        assertThat(other.isSuspected()).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, enchanted, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, enchanted)).isEqualTo(4);
    }

    @Test
    @DisplayName("Enchanted creature cannot become suspected while the Aura remains attached")
    void preventsBecomingSuspected() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new NervousGardener());

        harness.setHand(player1, List.of(new AirtightAlibi()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, enchanted.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new ConvenientTarget()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castEnchantment(player1, 0, enchanted.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(enchanted.isSuspected()).isFalse();
    }

    @Test
    @DisplayName("Entry trigger still untaps, grants hexproof, and clears suspicion after the Aura is destroyed")
    void resolvesTriggerUsingLastKnownAttachment() {
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        Permanent enchanted = harness.addToBattlefieldAndReturn(player2, new NervousGardener());
        enchanted.tap();
        enchanted.setSuspected(true);

        harness.setHand(player1, List.of(new AirtightAlibi()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, enchanted.getId());
        harness.passBothPriorities();

        Permanent aura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof AirtightAlibi)
                .findFirst().orElseThrow();
        assertThat(enchanted.isTapped()).isTrue();
        assertThat(enchanted.isSuspected()).isTrue();
        assertThat(gqs.hasKeyword(gd, enchanted, Keyword.HEXPROOF)).isFalse();

        harness.setHand(player1, List.of(new MakeYourMove()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, aura.getId());
        harness.assertNotOnBattlefield(player1, "Airtight Alibi");
        harness.passBothPriorities();

        assertThat(enchanted.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, enchanted, Keyword.HEXPROOF)).isTrue();
        assertThat(enchanted.isSuspected()).isFalse();
        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enchanted)).isEqualTo(2);

        harness.setHand(player2, List.of(new ConvenientTarget()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castEnchantment(player2, 0, enchanted.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(enchanted.isSuspected()).isTrue();
    }
}
