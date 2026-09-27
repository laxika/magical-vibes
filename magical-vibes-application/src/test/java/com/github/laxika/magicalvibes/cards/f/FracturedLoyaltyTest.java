package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.cards.a.AweStrike;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.o.Override;
import com.github.laxika.magicalvibes.cards.r.Regress;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FracturedLoyalty.class, AlphaMyr.class, AweStrike.class, IcyManipulator.class,
        Override.class, Regress.class})
class FracturedLoyaltyTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Fractured Loyalty attaches it to the target creature")
    void resolvingAttachesToTargetCreature() {
        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        harness.setHand(player1, List.of(new FracturedLoyalty()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Fractured Loyalty");
        assertThat(aura.isAttached()).isTrue();
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Fractured Loyalty cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        addCreatureReady(player2, new AlphaMyr());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new IcyManipulator());
        harness.setHand(player1, List.of(new FracturedLoyalty()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Controller of a targeting spell gains control of the enchanted creature")
    void targetingSpellControllerGainsControl() {
        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        addAura(player1, creature);

        harness.setHand(player2, List.of(new AweStrike()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castInstant(player2, 0, creature.getId());

        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Targeting Fractured Loyalty itself does not trigger its control change")
    void doesNotTriggerWhenAuraItselfIsTargeted() {
        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        Permanent aura = addAura(player1, creature);

        harness.setHand(player2, List.of(new Regress()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.castInstant(player2, 0, aura.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Regress");
    }

    @Test
    @DisplayName("Control gained from Fractured Loyalty remains after the Aura leaves")
    void controlRemainsAfterAuraLeaves() {
        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        Permanent aura = addAura(player1, creature);

        harness.setHand(player2, List.of(new AweStrike()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("The control trigger uses the enchanted creature if the Aura leaves before resolution")
    void triggerUsesLastKnownAttachmentWhenAuraLeaves() {
        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        Permanent aura = addAura(player1, creature);

        harness.setHand(player2, List.of(new AweStrike()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castInstant(player2, 0, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("The control trigger remembers the controller if the targeting spell is countered")
    void triggerUsesTargetingSpellControllerWhenSpellIsCountered() {
        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        addAura(player1, creature);

        AweStrike aweStrike = new AweStrike();
        harness.setHand(player2, List.of(aweStrike));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castInstant(player2, 0, creature.getId());

        harness.setHand(player1, List.of(new Override()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castInstant(player1, 0, aweStrike.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Awe Strike");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Controller of a targeting ability gains control of the enchanted creature")
    void targetingAbilityControllerGainsControl() {
        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        addAura(player1, creature);

        Permanent icyManipulator = harness.addToBattlefieldAndReturn(player2, new IcyManipulator());
        icyManipulator.setSummoningSick(false);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(icyManipulator), null, creature.getId());

        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(creature.getId()));
    }

    private Permanent addAura(Player controller, Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new FracturedLoyalty());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
