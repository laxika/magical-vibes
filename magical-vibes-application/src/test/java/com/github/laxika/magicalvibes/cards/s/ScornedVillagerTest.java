package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.MoonscarredWerewolf;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScornedVillagerTest extends BaseCardTest {

    

    

    @Test
    @DisplayName("Tapping Scorned Villager produces one green mana")
    void tappingFrontFaceProducesOneGreenMana() {
        Permanent perm = new Permanent(new ScornedVillager());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping Moonscarred Werewolf produces two green mana")
    void tappingBackFaceProducesTwoGreenMana() {
        Permanent perm = new Permanent(new MoonscarredWerewolf());
        perm.setSummoningSick(false);
        perm.setTransformed(true);
        gd.playerBattlefields.get(player1.getId()).add(perm);

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
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

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

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

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

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
        assertThat(gqs.hasKeyword(gd, villager, Keyword.VIGILANCE)).isTrue();
    }

    private void advanceFromUntapToResolveUpkeepTrigger(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
