package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmmaraSoulOfTheAccordTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Emmara creates a 1/1 white Soldier token with lifelink")
    void tappingEmmaraCreatesLifelinkSoldier() {
        Permanent emmara = harness.addToBattlefieldAndReturn(player1, new EmmaraSoulOfTheAccord());

        tap(emmara);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getName()).isEqualTo("Soldier");
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.hasKeyword(Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Tapping another creature you control does not trigger Emmara")
    void tappingAnotherCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new EmmaraSoulOfTheAccord());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new CoralMerfolk());

        tap(other);

        assertThat(gd.stack).isEmpty();
    }

    private void tap(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
    }
}
