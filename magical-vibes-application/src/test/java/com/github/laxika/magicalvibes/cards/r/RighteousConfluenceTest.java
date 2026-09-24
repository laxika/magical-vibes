package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GhostlyPrison;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RighteousConfluence.class, GhostlyPrison.class, GrizzlyBears.class})
class RighteousConfluenceTest extends BaseCardTest {

    @Test
    void repeatedKnightModeCreatesThreeVigilantKnights() {
        harness.setHand(player1, List.of(new RighteousConfluence()));
        addMana();

        int modes = ChooseOneEffect.encodeRepeatedModeSelection(3, 0, 0, 0);
        harness.castSorcery(player1, 0, modes);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3)
                .allMatch(permanent -> gqs.isCreature(gd, permanent)
                        && gqs.getEffectivePower(gd, permanent) == 2
                        && gqs.getEffectiveToughness(gd, permanent) == 2
                        && gqs.hasKeyword(gd, permanent, Keyword.VIGILANCE));
    }

    @Test
    void repeatedLifeModeGainsFifteenLife() {
        harness.setHand(player1, List.of(new RighteousConfluence()));
        addMana();

        int modes = ChooseOneEffect.encodeRepeatedModeSelection(3, 2, 2, 2);
        harness.castSorcery(player1, 0, modes);
        harness.passBothPriorities();

        harness.assertLife(player1, 35);
    }

    @Test
    void enchantmentModeExilesTargetEnchantment() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GhostlyPrison());
        harness.setHand(player1, List.of(new RighteousConfluence()));
        addMana();

        int modes = ChooseOneEffect.encodeRepeatedModeSelection(3, 1, 0, 2);
        harness.castSorcery(player1, 0, modes, List.of(enchantment.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(enchantment);
        assertThat(gd.findExiledCard(enchantment.getCard().getId())).isNotNull();
    }

    @Test
    void enchantmentModeRejectsNonenchantmentPermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RighteousConfluence()));
        addMana();

        int modes = ChooseOneEffect.encodeRepeatedModeSelection(3, 1, 0, 2);
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, modes, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 2);
    }
}
