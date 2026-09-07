package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TwistedFealty.class, GrizzlyBears.class})
class TwistedFealtyTest extends BaseCardTest {

    @Test
    void stealsUntapsGrantsHasteAndAttachesWickedRole() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();
        castTwistedFealty(List.of(target.getId(), target.getId()));

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();

        Permanent role = findPermanent(player1, "Wicked");
        assertThat(role.getCard().isToken()).isTrue();
        assertThat(role.getCard().getSubtypes()).contains(CardSubtype.ROLE);
        assertThat(role.getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isTrue();
    }

    @Test
    void roleTargetMayBeOmitted() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castTwistedFealty(List.of(target.getId()));

        assertThat(findPermanents(player1, "Wicked")).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent -> permanent.getId().equals(target.getId()));
    }

    @Test
    void controlAndHasteExpireButRoleRemainsAttached() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castTwistedFealty(List.of(target.getId(), target.getId()));
        Permanent role = findPermanent(player1, "Wicked");

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(role.getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isTrue();
    }

    @Test
    void wickedRoleCausesEachOpponentToLoseLifeWhenItDies() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castTwistedFealty(List.of(target.getId(), target.getId()));
        Permanent role = findPermanent(player1, "Wicked");
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, role));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    private void castTwistedFealty(List<java.util.UUID> targets) {
        harness.setHand(player1, List.of(new TwistedFealty()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, targets);
        harness.passBothPriorities();
    }
}
