package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShattergangBrothers.class, GrizzlyBears.class, Pacifism.class, Spellbook.class})
class ShattergangBrothersTest extends BaseCardTest {

    @Test
    void blackAbilitySacrificesEachOpponentsCreature() {
        Permanent source = addReadySource();
        Permanent sacrificedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        prepareAbility(ManaColor.BLACK);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, sacrificedCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(source);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void redAbilitySacrificesEachOpponentsArtifact() {
        Permanent source = addReadySource();
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player2, new Spellbook());

        prepareAbility(ManaColor.RED);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(source);
        harness.assertInGraveyard(player1, "Spellbook");
        harness.assertNotOnBattlefield(player2, "Spellbook");
    }

    @Test
    void greenAbilitySacrificesEachOpponentsEnchantment() {
        Permanent source = addReadySource();
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent ownAura = harness.addToBattlefieldAndReturn(player1, new Pacifism());
        ownAura.setAttachedTo(source.getId());
        Permanent opposingAura = harness.addToBattlefieldAndReturn(player2, new Pacifism());
        opposingAura.setAttachedTo(opposingCreature.getId());

        prepareAbility(ManaColor.GREEN);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(source);
        harness.assertInGraveyard(player1, "Pacifism");
        harness.assertNotOnBattlefield(player2, "Pacifism");
    }

    @Test
    void canSacrificeSummoningSickSourceAndAbilityStillResolves() {
        harness.addToBattlefield(player1, new ShattergangBrothers());
        harness.addToBattlefield(player2, new GrizzlyBears());

        prepareAbility(ManaColor.BLACK);
        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertInGraveyard(player1, "Shattergang Brothers");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void opponentChoosesExactlyOneCreatureAtResolution() {
        addReadySource();
        Permanent cost = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent chosen = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent survivor = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Spellbook());

        prepareAbility(ManaColor.BLACK);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, cost.getId());
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(chosen, survivor);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player2, List.of(chosen.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(survivor).doesNotContain(chosen);
        harness.assertOnBattlefield(player2, "Spellbook");
        harness.assertOnBattlefield(player1, "Shattergang Brothers");
    }

    @Test
    void opponentChoosesExactlyOneArtifactWithoutSacrificingCreatures() {
        addReadySource();
        harness.addToBattlefield(player1, new Spellbook());
        Permanent chosen = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        Permanent survivor = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        harness.addToBattlefield(player2, new GrizzlyBears());

        prepareAbility(ManaColor.RED);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.assertInGraveyard(player1, "Spellbook");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(chosen, survivor);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player2, List.of(chosen.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(survivor).doesNotContain(chosen);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void enchantmentCostIsPaidEvenWhenOpponentHasNoEnchantment() {
        Permanent source = addReadySource();
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Pacifism());
        aura.setAttachedTo(source.getId());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Spellbook());

        prepareAbility(ManaColor.GREEN);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.assertInGraveyard(player1, "Pacifism");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Shattergang Brothers");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Spellbook");
    }

    @Test
    void opponentChoosesExactlyOneEnchantmentWithoutSacrificingItsCreature() {
        Permanent source = addReadySource();
        Permanent cost = harness.addToBattlefieldAndReturn(player1, new Pacifism());
        cost.setAttachedTo(source.getId());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent chosen = harness.addToBattlefieldAndReturn(player2, new Pacifism());
        chosen.setAttachedTo(creature.getId());
        Permanent survivor = harness.addToBattlefieldAndReturn(player2, new Pacifism());
        survivor.setAttachedTo(creature.getId());

        prepareAbility(ManaColor.GREEN);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.assertInGraveyard(player1, "Pacifism");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(chosen, survivor);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player2, List.of(chosen.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(creature, survivor).doesNotContain(chosen);
        harness.assertInGraveyard(player2, "Pacifism");
        harness.assertOnBattlefield(player1, "Shattergang Brothers");
    }

    private Permanent addReadySource() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ShattergangBrothers());
        source.setSummoningSick(false);
        return source;
    }

    private void prepareAbility(ManaColor coloredMana) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, coloredMana, 1);
    }
}
