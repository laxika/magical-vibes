package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.a.AcquisitionsExpert;
import com.github.laxika.magicalvibes.cards.a.AsylumVisitor;
import com.github.laxika.magicalvibes.cards.b.BladeJuggler;
import com.github.laxika.magicalvibes.cards.b.BloodthirstyAerialist;
import com.github.laxika.magicalvibes.cards.b.BonecladNecromancer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HoardRobber;
import com.github.laxika.magicalvibes.cards.m.MalakirBloodPriest;
import com.github.laxika.magicalvibes.cards.m.MalakirCullblade;
import com.github.laxika.magicalvibes.cards.m.MorbidOpportunist;
import com.github.laxika.magicalvibes.cards.s.SlaughterSpecialist;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TavernSwindler;
import com.github.laxika.magicalvibes.cards.t.ThievesGuildEnforcer;
import com.github.laxika.magicalvibes.cards.t.TithebearerGiant;
import com.github.laxika.magicalvibes.cards.v.VengefulWarchief;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.cards.y.YuanTiFangBlade;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({XandersWake.class, GrizzlyBears.class, Shock.class, WrathOfGod.class,
        ThievesGuildEnforcer.class, SlaughterSpecialist.class, AcquisitionsExpert.class,
        MalakirBloodPriest.class, BonecladNecromancer.class, TavernSwindler.class,
        BladeJuggler.class, HoardRobber.class, MorbidOpportunist.class,
        BloodthirstyAerialist.class, AsylumVisitor.class, YuanTiFangBlade.class,
        TithebearerGiant.class, MalakirCullblade.class, VengefulWarchief.class})
class XandersWakeTest extends BaseCardTest {

    private static final Set<String> SPELLBOOK = Set.of(
            "Thieves' Guild Enforcer", "Slaughter Specialist", "Acquisitions Expert",
            "Malakir Blood-Priest", "Boneclad Necromancer", "Tavern Swindler", "Blade Juggler",
            "Hoard Robber", "Morbid Opportunist", "Bloodthirsty Aerialist", "Asylum Visitor",
            "Yuan-Ti Fang-Blade", "Tithebearer Giant", "Malakir Cullblade", "Vengeful Warchief");

    @Test
    void draftsThreeSpellbookCardsWhenOneOrMoreControlledCreaturesDie() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new XandersWake());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).hasSize(3);
        assertThat(choice.cards()).extracting(Card::getName).allMatch(SPELLBOOK::contains);

        Card drafted = choice.cards().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(drafted.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(drafted);
    }

    @Test
    void triggersOnlyOnceForSimultaneousDeaths() {
        harness.addToBattlefield(player1, new XandersWake());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castSorcery(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void doesNotTriggerAgainLaterInTheSameTurn() {
        harness.addToBattlefield(player1, new XandersWake());
        Permanent firstBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player2, 0, firstBears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        assertThat(choice).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(choice.cards().getFirst().getId()));

        harness.castInstant(player2, 0, secondBears.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class))
                .isNull();
    }

    @Test
    void triggersAgainOnALaterTurn() {
        harness.addToBattlefield(player1, new XandersWake());
        Permanent firstBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, firstBears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        PendingInteraction.SpellbookDraftChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(firstChoice.cards().getFirst().getId()));

        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();

        Permanent secondBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, secondBears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class))
                .isNotNull();
    }
}
