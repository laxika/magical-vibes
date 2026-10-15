package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TattermungeDuo.class, Shock.class, GrizzlyBears.class})
class TattermungeDuoTest extends BaseCardTest {

    private Permanent readyDuo() {
        Permanent duo = addCreatureReady(player1, new TattermungeDuo());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return duo;
    }

    @Test
    @DisplayName("Casting a red spell gives +1/+1 until end of turn")
    void redSpellGivesBoost() {
        Permanent duo = readyDuo();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities(); // resolve the +1/+1 trigger

        assertThat(duo.getPowerModifier()).isEqualTo(1);
        assertThat(duo.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, duo, Keyword.FORESTWALK)).isFalse();
    }

    @Test
    @DisplayName("+1/+1 from a red spell wears off at end of turn")
    void redBoostWearsOff() {
        Permanent duo = readyDuo();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(duo.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(duo.getPowerModifier()).isEqualTo(0);
        assertThat(duo.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Casting a green spell grants forestwalk until end of turn")
    void greenSpellGrantsForestwalk() {
        Permanent duo = readyDuo();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve the forestwalk trigger

        assertThat(gqs.hasKeyword(gd, duo, Keyword.FORESTWALK)).isTrue();
        assertThat(duo.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Forestwalk from a green spell wears off at end of turn")
    void forestwalkWearsOff() {
        Permanent duo = readyDuo();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, duo, Keyword.FORESTWALK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, duo, Keyword.FORESTWALK)).isFalse();
    }

    @Test
    void hybridSpellPaidWithRedTriggersBothAbilities() {
        assertHybridSpellTriggersBothAbilities(ManaColor.RED);
    }

    @Test
    void hybridSpellPaidWithGreenTriggersBothAbilities() {
        assertHybridSpellTriggersBothAbilities(ManaColor.GREEN);
    }

    private void assertHybridSpellTriggersBothAbilities(ManaColor manaColor) {
        Permanent duo = readyDuo();
        harness.addMana(player1, manaColor, 3);
        harness.setHand(player1, List.of(new TattermungeDuo()));

        harness.castCreature(player1, 0);
        assertThat(duo.getPowerModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, duo, Keyword.FORESTWALK)).isFalse();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(duo.getPowerModifier()).isEqualTo(1);
        assertThat(duo.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, duo, Keyword.FORESTWALK)).isTrue();
    }

    @Test
    void opponentHybridSpellDoesNotTriggerEitherAbility() {
        Permanent duo = readyDuo();
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.GREEN, 3);
        harness.setHand(player2, List.of(new TattermungeDuo()));

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(duo.getPowerModifier()).isZero();
        assertThat(duo.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, duo, Keyword.FORESTWALK)).isFalse();
    }

    @Test
    void castingDuoDoesNotTriggerItsOwnAbilities() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 3);
        TattermungeDuo card = new TattermungeDuo();
        harness.setHand(player1, List.of(card));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent duo = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(card.getId()))
                .findFirst().orElseThrow();
        assertThat(duo.getPowerModifier()).isZero();
        assertThat(duo.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, duo, Keyword.FORESTWALK)).isFalse();
    }

    @Test
    void repeatedRedSpellsGiveCumulativeBoosts() {
        Permanent duo = readyDuo();
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setHand(player1, List.of(new Shock(), new Shock()));

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(duo.getPowerModifier()).isEqualTo(2);
        assertThat(duo.getToughnessModifier()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, duo, Keyword.FORESTWALK)).isFalse();
    }
}
