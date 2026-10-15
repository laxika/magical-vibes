package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

@CardUsed({ScornedVillager.class})
class ScornedVillagerTest extends BaseCardTest {



    @Test
    @DisplayName("Tapping Scorned Villager produces one green mana")
    void tappingFrontFaceProducesOneGreenMana() {
        Permanent perm = addCreatureReady(player1, new ScornedVillager());

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(perm.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping Moonscarred Werewolf produces two green mana")
    void tappingBackFaceProducesTwoGreenMana() {
        Permanent perm = addCreatureReady(player1, new ScornedVillager());
        advanceFromUntapToResolveUpkeepTrigger(player1);

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(perm.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Transforms to Moonscarred Werewolf when no spells were cast last turn")
    void transformsWhenNoSpellsCastLastTurn() {
        harness.addToBattlefield(player1, new ScornedVillager());
        Permanent villager = findPermanent(player1, "Scorned Villager");

        gd.spellsCastLastTurn.clear();
        advanceFromUntapToResolveUpkeepTrigger(player1);

        assertThat(villager.isTransformed()).isTrue();
        assertThat(villager.getCard().getName()).isEqualTo("Moonscarred Werewolf");
        assertThat(gqs.getEffectivePower(gd, villager)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, villager)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not transform when a spell was cast last turn")
    void doesNotTransformWhenSpellCastLastTurn() {
        harness.addToBattlefield(player1, new ScornedVillager());
        Permanent villager = findPermanent(player1, "Scorned Villager");

        gd.spellsCastLastTurn.put(player1.getId(), 1);

        advanceToUpkeep(player1);

        assertThat(villager.isTransformed()).isFalse();
        assertThat(villager.getCard().getName()).isEqualTo("Scorned Villager");
    }

    @Test
    @DisplayName("Moonscarred Werewolf transforms back when a player cast two or more spells last turn")
    void transformsBackWhenTwoSpellsCastLastTurn() {
        harness.addToBattlefield(player1, new ScornedVillager());
        Permanent villager = findPermanent(player1, "Scorned Villager");

        gd.spellsCastLastTurn.clear();
        advanceFromUntapToResolveUpkeepTrigger(player1);
        assertThat(villager.isTransformed()).isTrue();

        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        advanceFromUntapToResolveUpkeepTrigger(player2);

        assertThat(villager.isTransformed()).isFalse();
        assertThat(villager.getCard().getName()).isEqualTo("Scorned Villager");
        assertThat(gqs.getEffectivePower(gd, villager)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, villager)).isEqualTo(1);
    }

    @Test
    @DisplayName("Moonscarred Werewolf does not transform back when only one spell was cast last turn")
    void doesNotTransformBackWithOnlyOneSpellCastLastTurn() {
        harness.addToBattlefield(player1, new ScornedVillager());
        Permanent villager = findPermanent(player1, "Scorned Villager");

        gd.spellsCastLastTurn.clear();
        advanceFromUntapToResolveUpkeepTrigger(player1);
        assertThat(villager.isTransformed()).isTrue();

        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player1.getId(), 1);
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        advanceToUpkeep(player2);

        assertThat(villager.isTransformed()).isTrue();
        assertThat(villager.getCard().getName()).isEqualTo("Moonscarred Werewolf");
    }

    @Test
    @DisplayName("Transform triggers on opponent's upkeep too")
    void transformTriggersOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new ScornedVillager());
        Permanent villager = findPermanent(player1, "Scorned Villager");

        gd.spellsCastLastTurn.clear();
        advanceFromUntapToResolveUpkeepTrigger(player2);

        assertThat(villager.isTransformed()).isTrue();
        assertThat(villager.getCard().getName()).isEqualTo("Moonscarred Werewolf");
    }

    @Test
    @DisplayName("Moonscarred Werewolf has vigilance")
    void moonscarredWerewolfHasVigilance() {
        harness.addToBattlefield(player1, new ScornedVillager());
        Permanent villager = findPermanent(player1, "Scorned Villager");

        gd.spellsCastLastTurn.clear();
        advanceFromUntapToResolveUpkeepTrigger(player1);

        assertThat(villager.isTransformed()).isTrue();
        declareAttackersAndPrepareBlockers(List.of(0));
        assertThat(villager.isTapped()).isFalse();
        harness.tapPermanent(player1, 0);
        assertThat(villager.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Summoning sickness persists through transformation on the opponent's upkeep")
    void summoningSicknessPersistsThroughTransformation() {
        Permanent villager = harness.addToBattlefieldAndReturn(player1, new ScornedVillager());

        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        advanceFromUntapToResolveUpkeepTrigger(player2);

        assertThat(villager.isTransformed()).isTrue();
        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Transforming on the opponent's upkeep preserves the tapped state")
    void transformationPreservesTappedState() {
        Permanent villager = addCreatureReady(player1, new ScornedVillager());
        harness.tapPermanent(player1, 0);

        advanceFromUntapToResolveUpkeepTrigger(player2);

        assertThat(villager.isTransformed()).isTrue();
        assertThat(villager.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Werewolf stays transformed when no spells were cast last turn")
    void backFaceRemainsWithNoSpellsCast() {
        Permanent villager = harness.addToBattlefieldAndReturn(player1, new ScornedVillager());
        advanceFromUntapToResolveUpkeepTrigger(player1);

        advanceFromUntapToResolveUpkeepTrigger(player2);

        assertThat(villager.isTransformed()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private void advanceFromUntapToResolveUpkeepTrigger(Player activePlayer) {
        advanceToUpkeep(activePlayer);
        resolveAllTriggers();
    }
}
