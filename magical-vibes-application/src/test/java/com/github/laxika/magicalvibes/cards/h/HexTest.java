package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Hex.class, GrizzlyBears.class, Forest.class})
class HexTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys six target creatures")
    void destroysSixTargetCreatures() {
        List<UUID> targets = addSixCreatures();
        harness.setHand(player1, List.of(new Hex()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, targets);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Requires exactly six creature targets")
    void requiresExactlySixCreatureTargets() {
        List<UUID> targets = addSixCreatures();
        harness.setHand(player1, List.of(new Hex()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targets.subList(0, 5)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        List<UUID> targets = new ArrayList<>(addSixCreatures());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        targets.set(0, forest.getId());
        harness.setHand(player1, List.of(new Hex()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targets))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    private List<UUID> addSixCreatures() {
        return IntStream.range(0, 6)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId())
                .toList();
    }
}
