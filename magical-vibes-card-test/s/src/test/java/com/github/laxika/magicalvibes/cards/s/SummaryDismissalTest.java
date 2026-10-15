package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.AutumnsVeil;
import com.github.laxika.magicalvibes.cards.a.AwakenedSkyclave;
import com.github.laxika.magicalvibes.cards.c.Commandeer;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.cards.n.NezahalPrimalTide;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.v.VolcanicFallout;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SummaryDismissal.class, LightningBolt.class, VolcanicFallout.class,
        GrizzlyBears.class, IcyManipulator.class, NezahalPrimalTide.class,
        AutumnsVeil.class, InvasionOfZendikar.class, AwakenedSkyclave.class, Commandeer.class})
class SummaryDismissalTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving exiles other spells on the stack")
    void exilesOtherSpells() {
        LightningBolt bolt = new LightningBolt();
        harness.setHand(player1, List.of(bolt));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);

        harness.castFromHand(player2, new SummaryDismissal(), "{2}{U}{U}");
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Lightning Bolt"));
        harness.assertInGraveyard(player2, "Summary Dismissal");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Exiles uncounterable spells (they still leave the stack)")
    void exilesUncounterableSpells() {
        VolcanicFallout fallout = new VolcanicFallout();
        harness.castFromHand(player1, fallout, "{1}{R}{R}");
        harness.passPriority(player1);

        harness.castFromHand(player2, new SummaryDismissal(), "{2}{U}{U}");
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Volcanic Fallout"));
        // Fallout never resolved — no damage
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Counters activated abilities on the stack")
    void countersActivatedAbilities() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        Permanent icy = harness.addToBattlefieldAndReturn(player2, new IcyManipulator());
        icy.setSummoningSick(false);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(icy), null, bearsId);

        assertThat(gd.stack).anyMatch(se -> se.getEntryType() == StackEntryType.ACTIVATED_ABILITY);

        harness.castFromHand(player1, new SummaryDismissal(), "{2}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).noneMatch(se -> se.getEntryType() == StackEntryType.ACTIVATED_ABILITY);
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("With empty stack besides itself, resolves with no targets and goes to graveyard")
    void resolvesAlone() {
        harness.castFromHand(player1, new SummaryDismissal(), "{2}{U}{U}");
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Summary Dismissal");
    }

    @Test
    @DisplayName("Counters triggered abilities from an uncounterable source")
    void countersTriggerFromUncounterableSource() {
        harness.addToBattlefield(player1, new NezahalPrimalTide());
        GrizzlyBears drawCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawCard));
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, player1.getId());
        assertThat(gd.stack).anyMatch(se -> se.getEntryType() == StackEntryType.TRIGGERED_ABILITY);

        harness.castFromHand(player1, new SummaryDismissal(), "{2}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawCard);
        harness.assertOnBattlefield(player1, "Nezahal, Primal Tide");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Spell-only protection does not protect activated abilities")
    void countersAbilitiesThroughAutumnsVeil() {
        harness.castFromHand(player2, new AutumnsVeil(), "{G}");
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent icy = harness.addToBattlefieldAndReturn(player2, new IcyManipulator());
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.ensurePriority(player2);
        harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(icy), null,
                harness.getPermanentId(player1, "Grizzly Bears"));
        assertThat(gd.stack).anyMatch(se -> se.getEntryType() == StackEntryType.ACTIVATED_ABILITY);

        harness.castFromHand(player1, new SummaryDismissal(), "{2}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Grizzly Bears").isTapped()).isFalse();
        assertThat(icy.isTapped()).isTrue();
        harness.assertOnBattlefield(player2, "Icy Manipulator");
    }

    @Test
    @DisplayName("Exiles battle spells")
    void exilesBattleSpells() {
        InvasionOfZendikar invasion = new InvasionOfZendikar();
        harness.castFromHand(player1, invasion, "{3}{G}");
        assertThat(gd.stack).anyMatch(se -> se.getEntryType() == StackEntryType.BATTLE_SPELL);

        harness.castFromHand(player2, new SummaryDismissal(), "{2}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.exiledCards).anyMatch(e -> e.card() == invasion
                && e.ownerId().equals(player1.getId()));
        harness.assertNotOnBattlefield(player1, "Invasion of Zendikar");
        harness.assertInGraveyard(player2, "Summary Dismissal");
    }

    @Test
    @DisplayName("Exiling a controlled spell preserves its owner")
    void preservesOwnerOfControlledSpell() {
        IcyManipulator icy = new IcyManipulator();
        harness.castFromHand(player1, icy, "{4}");
        harness.setHand(player2, List.of(new Commandeer()));
        harness.addMana(player2, ManaColor.BLUE, 7);
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, icy.getId());
        harness.passBothPriorities();
        assertThat(gd.stack).anyMatch(se -> se.getCard() == icy
                && se.getControllerId().equals(player2.getId()));

        harness.castFromHand(player1, new SummaryDismissal(), "{2}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.exiledCards).anyMatch(e -> e.card() == icy
                && e.ownerId().equals(player1.getId()));
        harness.assertNotOnBattlefield(player2, "Icy Manipulator");
    }
}
