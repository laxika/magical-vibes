package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.ArcaneFlight;
import com.github.laxika.magicalvibes.cards.f.FaithsFetters;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArcaneFlight.class, FaithsFetters.class, Forest.class, GrizzlyBears.class, VenomousVines.class})
class VenomousVinesTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys an enchanted permanent")
    void destroysEnchantedPermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attachAura(creature);

        castAndResolveAt(creature);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Destroys an enchanted noncreature permanent")
    void destroysEnchantedNoncreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FaithsFetters());
        aura.setAttachedTo(land.getId());

        castAndResolveAt(land);

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Cannot target an unenchanted permanent")
    void cannotTargetUnenchantedPermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new VenomousVines()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an enchanted permanent");
    }

    @Test
    @DisplayName("Fizzles if the target is no longer enchanted")
    void fizzlesIfTargetIsNoLongerEnchanted() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent aura = attachAura(creature);

        castAt(creature);
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can destroy its controller's enchanted permanent")
    void destroysOwnEnchantedPermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new ArcaneFlight());
        aura.setAttachedTo(creature.getId());

        castAndResolveAt(creature);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Arcane Flight");
    }

    @Test
    @DisplayName("Still destroys the target if one of two Auras leaves")
    void destroysTargetWhileAnotherAuraRemains() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent removedAura = attachAura(creature);
        attachAura(creature);

        castAt(creature);
        gd.playerBattlefields.get(player1.getId()).remove(removedAura);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Arcane Flight");
    }

    @Test
    @DisplayName("Can destroy an Aura enchanted by another Aura")
    void destroysEnchantedAuraWithoutDestroyingItsHost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent targetAura = attachAura(creature);
        Permanent outerAura = harness.addToBattlefieldAndReturn(player2, new FaithsFetters());
        outerAura.setAttachedTo(targetAura.getId());

        castAndResolveAt(targetAura);

        harness.assertInGraveyard(player1, "Arcane Flight");
        harness.assertInGraveyard(player2, "Faith's Fetters");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("An Aura is not enchanted merely because it enchants a permanent")
    void cannotTargetAuraWithoutAnAuraAttachedToIt() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent aura = attachAura(creature);
        prepareSpell();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, aura.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an enchanted permanent");
    }

    private Permanent attachAura(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ArcaneFlight());
        aura.setAttachedTo(creature.getId());
        return aura;
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new VenomousVines()));
        harness.addMana(player1, ManaColor.GREEN, 4);
    }

    private void castAt(Permanent target) {
        prepareSpell();
        harness.castSorcery(player1, 0, target.getId());
    }

    private void castAndResolveAt(Permanent target) {
        prepareSpell();
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }
}
