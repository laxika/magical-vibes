package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DecimatorOfTheProvincesTest extends BaseCardTest {

    @Test
    @DisplayName("Hardcast: when cast, own creatures get +2/+2 and trample until end of turn")
    void hardcastBoostsOwnCreatures() {
        Permanent ally = addReadyCreature(player1, new GrizzlyBears());
        Permanent opponent = addReadyCreature(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DecimatorOfTheProvinces()));
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve cast trigger
        harness.passBothPriorities(); // resolve creature spell

        assertThat(ally.getEffectivePower()).isEqualTo(4);
        assertThat(ally.getEffectiveToughness()).isEqualTo(4);
        assertThat(ally.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(opponent.getEffectivePower()).isEqualTo(2);
        assertThat(opponent.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Decimator of the Provinces"));
    }

    @Test
    @DisplayName("Emerge: sacrifice a creature, pay emerge cost reduced by its mana value")
    void emergeSacrificesAndReducesCost() {
        harness.addToBattlefield(player1, new GrizzlyBears()); // MV 2
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        Permanent other = addReadyCreature(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new DecimatorOfTheProvinces()));
        // Emerge {6}{G}{G}{G} reduced by 2 → {4}{G}{G}{G}
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(bearsId));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(other.getEffectivePower()).isEqualTo(4);
        assertThat(other.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Decimator of the Provinces"))
                .noneMatch(p -> p.getId().equals(bearsId));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Emerge fails without enough mana after reduction")
    void emergeFailsWithInsufficientMana() {
        harness.addToBattlefield(player1, new GrizzlyBears()); // MV 2
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.setHand(player1, List.of(new DecimatorOfTheProvinces()));
        // Need {4}{G}{G}{G} after reduction; only {3}{G}{G}{G} available
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() ->
                harness.castCreatureWithAlternateCost(player1, 0, List.of(bearsId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cast trigger resolves before the creature spell")
    void castTriggerResolvesBeforeCreature() {
        Permanent ally = addReadyCreature(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DecimatorOfTheProvinces()));
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities(); // resolve trigger only

        assertThat(ally.getEffectivePower()).isEqualTo(4);
        assertThat(ally.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Decimator of the Provinces"));
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Decimator of the Provinces");
    }

    @Test
    @DisplayName("Cast trigger boosts wear off at end of turn")
    void boostsWearOffAtEndOfTurn() {
        Permanent ally = addReadyCreature(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DecimatorOfTheProvinces()));
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(ally.getEffectivePower()).isEqualTo(4);
        assertThat(ally.hasKeyword(Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ally.getEffectivePower()).isEqualTo(2);
        assertThat(ally.getEffectiveToughness()).isEqualTo(2);
        assertThat(ally.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
