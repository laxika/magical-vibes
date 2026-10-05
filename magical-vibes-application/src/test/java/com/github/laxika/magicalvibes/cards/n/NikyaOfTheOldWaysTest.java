package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GruulGuildgate;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.PlazaOfHarmony;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NikyaOfTheOldWays.class, Forest.class, GrizzlyBears.class, Shock.class,
        GruulGuildgate.class, LlanowarElves.class, PlazaOfHarmony.class, TurnToFrog.class})
class NikyaOfTheOldWaysTest extends BaseCardTest {

    @Test
    @DisplayName("Its controller cannot cast noncreature spells, but opponents can")
    void restrictsNoncreatureSpellsToController() {
        harness.addToBattlefield(player1, new NikyaOfTheOldWays());
        var target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player2, 0, target.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Its controller can cast creature spells")
    void allowsCreatureSpells() {
        harness.addToBattlefield(player1, new NikyaOfTheOldWays());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Its controller's land produces one additional mana")
    void addsManaWhenControllerTapsLand() {
        harness.addToBattlefield(player1, new NikyaOfTheOldWays());
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's land does not produce additional mana")
    void doesNotAddManaWhenOpponentTapsLand() {
        harness.addToBattlefield(player1, new NikyaOfTheOldWays());
        harness.addToBattlefield(player2, new Forest());

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("A choice land adds extra mana only of the type actually produced")
    void choiceLandAddsOnlyChosenType() {
        harness.addToBattlefield(player1, new NikyaOfTheOldWays());
        addCreatureReady(player1, new GruulGuildgate());

        harness.activateAbility(player1, 1, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Colorless mana from a land also receives one additional mana")
    void addsColorlessManaImmediately() {
        harness.addToBattlefield(player1, new NikyaOfTheOldWays());
        harness.addToBattlefield(player1, new PlazaOfHarmony());

        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tapping a creature for mana does not trigger Nikya")
    void doesNotAddManaForNonland() {
        harness.addToBattlefield(player1, new NikyaOfTheOldWays());
        addCreatureReady(player1, new LlanowarElves());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Nikya does not add mana after losing all abilities")
    void abilityRemovalDisablesManaTrigger() {
        var nikya = harness.addToBattlefieldAndReturn(player1, new NikyaOfTheOldWays());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player2, List.of(new TurnToFrog()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, nikya.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasLostAllAbilities(gd, nikya)).isTrue();
        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }
}
