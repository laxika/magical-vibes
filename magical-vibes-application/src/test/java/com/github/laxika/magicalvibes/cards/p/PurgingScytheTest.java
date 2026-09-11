package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.ArgothianSwine;
import com.github.laxika.magicalvibes.cards.g.GorillaWarrior;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PurgingScythe.class, GorillaWarrior.class, ArgothianSwine.class})
class PurgingScytheTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to the creature with the least toughness")
    void dealsDamageToCreatureWithLeastToughness() {
        harness.addToBattlefield(player1, new PurgingScythe());
        Permanent leastToughness = addCreatureReady(player2, new GorillaWarrior());
        Permanent larger = addCreatureReady(player2, new ArgothianSwine());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(leastToughness);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(larger);
    }

    @Test
    @DisplayName("The controller chooses among creatures tied for least toughness")
    void controllerChoosesAmongTiedCreatures() {
        harness.addToBattlefield(player1, new PurgingScythe());
        Permanent first = addCreatureReady(player1, new GorillaWarrior());
        Permanent second = addCreatureReady(player2, new GorillaWarrior());
        addCreatureReady(player2, new ArgothianSwine());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(first.getId(), second.getId());

        harness.handlePermanentChosen(player1, second.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(first);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(second);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new PurgingScythe());
        Permanent creature = addCreatureReady(player2, new GorillaWarrior());

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }

    @Test
    @DisplayName("Deals nonlethal damage to a creature with greater toughness")
    void dealsNonlethalDamageToCreatureWithGreaterToughness() {
        harness.addToBattlefield(player1, new PurgingScythe());
        Permanent creature = addCreatureReady(player2, new ArgothianSwine());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(creature.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does nothing when there are no creatures")
    void doesNothingWhenThereAreNoCreatures() {
        harness.addToBattlefield(player1, new PurgingScythe());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Chooses the least-tough creature when the ability resolves")
    void choosesLeastToughCreatureWhenAbilityResolves() {
        harness.addToBattlefield(player1, new PurgingScythe());
        Permanent originalLeastToughness = addCreatureReady(player2, new ArgothianSwine());

        advanceToUpkeep(player1);
        Permanent newLeastToughness = addCreatureReady(player2, new GorillaWarrior());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(originalLeastToughness);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(newLeastToughness);
    }
}
