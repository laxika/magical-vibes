package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
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

@CardUsed({Cryoshatter.class, Boomerang.class, FountainOfYouth.class, GrizzlyBears.class, HillGiant.class, Shock.class})
class CryoshatterTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Cryoshatter attaches it and gives the creature -5/-0")
    void resolvingAttachesAndDebuffs() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Cryoshatter()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Cryoshatter");
        assertThat(aura.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(-3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("When the enchanted creature becomes tapped, Cryoshatter destroys it")
    void tappingEnchantedCreatureDestroysIt() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setSummoningSick(false);
        attachAura(bears);

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Cryoshatter");
    }

    @Test
    @DisplayName("When the enchanted creature is dealt damage, Cryoshatter destroys it")
    void damageToEnchantedCreatureDestroysIt() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        attachAura(giant);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, giant.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertNotOnBattlefield(player1, "Cryoshatter");
    }

    @Test
    @DisplayName("Cryoshatter cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());

        harness.setHand(player1, List.of(new Cryoshatter()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void attachAura(Permanent creature) {
        Permanent aura = new Permanent(new Cryoshatter());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
    }

    @Test
    @DisplayName("Attaching Cryoshatter to an already tapped creature does not destroy it")
    void attachingToTappedCreatureDoesNotTrigger() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setTapped(true);
        harness.setHand(player1, List.of(new Cryoshatter()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, bears.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(findPermanent(player1, "Cryoshatter").getAttachedTo()).isEqualTo(bears.getId());
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Removing Cryoshatter in response to its tap trigger does not save the creature")
    void tapTriggerSurvivesAuraRemoval() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        attachAura(bears);
        Permanent aura = findPermanent(player1, "Cryoshatter");
        declareAttackers(player2, List.of(0));

        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, aura.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Cryoshatter");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Removing Cryoshatter in response to its damage trigger does not save the creature")
    void damageTriggerSurvivesAuraRemoval() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        attachAura(giant);
        Permanent aura = findPermanent(player1, "Cryoshatter");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, giant.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, aura.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Cryoshatter");
        harness.assertInGraveyard(player2, "Hill Giant");
    }
}
