package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.FumeSpitter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.m.MarchOfTheMachines;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.d.DissonantWave;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EmeraldDragon.class, DissonantWave.class, FumeSpitter.class,
        GrizzlyBears.class, Millstone.class, HowlingMine.class, MarchOfTheMachines.class})
class EmeraldDragonTest extends BaseCardTest {

    @Test
    void adventureCountersAnAbilityFromANoncreatureSource() {
        Millstone millstone = new Millstone();
        harness.addToBattlefield(player2, millstone);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passPriority(player2);

        EmeraldDragon card = new EmeraldDragon();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, millstone.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void adventureCannotTargetAnAbilityFromACreatureSource() {
        FumeSpitter fumeSpitter = new FumeSpitter();
        harness.addToBattlefield(player2, fumeSpitter);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null,
                harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player2);

        EmeraldDragon card = new EmeraldDragon();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, fumeSpitter.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void creatureFaceCanBeCastFromExileAfterAdventure() {
        Millstone millstone = new Millstone();
        harness.addToBattlefield(player2, millstone);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passPriority(player2);

        EmeraldDragon card = new EmeraldDragon();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, millstone.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Emerald Dragon");
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    @Test
    void adventureCountersATriggeredAbilityFromANoncreatureSource() {
        HowlingMine mine = new HowlingMine();
        harness.addToBattlefield(player1, mine);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of());
        EmeraldDragon card = new EmeraldDragon();
        harness.setHand(player2, List.of(card));
        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.passUntil(player1, TurnStep.DRAW);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);

        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.ensurePriority(player2);
        harness.castAdventure(player2, 0, mine.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertOnBattlefield(player1, "Howling Mine");
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void adventureCannotTargetACreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(bears));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castCreature(player2, 0);
        harness.ensurePriority(player1);
        harness.setHand(player1, List.of(new EmeraldDragon()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void adventureCannotTargetAnAbilityFromAnAnimatedArtifact() {
        Millstone millstone = new Millstone();
        harness.addToBattlefieldAndReturn(player2, millstone).setSummoningSick(false);
        harness.addToBattlefield(player2, new MarchOfTheMachines());
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.ensurePriority(player1);
        harness.setHand(player1, List.of(new EmeraldDragon()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, millstone.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void creatureFaceCanBeCastDirectlyFromHand() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new EmeraldDragon()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Emerald Dragon");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void adventureGoesToGraveyardWhenItsTargetHasAlreadyBeenCountered() {
        Millstone millstone = new Millstone();
        harness.addToBattlefield(player2, millstone);
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, 0, null, player1.getId());
        EmeraldDragon first = new EmeraldDragon();
        EmeraldDragon second = new EmeraldDragon();
        harness.setHand(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.ensurePriority(player1);
        harness.castAdventure(player1, 0, millstone.getId());
        harness.ensurePriority(player1);
        harness.castAdventure(player1, 0, millstone.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.findExiledCard(second.getId())).isNotNull();
        assertThat(gd.findExiledCard(first.getId())).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first);
        harness.assertOnBattlefield(player2, "Millstone");
    }

    @Test
    void adventureCannotTargetANoncreatureSpell() {
        Millstone millstone = new Millstone();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(millstone));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castArtifact(player2, 0);
        harness.ensurePriority(player1);
        harness.setHand(player1, List.of(new EmeraldDragon()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, millstone.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
