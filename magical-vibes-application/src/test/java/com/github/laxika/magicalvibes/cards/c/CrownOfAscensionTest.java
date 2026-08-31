package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrownOfAscension.class, GrizzlyBears.class, RagingGoblin.class})
class CrownOfAscensionTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature has flying")
    void enchantedCreatureHasFlying() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        attachCrown(bears);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Sacrificing the Aura gives flying to the enchanted creature and creatures sharing its type")
    void sacrificeGivesFlyingToEnchantedAndSharingCreatures() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherBears = addCreatureReady(player2, new GrizzlyBears());
        Permanent goblin = addCreatureReady(player1, new RagingGoblin());
        Permanent otherGoblin = addCreatureReady(player2, new RagingGoblin());
        Permanent crown = attachCrown(bears);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(crown), null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherBears, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, otherGoblin, Keyword.FLYING)).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(crown);
    }

    @Test
    @DisplayName("Sacrifice flying wears off at end of turn")
    void sacrificeFlyingWearsOffAtEndOfTurn() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherBears = addCreatureReady(player2, new GrizzlyBears());
        attachCrown(bears);

        Permanent crown = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof CrownOfAscension)
                .findFirst()
                .orElseThrow();
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(crown), null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, otherBears, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, otherBears, Keyword.FLYING)).isFalse();
    }

    private Permanent attachCrown(Permanent host) {
        Permanent crown = new Permanent(new CrownOfAscension());
        crown.setAttachedTo(host.getId());
        gd.playerBattlefields.get(player1.getId()).add(crown);
        return crown;
    }
}
