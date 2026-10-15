package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
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

@CardUsed({MobRule.class, CrawWurm.class, GrizzlyBears.class, HillGiant.class, GloriousAnthem.class})
class MobRuleTest extends BaseCardTest {

    @Test
    @DisplayName("The high-power mode steals, untaps, and gives haste to creatures with power 4 or greater")
    void highPowerMode() {
        Permanent large = harness.addToBattlefieldAndReturn(player2, new CrawWurm());
        Permanent small = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        large.tap();
        small.tap();

        cast(0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId)
                .contains(large.getId());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .contains(small.getId())
                .doesNotContain(large.getId());
        assertThat(large.isTapped()).isFalse();
        assertThat(large.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(small.isTapped()).isTrue();
        assertThat(small.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("The low-power mode includes power 3 and excludes power 4")
    void lowPowerMode() {
        Permanent small = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent boundary = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent large = harness.addToBattlefieldAndReturn(player2, new CrawWurm());
        small.tap();
        boundary.tap();
        large.tap();

        cast(1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId)
                .containsExactly(small.getId(), boundary.getId());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .containsExactly(large.getId());
        assertThat(small.isTapped()).isFalse();
        assertThat(boundary.isTapped()).isFalse();
        assertThat(large.isTapped()).isTrue();
        assertThat(small.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(boundary.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(large.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Temporary control and haste expire at cleanup")
    void effectsExpireAtCleanup() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .contains(target.getId());
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("High-power creatures still untap and gain haste after losing an opponent's anthem")
    void highPowerCreatureLosesAnthemAfterControlChange() {
        harness.addToBattlefield(player2, new GloriousAnthem());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        creature.tap();

        cast(0);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(creature.isTapped()).isFalse();
        assertThat(creature.hasKeyword(Keyword.HASTE)).isTrue();
        harness.assertOnBattlefield(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Low-power creatures still untap and gain haste after receiving the caster's anthem")
    void lowPowerCreatureGainsAnthemAfterControlChange() {
        harness.addToBattlefield(player1, new GloriousAnthem());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        creature.tap();

        cast(1);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(creature.isTapped()).isFalse();
        assertThat(creature.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Matching creatures already controlled by the caster are untapped and gain haste")
    void affectsCreaturesAlreadyControlled() {
        Permanent small = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent large = harness.addToBattlefieldAndReturn(player1, new CrawWurm());
        small.tap();
        large.tap();

        cast(1);

        assertThat(small.isTapped()).isFalse();
        assertThat(small.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(large.isTapped()).isTrue();
        assertThat(large.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("A mode can resolve with no matching creatures")
    void resolvesWithoutMatchingCreatures() {
        Permanent small = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        small.tap();

        cast(0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(small);
        assertThat(small.isTapped()).isTrue();
        assertThat(small.hasKeyword(Keyword.HASTE)).isFalse();
        harness.assertInGraveyard(player1, "Mob Rule");
    }

    private void cast(int mode) {
        harness.setHand(player1, List.of(new MobRule()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castSorcery(player1, 0, mode);
        harness.passBothPriorities();
    }
}
