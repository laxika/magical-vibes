package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AshnodsCylix;
import com.github.laxika.magicalvibes.cards.b.BenthicExplorers;
import com.github.laxika.magicalvibes.cards.l.LimDLsHighGuard;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PhyrexianBoon.class, LimDLsHighGuard.class, BenthicExplorers.class, AshnodsCylix.class})
class PhyrexianBoonTest extends BaseCardTest {

    private Permanent attach(Permanent creature) {
        Permanent boon = harness.addToBattlefieldAndReturn(player1, new PhyrexianBoon());
        boon.setAttachedTo(creature.getId());
        return boon;
    }

    @Test
    @DisplayName("Black enchanted creature gets +2/+1")
    void blackCreatureGetsBoost() {
        Permanent black = addCreatureReady(player1, new LimDLsHighGuard());
        int basePower = gqs.getEffectivePower(gd, black);
        int baseToughness = gqs.getEffectiveToughness(gd, black);

        attach(black);

        assertThat(gqs.getEffectivePower(gd, black)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, black)).isEqualTo(baseToughness + 1);
    }

    @Test
    @DisplayName("Nonblack enchanted creature gets -1/-2 instead")
    void nonBlackCreatureGetsPenalty() {
        Permanent nonBlack = addCreatureReady(player1, new BenthicExplorers());
        int basePower = gqs.getEffectivePower(gd, nonBlack);
        int baseToughness = gqs.getEffectiveToughness(gd, nonBlack);

        attach(nonBlack);

        assertThat(gqs.getEffectivePower(gd, nonBlack)).isEqualTo(basePower - 1);
        assertThat(gqs.getEffectiveToughness(gd, nonBlack)).isEqualTo(baseToughness - 2);
    }

    @Test
    @DisplayName("Modification wears off when Phyrexian Boon leaves the battlefield")
    void boostRemovedWhenAuraLeaves() {
        Permanent black = addCreatureReady(player1, new LimDLsHighGuard());
        int basePower = gqs.getEffectivePower(gd, black);

        Permanent boon = attach(black);
        assertThat(gqs.getEffectivePower(gd, black)).isEqualTo(basePower + 2);

        gd.playerBattlefields.get(player1.getId()).remove(boon);

        assertThat(gqs.getEffectivePower(gd, black)).isEqualTo(basePower);
    }

    @Test
    @DisplayName("Resolving Phyrexian Boon attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent creature = addCreatureReady(player2, new BenthicExplorers());

        harness.setHand(player1, List.of(new PhyrexianBoon()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof PhyrexianBoon
                        && p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AshnodsCylix());
        harness.setHand(player1, List.of(new PhyrexianBoon()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
