package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.ChamberSentry;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HelgaSkittishSeer.class, ChamberSentry.class, Forest.class, GrizzlyBears.class, HillGiant.class})
class HelgaSkittishSeerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a creature with mana value 4 or greater draws, gains life, and grows Helga")
    void qualifyingCreatureSpellTriggersHelga() {
        Permanent helga = addCreatureReady(player1, new HelgaSkittishSeer());
        harness.setLife(player1, 20);
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new Forest());
        harness.setHand(player1, List.of(new HillGiant()));
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(helga.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getCreatureSpellManaValueAtLeastFourOrXOnlyMana(ManaColor.RED)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).singleElement()
                .isInstanceOf(Forest.class);
    }

    @Test
    @DisplayName("Casting a creature with mana value less than 4 does not trigger Helga")
    void smallerCreatureSpellDoesNotTriggerHelga() {
        Permanent helga = addCreatureReady(player1, new HelgaSkittishSeer());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(helga.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Helga adds mana equal to her current power")
    void manaAbilityUsesCurrentPower() {
        Permanent helga = addCreatureReady(player1, new HelgaSkittishSeer());
        helga.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId())
                .getCreatureSpellManaValueAtLeastFourOrXOnlyMana(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    @DisplayName("Helga's restricted mana can cast a creature spell with {X} in its mana cost")
    void restrictedManaCastsXCreature() {
        addCreatureReady(player1, new HelgaSkittishSeer());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");
        harness.setHand(player1, List.of(new ChamberSentry()));

        harness.castArtifact(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof ChamberSentry);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getCreatureSpellManaValueAtLeastFourOrXOnlyMana(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Helga's restricted mana cannot cast a smaller non-X creature")
    void restrictedManaCannotCastSmallerNonXCreature() {
        addCreatureReady(player1, new HelgaSkittishSeer());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getCreatureSpellManaValueAtLeastFourOrXOnlyMana(ManaColor.RED)).isEqualTo(1);
    }
}
