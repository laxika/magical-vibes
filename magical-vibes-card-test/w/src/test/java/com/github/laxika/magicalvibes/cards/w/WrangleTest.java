package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Wrangle.class, AirElemental.class, CrawWurm.class, GrizzlyBears.class, GloriousAnthem.class})
class WrangleTest extends BaseCardTest {

    @Test
    @DisplayName("Wrangle can target a creature with power 4")
    void canTargetCreatureWithPowerFour() {
        Permanent target = addCreatureReady(player2, new AirElemental());
        target.tap();
        castWrangle(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isTrue();
    }

    @Test
    @DisplayName("Wrangle's control and haste expire at cleanup")
    void controlAndHasteExpireAtCleanup() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castWrangle(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isFalse();
    }

    @Test
    @DisplayName("Wrangle rejects a creature with power greater than 4")
    void cannotTargetCreatureWithPowerGreaterThanFour() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new CrawWurm());
        harness.setHand(player1, List.of(new Wrangle()));
        addWrangleMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature with power 4 or less");
    }

    @Test
    @DisplayName("Wrangle fizzles if its target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Wrangle()));
        addWrangleMana();

        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Wrangle can untap and grant haste to a creature you already control")
    void canTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.tap();

        castWrangle(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        declareAttackers(List.of(0));
        resolveCombat();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Wrangle does nothing if the target's power exceeds four before resolution")
    void fizzlesIfPowerIncreasesBeforeResolution() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();
        harness.setHand(player1, List.of(new Wrangle()));
        addWrangleMana();
        harness.castSorcery(player1, 0, target.getId());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(target.isTapped()).isTrue();
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        harness.assertInGraveyard(player1, "Wrangle");
    }

    @Test
    @DisplayName("Wrangle completes resolution when gaining control raises the creature's power above four")
    void resolvesWhenControlChangeIncreasesPower() {
        harness.addToBattlefield(player1, new GloriousAnthem());
        Permanent target = addCreatureReady(player2, new AirElemental());
        target.tap();

        castWrangle(target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isTrue();
    }

    @Test
    @DisplayName("Wrangle cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new Wrangle()));
        addWrangleMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Wrangle uses modified power when choosing its target")
    void cannotTargetCreatureWhoseCountersRaisePowerAboveFour() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setHand(player1, List.of(new Wrangle()));
        addWrangleMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature with power 4 or less");
    }

    private void castWrangle(Permanent target) {
        harness.setHand(player1, List.of(new Wrangle()));
        addWrangleMana();
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }

    private void addWrangleMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);
    }
}
