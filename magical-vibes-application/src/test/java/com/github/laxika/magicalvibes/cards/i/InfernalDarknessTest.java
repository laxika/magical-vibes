package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AncientTomb;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InfernalDarknessTest extends BaseCardTest {

    @Test
    @DisplayName("Resolves onto the battlefield")
    void resolvesOntoBattlefield() {
        harness.setHand(player1, List.of(new InfernalDarkness()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Infernal Darkness");
    }

    @Test
    @DisplayName("Lands produce black instead of their normal color")
    void landsProduceBlack() {
        harness.addToBattlefield(player1, new InfernalDarkness());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Mountain());

        harness.tapPermanent(player1, 1);
        harness.tapPermanent(player1, 2);
        harness.tapPermanent(player1, 3);
        harness.tapPermanent(player1, 4);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(4);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(0);
    }

    @Test
    @DisplayName("Affects opponent lands too")
    void affectsOpponentLands() {
        harness.addToBattlefield(player1, new InfernalDarkness());
        harness.addToBattlefield(player2, new Forest());

        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(0);
    }

    @Test
    @DisplayName("Preserves the amount when a land produces multiple mana")
    void preservesAmountForMultiManaLand() {
        harness.addToBattlefield(player1, new InfernalDarkness());
        harness.addToBattlefield(player1, new AncientTomb());
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(0);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Paying cumulative upkeep costs {B} and 1 life")
    void paysCumulativeUpkeep() {
        Permanent darkness = harness.addToBattlefieldAndReturn(player1, new InfernalDarkness());
        int lifeBefore = gd.getLife(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(darkness.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(darkness);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(0);
    }

    @Test
    @DisplayName("Second upkeep costs {B}{B} and 2 life")
    void secondUpkeepCostsScale() {
        Permanent darkness = harness.addToBattlefieldAndReturn(player1, new InfernalDarkness());
        darkness.setCounterCount(CounterType.AGE, 1);
        int lifeBefore = gd.getLife(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(darkness.getCounterCount(CounterType.AGE)).isEqualTo(2);

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(darkness);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(0);
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Infernal Darkness")
    void declineSacrifices() {
        Permanent darkness = harness.addToBattlefieldAndReturn(player1, new InfernalDarkness());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(darkness);
        harness.assertInGraveyard(player1, "Infernal Darkness");
    }
}
