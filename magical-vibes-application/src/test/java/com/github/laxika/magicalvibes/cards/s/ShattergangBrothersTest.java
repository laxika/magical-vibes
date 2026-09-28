package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShattergangBrothers.class, GrizzlyBears.class, FountainOfYouth.class, Pacifism.class, Spellbook.class})
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
        harness.addToBattlefield(player1, new Pacifism());
        harness.addToBattlefield(player2, new Pacifism());

        prepareAbility(ManaColor.GREEN);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(source);
        harness.assertInGraveyard(player1, "Pacifism");
        harness.assertNotOnBattlefield(player2, "Pacifism");
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
