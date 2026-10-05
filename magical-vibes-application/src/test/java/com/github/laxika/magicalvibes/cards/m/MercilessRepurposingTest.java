package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DoublingSeason;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MercilessRepurposing.class, GrizzlyBears.class, Forest.class, DoublingSeason.class})
class MercilessRepurposingTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles target creature and incubates three")
    void exilesTargetCreatureAndIncubatesThree() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        prepareSpell();
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));

        Permanent incubator = findPermanent(player1, "Incubator");
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        UUID targetId = harness.getPermanentId(player2, "Forest");
        harness.setHand(player1, List.of(new MercilessRepurposing()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Incubator transforms into a three-power artifact creature without losing counters")
    void incubatorTransformsAndKeepsCounters() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        prepareSpell();
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        Permanent incubator = findPermanent(player1, "Incubator");
        assertThat(countPermanents(player1, "Incubator")).isEqualTo(1);
        assertThat(countPermanents(player2, "Incubator")).isZero();
        assertThat(gqs.isCreature(gd, incubator)).isFalse();
        assertThat(gqs.isArtifact(gd, incubator)).isTrue();
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);

        harness.addMana(player1, ManaColor.BLACK, 2);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(incubator);
        harness.activateAbility(player1, index, null, null);
        assertThat(incubator.isTransformed()).isFalse();
        harness.passBothPriorities();

        assertThat(incubator.isTransformed()).isTrue();
        assertThat(gqs.isCreature(gd, incubator)).isTrue();
        assertThat(gqs.isArtifact(gd, incubator)).isTrue();
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, incubator)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, incubator)).isEqualTo(3);
        assertThatThrownBy(() -> harness.activateAbility(player1, index, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can exile its controller's creature and still incubate")
    void canExileOwnCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        prepareSpell();
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(findPermanent(player1, "Incubator").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(3);
    }

    @Test
    @DisplayName("Does not incubate when the only target leaves before resolution")
    void doesNotIncubateWhenTargetLeaves() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent target = findPermanent(player2, "Grizzly Bears");
        prepareSpell();
        harness.castInstant(player1, 0, target.getId());
        harness.getPermanentRemovalService().removePermanentToExile(gd, target);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Incubator")).isZero();
        harness.assertInGraveyard(player1, "Merciless Repurposing");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Doubling Season doubles both Incubator tokens and their entering counters")
    void doublingSeasonDoublesTokensAndCounters() {
        harness.addToBattlefield(player1, new DoublingSeason());
        harness.addToBattlefield(player2, new GrizzlyBears());
        prepareSpell();
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        assertThat(findPermanents(player1, "Incubator"))
                .hasSize(2)
                .allSatisfy(token -> assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                        .isEqualTo(6));
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    private void prepareSpell() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new MercilessRepurposing()));
        addMana();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 6);
    }
}
