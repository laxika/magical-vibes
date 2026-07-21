package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ItOfTheHorridSwarmTest extends BaseCardTest {

    @Test
    @DisplayName("Hardcast: when cast, create two 1/1 green Insect tokens")
    void hardcastCreatesInsects() {
        harness.setHand(player1, List.of(new ItOfTheHorridSwarm()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve cast trigger
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("It of the Horrid Swarm"));
        List<Permanent> insects = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Insect"))
                .toList();
        assertThat(insects).hasSize(2);
        assertThat(insects).allSatisfy(token -> {
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.INSECT);
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Emerge: sacrifice a creature, pay emerge cost reduced by its mana value")
    void emergeSacrificesAndReducesCost() {
        harness.addToBattlefield(player1, new GrizzlyBears()); // MV 2
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.setHand(player1, List.of(new ItOfTheHorridSwarm()));
        // Emerge {6}{G} reduced by 2 → {4}{G}
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(bearsId));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("It of the Horrid Swarm"))
                .noneMatch(p -> p.getId().equals(bearsId));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        long insects = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Insect") && p.getCard().isToken())
                .count();
        assertThat(insects).isEqualTo(2);
    }

    @Test
    @DisplayName("Emerge fails without enough mana after reduction")
    void emergeFailsWithInsufficientMana() {
        harness.addToBattlefield(player1, new GrizzlyBears()); // MV 2
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.setHand(player1, List.of(new ItOfTheHorridSwarm()));
        // Need {4}{G} after reduction; only {3}{G} available
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() ->
                harness.castCreatureWithAlternateCost(player1, 0, List.of(bearsId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cast trigger resolves before the creature spell")
    void castTriggerResolvesBeforeCreature() {
        harness.setHand(player1, List.of(new ItOfTheHorridSwarm()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities(); // resolve trigger only

        long insects = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Insect") && p.getCard().isToken())
                .count();
        assertThat(insects).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("It of the Horrid Swarm"));
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("It of the Horrid Swarm");
    }
}
