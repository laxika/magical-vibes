package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.w.Weakness;
import com.github.laxika.magicalvibes.cards.b.BalduvianBarbarians;
import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.g.GiantCindermaw;
import com.github.laxika.magicalvibes.cards.k.KnightOfStromgald;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SwordsToPlowshares.class, BalduvianBears.class, BalduvianBarbarians.class, GiantCindermaw.class, KnightOfStromgald.class, Plains.class, GrizzlyBears.class, HillGiant.class, Weakness.class})
class SwordsToPlowsharesTest extends BaseCardTest {

    private void giveSwords() {
        harness.setHand(player1, List.of(new SwordsToPlowshares()));
        harness.addMana(player1, ManaColor.WHITE, 1);
    }

    @Test
    @DisplayName("Exiles the target creature and its controller gains life equal to its power")
    void exilesCreatureAndControllerGainsLife() {
        Permanent target = addCreatureReady(player2, new BalduvianBears());
        harness.setLife(player2, 20);
        giveSwords();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Balduvian Bears");
        harness.assertNotInGraveyard(player2, "Balduvian Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Balduvian Bears"));

        harness.assertLife(player2, 22);
    }

    @Test
    @DisplayName("Life gain scales with the creature's power")
    void lifeGainScalesWithPower() {
        Permanent target = addCreatureReady(player2, new BalduvianBarbarians());
        harness.setLife(player2, 20);
        giveSwords();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Balduvian Barbarians");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Balduvian Barbarians"));
        harness.assertLife(player2, 23);
    }

    @Test
    @DisplayName("Life goes to the controller of the exiled creature (caster's own creature)")
    void lifeGoesToControllerOfExiledCreature() {
        Permanent target = addCreatureReady(player1, new BalduvianBears());
        harness.setLife(player1, 20);
        giveSwords();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player1, "Balduvian Bears");
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        addCreatureReady(player2, new BalduvianBears());
        giveSwords();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Plains());
        giveSwords();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature with protection from white")
    void cannotTargetCreatureWithProtectionFromWhite() {
        Permanent target = addCreatureReady(player2, new KnightOfStromgald());
        giveSwords();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Uses the target's power at resolution")
    void usesPowerAtResolution() {
        Permanent target = addCreatureReady(player2, new BalduvianBears());
        harness.setLife(player2, 20);
        giveSwords();

        harness.castInstant(player1, 0, target.getId());
        target.setPowerModifier(1);
        harness.passBothPriorities();

        harness.assertLife(player2, 23);
    }

    @Test
    @DisplayName("Does not gain life when the target's power is negative")
    void doesNotGainLifeForNegativePower() {
        Permanent target = addCreatureReady(player2, new BalduvianBears());
        target.setPowerModifier(-3);
        harness.setLife(player2, 20);
        giveSwords();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Balduvian Bears");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Exiles a creature before its life-gain prevention applies")
    void exilesCreatureBeforeItsLifeGainPreventionApplies() {
        Permanent target = addCreatureReady(player2, new GiantCindermaw());
        harness.setLife(player2, 20);
        giveSwords();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Giant Cindermaw");
        harness.assertLife(player2, 24);
    }

    @Test
    @DisplayName("Fizzles with no life gain if the target leaves before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent target = addCreatureReady(player2, new BalduvianBears());
        harness.setLife(player2, 20);
        giveSwords();

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Balduvian Bears"));
    }
}
