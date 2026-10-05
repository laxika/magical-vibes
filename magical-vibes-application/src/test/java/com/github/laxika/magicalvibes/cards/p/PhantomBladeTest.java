package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PhantomBlade.class, GrizzlyBears.class})
class PhantomBladeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters attached to your creature, destroys another creature, and grants its bonuses")
    void entersAttachesDestroysAndGrantsBonuses() {
        Permanent equippedCreature = addCreatureReady(player1);
        Permanent destroyedCreature = addCreatureReady(player2);
        castPhantomBlade(equippedCreature.getId(), destroyedCreature.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent blade = findPermanent(player1, "Phantom Blade");
        assertThat(blade.getAttachedTo()).isEqualTo(equippedCreature.getId());
        assertThat(gqs.getEffectivePower(gd, equippedCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, equippedCreature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, equippedCreature, Keyword.MENACE)).isTrue();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Both ETB targets are optional")
    void entersWithoutChoosingEitherTarget() {
        Permanent ownCreature = addCreatureReady(player1);
        Permanent opponentCreature = addCreatureReady(player2);
        castPhantomBlade();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        Permanent blade = findPermanent(player1, "Phantom Blade");
        assertThat(blade.getAttachedTo()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentCreature);
    }

    @Test
    @DisplayName("Equip {2} attaches Phantom Blade to a creature you control")
    void equipAttachesToCreatureYouControl() {
        Permanent blade = addBladeReady();
        Permanent creature = addCreatureReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("The destroy target must be different from the attach target")
    void cannotChooseTheSameCreatureForBothTargets() {
        Permanent creature = addCreatureReady(player1);

        assertThatThrownBy(() -> castCard(List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canAttachWithoutDestroyingACreature() {
        Permanent creature = addCreatureReady(player1);
        Permanent blade = harness.enterBattlefieldAndReturn(player1, new PhantomBlade());
        harness.handlePermanentChosen(player1, creature.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(creature.getId());
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void canDestroyWithoutAttaching() {
        Permanent ownCreature = addCreatureReady(player1);
        Permanent opponentCreature = addCreatureReady(player2);
        Permanent blade = harness.enterBattlefieldAndReturn(player1, new PhantomBlade());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void canDestroyYourOwnCreatureWithoutAttaching() {
        Permanent creature = addCreatureReady(player1);
        Permanent blade = harness.enterBattlefieldAndReturn(player1, new PhantomBlade());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void destroysTheOtherTargetWhenTheAttachTargetLeaves() {
        Permanent creature = addCreatureReady(player1);
        Permanent opponentCreature = addCreatureReady(player2);
        Permanent blade = harness.enterBattlefieldAndReturn(player1, new PhantomBlade());
        harness.handlePermanentChosen(player1, creature.getId());
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerGraveyards.get(player1.getId()).add(creature.getCard());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isNull();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void attachesWhenTheDestroyTargetLeaves() {
        Permanent creature = addCreatureReady(player1);
        Permanent opponentCreature = addCreatureReady(player2);
        Permanent blade = harness.enterBattlefieldAndReturn(player1, new PhantomBlade());
        harness.handlePermanentChosen(player1, creature.getId());
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        gd.playerBattlefields.get(player2.getId()).remove(opponentCreature);
        gd.playerGraveyards.get(player2.getId()).add(opponentCreature.getCard());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(creature.getId());
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void reequippingMovesTheBonusesToTheNewCreature() {
        Permanent blade = addBladeReady();
        Permanent firstCreature = addCreatureReady(player1);
        Permanent secondCreature = addCreatureReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, firstCreature.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, secondCreature.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(secondCreature.getId());
        assertThat(gqs.getEffectivePower(gd, firstCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, firstCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, firstCreature, Keyword.MENACE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, secondCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, secondCreature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, secondCreature, Keyword.MENACE)).isTrue();
    }

    private void castPhantomBlade(java.util.UUID... targets) {
        castCard(List.of(targets));
    }

    private void castCard(List<java.util.UUID> targets) {
        harness.setHand(player1, List.of(new PhantomBlade()));
        addManaForCast();
        gs.playCard(gd, player1, 0, 0, null, null, targets, List.of());
    }

    private void addManaForCast() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private Permanent addCreatureReady(com.github.laxika.magicalvibes.model.Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        creature.setSummoningSick(false);
        return creature;
    }

    private Permanent addBladeReady() {
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new PhantomBlade());
        blade.setSummoningSick(false);
        return blade;
    }
}
