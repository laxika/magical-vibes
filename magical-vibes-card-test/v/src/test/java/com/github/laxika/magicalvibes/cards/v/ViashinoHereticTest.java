package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.ChaliceOfLife;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.cards.w.WeldingJar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ViashinoHeretic.class, FountainOfYouth.class, GrizzlyBears.class, LeoninScimitar.class,
        RodOfRuin.class, Incinerate.class, WeldingJar.class, ChaliceOfLife.class})
class ViashinoHereticTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target artifact and deals damage equal to its mana value to its controller")
    void destroysArtifactAndDealsManaValueDamage() {
        addCreatureReady(player1, new ViashinoHeretic());
        harness.addToBattlefield(player2, new RodOfRuin());
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Rod of Ruin");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Rod of Ruin");
        harness.assertInGraveyard(player2, "Rod of Ruin");
        harness.assertLife(player2, 16);
    }

    @Test
    void activationTapsSource() {
        var heretic = addCreatureReady(player1, new ViashinoHeretic());
        var target = harness.addToBattlefieldAndReturn(player2, new RodOfRuin());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(heretic.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A zero-mana artifact causes no damage")
    void zeroManaValueArtifactDealsNoDamage() {
        addCreatureReady(player1, new ViashinoHeretic());
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        addCreatureReady(player1, new ViashinoHeretic());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 2);

        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can destroy an artifact controlled by its own player")
    void canTargetOwnArtifact() {
        addCreatureReady(player1, new ViashinoHeretic());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player1, "Leonin Scimitar");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Leonin Scimitar");
        harness.assertLife(player1, 19);
    }

    @Test
    void dealsDamageEvenWhenArtifactRegenerates() {
        addCreatureReady(player1, new ViashinoHeretic());
        var target = harness.addToBattlefieldAndReturn(player2, new RodOfRuin());
        harness.addToBattlefield(player2, new WeldingJar());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.activateAbility(player2, 1, null, target.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Rod of Ruin");
        harness.assertNotInGraveyard(player2, "Rod of Ruin");
        harness.assertInGraveyard(player2, "Welding Jar");
        assertThat(target.isTapped()).isTrue();
        harness.assertLife(player2, 16);
    }

    @Test
    void doesNotDealDamageWhenTargetHasAlreadyLeftBattlefield() {
        addCreatureReady(player1, new ViashinoHeretic());
        addCreatureReady(player1, new ViashinoHeretic());
        var target = harness.addToBattlefieldAndReturn(player2, new RodOfRuin());
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.activateAbility(player1, 1, null, target.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Rod of Ruin");
        harness.assertNotOnBattlefield(player2, "Rod of Ruin");
        harness.assertLife(player2, 16);
    }

    @Test
    void abilityStillDestroysAndDealsDamageAfterSourceDies() {
        var heretic = addCreatureReady(player1, new ViashinoHeretic());
        var target = harness.addToBattlefieldAndReturn(player2, new RodOfRuin());
        harness.setHand(player2, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.castAndResolveInstant(player2, 0, heretic.getId());
        harness.assertInGraveyard(player1, "Viashino Heretic");
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Rod of Ruin");
        harness.assertNotOnBattlefield(player2, "Rod of Ruin");
        harness.assertLife(player2, 16);
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new ViashinoHeretic());
        var target = harness.addToBattlefieldAndReturn(player2, new RodOfRuin());
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    void cannotActivateWithoutRedMana() {
        addCreatureReady(player1, new ViashinoHeretic());
        var target = harness.addToBattlefieldAndReturn(player2, new RodOfRuin());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    void cannotActivateAgainWhileTapped() {
        addCreatureReady(player1, new ViashinoHeretic());
        var target = harness.addToBattlefieldAndReturn(player2, new RodOfRuin());
        harness.addMana(player1, ManaColor.RED, 4);
        harness.activateAbility(player1, 0, null, target.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Rod of Ruin");
        harness.assertLife(player2, 16);
    }

    @Test
    void transformedArtifactDealsDamageUsingFrontFaceManaValue() {
        addCreatureReady(player1, new ViashinoHeretic());
        var chalice = harness.addToBattlefieldAndReturn(player2, new ChaliceOfLife());
        harness.setLife(player2, 29);

        harness.activateAbility(player2, 0, null, null);
        resolveAllTriggers();
        harness.assertOnBattlefield(player2, "Chalice of Death");
        harness.assertLife(player2, 30);

        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 0, null, chalice.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Chalice of Death");
        harness.assertInGraveyard(player2, "Chalice of Life");
        harness.assertLife(player2, 27);
    }
}
