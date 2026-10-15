package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BorosLocket;
import com.github.laxika.magicalvibes.cards.f.FreshFacedRecruit;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CandlelightVigil.class, FreshFacedRecruit.class, BorosLocket.class})
class CandlelightVigilTest extends BaseCardTest {

    @Test
    void resolvesOnOpponentsCreatureWithoutBoostingOtherCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FreshFacedRecruit());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new FreshFacedRecruit());
        harness.setHand(player1, List.of(new CandlelightVigil()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Candlelight Vigil").getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, other, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void enchantedAttackerDoesNotTapButOtherAttackerDoes() {
        Permanent target = addCreatureReady(player1, new FreshFacedRecruit());
        Permanent other = addCreatureReady(player1, new FreshFacedRecruit());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new CandlelightVigil());
        aura.setAttachedTo(target.getId());

        declareAttackersAndPrepareBlockers(List.of(0, 1));

        assertThat(target.isTapped()).isFalse();
        assertThat(other.isTapped()).isTrue();
    }

    @Test
    void doesNotEnterBattlefieldWhenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FreshFacedRecruit());
        harness.setHand(player1, List.of(new CandlelightVigil()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Candlelight Vigil")).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof CandlelightVigil);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Candlelight Vigil gives the enchanted creature +3/+2 and vigilance")
    void boostsAndGrantsVigilance() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new FreshFacedRecruit());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new CandlelightVigil());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Candlelight Vigil's effects stop when it leaves the battlefield")
    void effectsStopWhenRemoved() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new FreshFacedRecruit());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new CandlelightVigil());
        aura.setAttachedTo(bears.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Candlelight Vigil cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new BorosLocket());
        harness.setHand(player1, List.of(new CandlelightVigil()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
