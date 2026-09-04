package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FyndhornElder;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.t.TeferisIsle;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RitualOfSubdual.class, Forest.class, FyndhornElder.class, Island.class,
        Mountain.class, Plains.class, TeferisIsle.class})
class RitualOfSubdualTest extends BaseCardTest {

    @Test
    @DisplayName("Resolves onto the battlefield")
    void resolvesOntoBattlefield() {
        harness.setHand(player1, List.of(new RitualOfSubdual()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Ritual of Subdual");
    }

    @Test
    @DisplayName("Lands produce colorless instead of their normal color")
    void landsProduceColorless() {
        harness.addToBattlefield(player1, new RitualOfSubdual());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Mountain());

        harness.tapPermanent(player1, 1);
        harness.tapPermanent(player1, 2);
        harness.tapPermanent(player1, 3);
        harness.tapPermanent(player1, 4);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(4);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(0);
    }

    @Test
    @DisplayName("Does not replace mana from nonland permanents")
    void doesNotReplaceNonlandMana() {
        harness.addToBattlefield(player1, new RitualOfSubdual());
        addCreatureReady(player1, new FyndhornElder());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Preserves the amount when a land produces multiple mana")
    void preservesAmountForMultiManaLand() {
        harness.addToBattlefield(player1, new RitualOfSubdual());
        harness.addToBattlefield(player1, new TeferisIsle());

        harness.activateAbility(player1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Affects opponent lands too")
    void affectsOpponentLands() {
        harness.addToBattlefield(player1, new RitualOfSubdual());
        harness.addToBattlefield(player2, new Forest());

        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(0);
    }

    @Test
    @DisplayName("Paying cumulative upkeep costs {2}")
    void paysCumulativeUpkeep() {
        Permanent ritual = harness.addToBattlefieldAndReturn(player1, new RitualOfSubdual());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(ritual.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ritual);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(0);
    }

    @Test
    @DisplayName("Cumulative upkeep triggers only during its controller's upkeep")
    void triggersOnlyDuringControllerUpkeep() {
        Permanent ritual = harness.addToBattlefieldAndReturn(player1, new RitualOfSubdual());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(ritual.getCounterCount(CounterType.AGE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ritual);
    }

    @Test
    @DisplayName("Second upkeep costs {4}")
    void secondUpkeepCostsScale() {
        Permanent ritual = harness.addToBattlefieldAndReturn(player1, new RitualOfSubdual());
        ritual.setCounterCount(CounterType.AGE, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(ritual.getCounterCount(CounterType.AGE)).isEqualTo(2);

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ritual);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(0);
    }

    @Test
    @DisplayName("Accepting an unpaid cumulative upkeep sacrifices Ritual of Subdual")
    void cannotPayCumulativeUpkeep() {
        Permanent ritual = harness.addToBattlefieldAndReturn(player1, new RitualOfSubdual());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ritual);
        harness.assertInGraveyard(player1, "Ritual of Subdual");
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Ritual of Subdual")
    void declineSacrifices() {
        Permanent ritual = harness.addToBattlefieldAndReturn(player1, new RitualOfSubdual());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ritual);
        harness.assertInGraveyard(player1, "Ritual of Subdual");
    }
}
