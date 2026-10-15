package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.ChamberSentry;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainportBell;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Incinerate;
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

@CardUsed({HelgaSkittishSeer.class, ChamberSentry.class, Forest.class, FountainportBell.class,
        GrizzlyBears.class, HillGiant.class, Incinerate.class})
class HelgaSkittishSeerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a creature with mana value 4 or greater draws, gains life, and grows Helga")
    void qualifyingCreatureSpellTriggersHelga() {
        Permanent helga = addCreatureReady(player1, new HelgaSkittishSeer());
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(new Forest()));
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

    @Test
    @DisplayName("An X creature spell with chosen mana value four triggers Helga before resolving")
    void xCreatureAtThresholdTriggersHelga() {
        Permanent helga = addCreatureReady(player1, new HelgaSkittishSeer());
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new ChamberSentry()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castArtifact(player1, 0, 4);
        harness.passBothPriorities();

        assertThat(helga.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player1, 21);
        assertThat(gd.playerHands.get(player1.getId())).singleElement().isInstanceOf(Forest.class);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof ChamberSentry);
    }

    @Test
    @DisplayName("An X creature spell with chosen mana value three does not trigger Helga")
    void xCreatureBelowThresholdDoesNotTriggerHelga() {
        Permanent helga = addCreatureReady(player1, new HelgaSkittishSeer());
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new ChamberSentry()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castArtifact(player1, 0, 3);
        harness.passBothPriorities();

        assertThat(helga.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof ChamberSentry);
    }

    @Test
    @DisplayName("An opponent's qualifying creature spell does not trigger Helga")
    void opponentsCreatureDoesNotTriggerHelga() {
        Permanent helga = addCreatureReady(player1, new HelgaSkittishSeer());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new HillGiant()));
        harness.addMana(player2, ManaColor.RED, 4);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(helga.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof HillGiant);
    }

    @Test
    @DisplayName("Helga's mana cannot pay for a noncreature spell")
    void restrictedManaCannotCastNoncreature() {
        addCreatureReady(player1, new HelgaSkittishSeer());
        harness.setHand(player1, List.of(new FountainportBell()));
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getCreatureSpellManaValueAtLeastFourOrXOnlyMana(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Helga's mana cannot pay for an activated ability")
    void restrictedManaCannotPayForActivatedAbility() {
        addCreatureReady(player1, new HelgaSkittishSeer());
        harness.addToBattlefield(player1, new FountainportBell());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getCreatureSpellManaValueAtLeastFourOrXOnlyMana(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Helga's trigger still draws and gains life if Helga dies before it resolves")
    void triggerResolvesAfterHelgaDies() {
        Permanent helga = addCreatureReady(player1, new HelgaSkittishSeer());
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setHand(player2, List.of(new Incinerate()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.castAndResolveInstant(player2, 0, helga.getId());
        harness.assertNotOnBattlefield(player1, "Helga, Skittish Seer");
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        assertThat(gd.playerHands.get(player1.getId())).singleElement().isInstanceOf(Forest.class);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof HillGiant);
    }

    @Test
    @DisplayName("Helga's mana ability resolves immediately and produces only the chosen color")
    void manaAbilityDoesNotUseStack() {
        Permanent helga = addCreatureReady(player1, new HelgaSkittishSeer());
        helga.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.stack).isEmpty();
        assertThat(helga.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId())
                .getCreatureSpellManaValueAtLeastFourOrXOnlyMana(ManaColor.WHITE)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getCreatureSpellManaValueAtLeastFourOrXOnlyManaTotal()).isEqualTo(3);
    }
}
