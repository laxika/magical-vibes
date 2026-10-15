package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DragonstormGlobe;
import com.github.laxika.magicalvibes.cards.r.RescueLeopard;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LightfootTechnique.class, DragonstormGlobe.class, RescueLeopard.class})
class LightfootTechniqueTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a counter on target creature and grants flying and indestructible")
    void putsCounterAndGrantsKeywords() {
        harness.addToBattlefield(player1, new RescueLeopard());
        harness.setHand(player1, List.of(new LightfootTechnique()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player1, "Rescue Leopard");
        harness.castAndResolveInstant(player1, 0, targetId);

        Permanent leopard = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(leopard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(leopard.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(leopard.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Flying and indestructible wear off at cleanup but the counter remains")
    void temporaryKeywordsWearOffAtCleanup() {
        harness.addToBattlefield(player1, new RescueLeopard());
        harness.setHand(player1, List.of(new LightfootTechnique()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player1, "Rescue Leopard");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        Permanent leopard = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(leopard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(leopard.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(leopard.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new DragonstormGlobe());
        harness.setHand(player1, List.of(new LightfootTechnique()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player1, "Dragonstorm Globe");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can target an opponent's creature without affecting another creature")
    void canTargetOpponentsCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new RescueLeopard());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RescueLeopard());
        harness.setHand(player1, List.of(new LightfootTechnique()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(ownCreature.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(ownCreature.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Repeated casts accumulate counters while both keyword grants expire")
    void repeatedCastsAccumulateCounters() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RescueLeopard());
        harness.setHand(player1, List.of(new LightfootTechnique(), new LightfootTechnique()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(target.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }
}
