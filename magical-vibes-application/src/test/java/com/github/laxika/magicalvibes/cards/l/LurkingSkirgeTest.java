package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LurkingSkirge.class, GrizzlyBears.class, Spellbook.class})
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
                .containsExactlyInAnyOrder(CardSubtype.PHYREXIAN, CardSubtype.IMP);
        assertThat(gqs.hasKeyword(gd, skirge, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Does not trigger again once Lurking Skirge has become a creature")
    void doesNotTriggerAgainAfterBecomingCreature() {
        Permanent skirge = harness.addToBattlefieldAndReturn(player1, new LurkingSkirge());
        Permanent firstBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, firstBears));
        harness.passBothPriorities();

        Permanent secondBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, secondBears));

        assertThat(gd.stack).isEmpty();
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
    @DisplayName("Triggers when an opponent-owned creature I control is put into its owner's graveyard")
    void triggersForOpponentOwnedCreatureControlledByController() {
        Permanent skirge = harness.addToBattlefieldAndReturn(player1, new LurkingSkirge());
        GrizzlyBears bearsCard = new GrizzlyBears();
        bearsCard.setOwnerId(player2.getId());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, bearsCard);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears));
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, skirge)).isTrue();
    }

    @Test
    @DisplayName("Does not trigger when an opponent controls a creature owned by the controller")
    void ignoresControllerOwnedCreatureControlledByOpponent() {
        Permanent skirge = harness.addToBattlefieldAndReturn(player1, new LurkingSkirge());
        GrizzlyBears bearsCard = new GrizzlyBears();
        bearsCard.setOwnerId(player1.getId());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, bearsCard);

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
