package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LurkingSkirgeTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent's creature dying makes Lurking Skirge a 3/2 Phyrexian Imp with flying")
    void becomesCreatureWhenOpponentCreatureDies() {
        Permanent skirge = harness.addToBattlefieldAndReturn(player1, new LurkingSkirge());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears));
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, skirge)).isTrue();
        assertThat(gqs.isEnchantment(gd, skirge)).isFalse();
        assertThat(gqs.getEffectivePower(gd, skirge)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, skirge)).isEqualTo(2);
        assertThat(gqs.effectiveCreatureSubtypes(gd, skirge))
                .containsExactly(CardSubtype.PHYREXIAN, CardSubtype.IMP);
        assertThat(gqs.hasKeyword(gd, skirge, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Lurking Skirge ignores a creature controlled by its controller")
    void ignoresControllerCreatureDies() {
        Permanent skirge = harness.addToBattlefieldAndReturn(player1, new LurkingSkirge());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears));
        harness.passBothPriorities();

        assertThat(gqs.isEnchantment(gd, skirge)).isTrue();
        assertThat(gqs.isCreature(gd, skirge)).isFalse();
    }

    @Test
    @DisplayName("Lurking Skirge ignores a noncreature permanent put into an opponent's graveyard")
    void ignoresOpponentNoncreaturePermanent() {
        Permanent skirge = harness.addToBattlefieldAndReturn(player1, new LurkingSkirge());
        Permanent spellbook = harness.addToBattlefieldAndReturn(player2, new Spellbook());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, spellbook));
        harness.passBothPriorities();

        assertThat(gqs.isEnchantment(gd, skirge)).isTrue();
        assertThat(gqs.isCreature(gd, skirge)).isFalse();
    }
}
