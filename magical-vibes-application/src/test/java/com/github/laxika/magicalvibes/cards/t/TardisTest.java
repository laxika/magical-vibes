package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Orzhova;
import com.github.laxika.magicalvibes.cards.p.Panopticon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Tardis.class, TheFourthDoctor.class, GrizzlyBears.class, DoomBlade.class,
        DarkRitual.class, Panopticon.class, Orzhova.class, TheFirstDoctor.class})
class TardisTest extends BaseCardTest {

    @Test
    void crewingAnimatesTardisAndTapsTheCrew() {
        Permanent tardis = addTardis();
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());
        prepareMainPhase();

        harness.activateAbility(player1, indexOf(player1, tardis), null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, tardis)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    void attackWithATimeLordGrantsCascadeAndMayPlaneswalk() {
        preparePlanechase();
        Permanent tardis = addTardis();
        harness.addToBattlefieldAndReturn(player1, new TheFourthDoctor());
        addCreatureReady(player1, new GrizzlyBears());
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> crew(tardis));

        declareAttackers(List.of(indexOf(player1, tardis)));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.planechase.faceUp).extracting(object -> object.getCard().getName())
                .containsExactly("Orzhova");

        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLibrary(player1, List.of(new DarkRitual()));
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(card -> card.getName())
                .containsExactly("Dark Ritual");
    }

    @Test
    void attackWithoutATimeLordDoesNotGrantTheTrigger() {
        Permanent tardis = addTardis();
        addCreatureReady(player1, new GrizzlyBears());
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> crew(tardis));

        declareAttackers(List.of(indexOf(player1, tardis)));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void grantedCascadeTriggersTheFirstDoctor() {
        Permanent tardis = addTardis();
        harness.addToBattlefield(player1, new TheFirstDoctor());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> crew(tardis));

        declareAttackers(List.of(indexOf(player1, tardis)));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.setLibrary(player1, List.of());

        harness.castFromHand(player1, new DarkRitual(), "{B}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(com.github.laxika.magicalvibes.model.CounterType.PLUS_ONE_PLUS_ONE))
                .isOne();
    }

    @Test
    void decliningPlaneswalkStillGrantsCascade() {
        preparePlanechase();
        Permanent tardis = addTardis();
        harness.addToBattlefield(player1, new TheFourthDoctor());
        addCreatureReady(player1, new GrizzlyBears());
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> crew(tardis));

        declareAttackers(List.of(indexOf(player1, tardis)));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.planechase.faceUp).extracting(object -> object.getCard().getName())
                .containsExactly("Panopticon");
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new DarkRitual()));
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(card -> card.getName()).containsExactly("Dark Ritual");
    }

    @Test
    void losingTimeLordBeforeResolutionPreventsBothBenefits() {
        preparePlanechase();
        Permanent tardis = addTardis();
        Permanent doctor = harness.addToBattlefieldAndReturn(player1, new TheFourthDoctor());
        addCreatureReady(player1, new GrizzlyBears());
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> crew(tardis));

        declareAttackers(List.of(indexOf(player1, tardis)));
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, doctor.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "The Fourth Doctor");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.planechase.faceUp).extracting(object -> object.getCard().getName())
                .containsExactly("Panopticon");
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new DarkRitual()));
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Dark Ritual");
    }

    private Permanent addTardis() {
        Permanent tardis = harness.addToBattlefieldAndReturn(player1, new Tardis());
        tardis.setSummoningSick(false);
        return tardis;
    }

    private void crew(Permanent tardis) {
        prepareMainPhase();
        harness.activateAbility(player1, indexOf(player1, tardis), null, null);
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.PermanentChoice) {
            harness.handlePermanentChosen(player1, findPermanent(player1, "Grizzly Bears").getId());
        }
        harness.passBothPriorities();
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void preparePlanechase() {
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.deck.add(new Orzhova());
        gd.planechase.faceUp.add(new PlanarObject(new Panopticon(), gd.nextTimestamp()));
    }

    private int indexOf(com.github.laxika.magicalvibes.model.Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
