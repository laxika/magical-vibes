package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CyclopsElectromancer.class, Divination.class, GrizzlyBears.class, Shock.class})
class CyclopsElectromancerTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to instant and sorcery cards in its controller's graveyard")
    void dealsDamageForInstantAndSorceryCardsInGraveyard() {
        GrizzlyBears target = new GrizzlyBears();
        target.setToughness(8);
        Permanent targetPermanent = harness.addToBattlefieldAndReturn(player2, target);
        harness.setGraveyard(player1, List.of(new Shock(), new Shock(), new Divination(), new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new Shock()));

        harness.setHand(player1, List.of(new CyclopsElectromancer()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0, 0, targetPermanent.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(targetPermanent.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a creature its controller controls")
    void cannotTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CyclopsElectromancer()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Deals no damage when only the opponent has instant and sorcery cards")
    void dealsZeroDamageWithNoQualifyingCards() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new Shock(), new Divination()));
        harness.setHand(player1, List.of(new CyclopsElectromancer()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Cyclops Electromancer");
    }

    @Test
    @DisplayName("Counts cards added to the graveyard before the trigger resolves")
    void countsCardsAddedBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setHand(player1, List.of(new CyclopsElectromancer()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        harness.setGraveyard(player1, List.of(new Shock(), new Divination()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not count cards removed from the graveyard before resolution")
    void excludesCardsRemovedBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Shock(), new Divination()));
        harness.setHand(player1, List.of(new CyclopsElectromancer()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        harness.setGraveyard(player1, List.of(new Shock()));
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The trigger still deals damage after Cyclops Electromancer dies")
    void triggerResolvesAfterSourceDies() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setHand(player1, List.of(new CyclopsElectromancer()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        Permanent source = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof CyclopsElectromancer)
                .findFirst().orElseThrow();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, source.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Cyclops Electromancer");
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can enter the battlefield when no opponent controls a creature")
    void entersWithNoLegalTarget() {
        harness.setGraveyard(player1, List.of(new Shock(), new Divination()));

        harness.castFromHand(player1, new CyclopsElectromancer(), "{4}{R}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Cyclops Electromancer");
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
