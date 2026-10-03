package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.e.EumidianTerrabotanist;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BiosynthicBurst.class, EumidianTerrabotanist.class})
class BiosynthicBurstTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a counter on, grants keywords to, and untaps the target creature")
    void resolvesAllEffects() {
        Permanent target = addCreature(player1);
        target.tap();
        castBurst(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.hasKeyword(Keyword.REACH)).isTrue();
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Temporary keywords expire at end of turn while the counter remains")
    void temporaryEffectsExpireAtEndOfTurn() {
        Permanent target = addCreature(player1);
        target.tap();
        castBurst(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.hasKeyword(Keyword.REACH)).isFalse();
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentsCreature() {
        Permanent target = addCreature(player2);
        harness.setHand(player1, List.of(new BiosynthicBurst()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("An untapped target still receives the counter and all keywords")
    void resolvesOnUntappedCreature() {
        Permanent target = addCreature(player1);
        castBurst(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.hasKeyword(Keyword.REACH)).isTrue();
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Only the target receives counters, keywords, and an untap")
    void affectsOnlyTarget() {
        Permanent target = addCreature(player1);
        Permanent other = addCreature(player1);
        Permanent opponent = addCreature(player2);
        target.tap();
        other.tap();
        opponent.tap();
        castBurst(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.isTapped()).isFalse();
        for (Permanent unaffected : List.of(other, opponent)) {
            assertThat(unaffected.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
            assertThat(unaffected.hasKeyword(Keyword.REACH)).isFalse();
            assertThat(unaffected.hasKeyword(Keyword.TRAMPLE)).isFalse();
            assertThat(unaffected.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
            assertThat(unaffected.isTapped()).isTrue();
        }
    }

    @Test
    @DisplayName("The spell has no effect if the target changes controller before resolution")
    void targetMustStillBeControlledAtResolution() {
        Permanent target = addCreature(player1);
        target.tap();
        harness.setHand(player1, List.of(new BiosynthicBurst()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(target.hasKeyword(Keyword.REACH)).isFalse();
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Biosynthic Burst");
    }

    @Test
    @DisplayName("The spell does not affect another creature when its target leaves the battlefield")
    void targetLeavingBattlefieldDoesNotRedirectEffects() {
        Permanent target = addCreature(player1);
        Permanent other = addCreature(player1);
        other.tap();
        harness.setHand(player1, List.of(new BiosynthicBurst()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(other.hasKeyword(Keyword.REACH)).isFalse();
        assertThat(other.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(other.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(other.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Biosynthic Burst");
    }

    private void castBurst(Permanent target) {
        harness.setHand(player1, List.of(new BiosynthicBurst()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addCreature(Player player) {
        return addCreatureReady(player, new EumidianTerrabotanist());
    }
}
