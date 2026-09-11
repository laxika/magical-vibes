package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({MechtitanCore.class, Ornithopter.class, MindStone.class,
        GrizzlyBears.class, Murder.class})
class MechtitanCoreTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles four qualifying permanents and returns them tapped when Mechtitan leaves")
    void createsMechtitanAndReturnsExiledPermanentsTapped() {
        Permanent core = harness.addToBattlefieldAndReturn(player1, new MechtitanCore());
        List<Permanent> ornithopters = List.of(
                harness.addToBattlefieldAndReturn(player1, new Ornithopter()),
                harness.addToBattlefieldAndReturn(player1, new Ornithopter()),
                harness.addToBattlefieldAndReturn(player1, new Ornithopter()),
                harness.addToBattlefieldAndReturn(player1, new Ornithopter())
        );
        Permanent nonCreatureArtifact = harness.addToBattlefieldAndReturn(player1, new MindStone());
        Permanent nonArtifactCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent mechtitan = findPermanents(player1, "Mechtitan").getFirst();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(
                        core.getCard().getId(),
                        ornithopters.get(0).getCard().getId(),
                        ornithopters.get(1).getCard().getId(),
                        ornithopters.get(2).getCard().getId(),
                        ornithopters.get(3).getCard().getId());

        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.clearPriorityPassed();
        harness.castInstant(player1, 0, mechtitan.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Mechtitan")).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> ornithopters.stream()
                        .anyMatch(ornithopter -> ornithopter.getCard().getId().equals(p.getCard().getId())))
                .allMatch(Permanent::isTapped);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(nonCreatureArtifact, nonArtifactCreature);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(core.getCard().getId());
    }

    @Test
    @DisplayName("Cannot activate without four artifact creatures or Vehicles")
    void requiresFourQualifyingPermanents() {
        harness.addToBattlefield(player1, new MechtitanCore());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new MindStone());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 4 other artifacts");
    }
}
