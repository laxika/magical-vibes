package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.l.LivingEnd;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KolvoriGodOfKinship;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.SawItComing;
import com.github.laxika.magicalvibes.cards.t.TheRinghartCrest;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BohnBeguilingBalladeer.class, LivingEnd.class, Forest.class, GrizzlyBears.class,
        LightningBolt.class, SawItComing.class, KolvoriGodOfKinship.class, TheRinghartCrest.class})
class BohnBeguilingBalladeerTest extends BaseCardTest {

    @Test
    void grantsForetellToNonlandCardsWithReducedCost() {
        addCreatureReady(player1, new BohnBeguilingBalladeer());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears, new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.foretell(player1, 0);

        ExiledCardEntry entry = gd.findExiledCard(bears.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isTrue();
        assertThatThrownBy(() -> harness.foretell(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void secondSpellGoadsTargetCreatureAnOpponentControls() {
        addCreatureReady(player1, new BohnBeguilingBalladeer());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(target.getId()).doesNotContain(ownCreature.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    void foretoldCardKeepsReducedCostAfterBohnDies() {
        Permanent bohn = addCreatureReady(player1, new BohnBeguilingBalladeer());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, bohn.getId());
        harness.assertNotOnBattlefield(player1, "Bohn, Beguiling Balladeer");

        gd.turnNumber++;
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castFromExile(player1, bears.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void cannotCastForetoldCardOnTheTurnItWasForetold() {
        addCreatureReady(player1, new BohnBeguilingBalladeer());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(bears.getId())).isNotNull();
    }

    @Test
    void doesNotGrantForetellToOpponentsHand() {
        addCreatureReady(player1, new BohnBeguilingBalladeer());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.foretell(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("does not have foretell");
    }

    @Test
    void nativeForetellCostIsNotReplacedByBohn() {
        addCreatureReady(player1, new BohnBeguilingBalladeer());
        SawItComing counter = new SawItComing();
        harness.setHand(player1, List.of(counter));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);

        gd.turnNumber++;
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castFromExile(player1, counter.getId(), gd.stack.getLast().getCard().getId());
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Saw It Coming");
        harness.assertInGraveyard(player2, "Lightning Bolt");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    void canForetellNonlandCardWithNoManaCost() {
        addCreatureReady(player1, new BohnBeguilingBalladeer());
        LivingEnd livingEnd = new LivingEnd();
        harness.setHand(player1, List.of(livingEnd));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.foretell(player1, 0);

        assertThat(gd.findExiledCard(livingEnd.getId())).isNotNull();
        assertThat(gd.findExiledCard(livingEnd.getId()).faceDown()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void modalBackFaceUsesItsOwnReducedForetellCost() {
        addCreatureReady(player1, new BohnBeguilingBalladeer());
        KolvoriGodOfKinship kolvori = new KolvoriGodOfKinship();
        harness.setHand(player1, List.of(kolvori));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);

        gd.turnNumber++;
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.ensurePriority(player1);
        gs.playCardFromExile(gd, player1, kolvori.getId(), 1, null);

        assertThat(gd.findExiledCard(kolvori.getId())).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void onlySecondSpellTriggersEvenWhenFirstWasCastBeforeBohnEntered() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        addCreatureReady(player1, new BohnBeguilingBalladeer());

        harness.castAndResolveInstant(player1, 0, player2.getId());
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 11);
    }

    @Test
    void secondSpellTriggersOnOpponentsTurnButOpponentsSpellsDoNotCount() {
        addCreatureReady(player1, new BohnBeguilingBalladeer());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.castAndResolveInstant(player2, 0, player1.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();

        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        harness.assertLife(player2, 17);

        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();
        harness.assertLife(player2, 14);
        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }
}
