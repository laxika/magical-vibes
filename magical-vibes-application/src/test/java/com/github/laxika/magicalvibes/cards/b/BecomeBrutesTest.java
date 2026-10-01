package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BecomeBrutes.class, GrizzlyBears.class})
class BecomeBrutesTest extends BaseCardTest {

    @Test
    void omittingOptionalTargetCreatesOnlyOneRole() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        castBecomeBrutes(List.of(target.getId()));

        assertThat(findPermanents(player1, "Monster"))
                .singleElement()
                .extracting(Permanent::getAttachedTo)
                .isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
    }

    @Test
    void createsRoleForEachChosenTargetGroup() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());

        castBecomeBrutes(List.of(first.getId(), second.getId()));

        assertThat(findPermanents(player1, "Monster"))
                .extracting(Permanent::getAttachedTo)
                .containsExactlyInAnyOrder(first.getId(), second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
    }

    private void castBecomeBrutes(List<UUID> targets) {
        harness.setHand(player1, List.of(new BecomeBrutes()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castSorcery(player1, 0, targets);
        harness.passBothPriorities();
    }
}
