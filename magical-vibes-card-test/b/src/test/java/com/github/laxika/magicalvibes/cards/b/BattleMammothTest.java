package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CaptainOfTheMists;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SeedsOfStrength;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BattleMammoth.class, CaptainOfTheMists.class, GrizzlyBears.class, Forest.class, Shock.class,
        SeedsOfStrength.class})
class BattleMammothTest extends BaseCardTest {

    @Test
    @DisplayName("Triggers when an opponent targets a permanent you control")
    void triggersOnOpponentTargetingPermanent() {
        harness.addToBattlefield(player1, new BattleMammoth());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, bearsId);

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    @DisplayName("Does not trigger when an opponent targets a player")
    void doesNotTriggerOnPlayerTarget() {
        harness.addToBattlefield(player1, new BattleMammoth());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Triggers for an opponent activated ability targeting a noncreature permanent")
    void triggersOnOpponentAbilityTargetingPermanent() {
        harness.addToBattlefield(player1, new BattleMammoth());
        harness.addToBattlefield(player1, new Forest());
        UUID forestId = harness.getPermanentId(player1, "Forest");

        Permanent captain = harness.addToBattlefieldAndReturn(player2, new CaptainOfTheMists());
        captain.setSummoningSick(false);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateAbility(player2, 0, null, forestId);

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    @DisplayName("Accepting the trigger draws a card")
    void acceptingTriggerDrawsCard() {
        harness.addToBattlefield(player1, new BattleMammoth());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, bearsId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Can be foretold and cast from exile later")
    void foretellsAndCastsLater() {
        BattleMammoth mammoth = new BattleMammoth();
        harness.setHand(player1, List.of(mammoth));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.foretell(player1, 0);

        ExiledCardEntry entry = gd.findExiledCard(mammoth.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isTrue();

        gd.turnNumber++;
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, mammoth.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void targetingMammothItselfAllowsDecliningTheDraw() {
        Permanent mammoth = harness.addToBattlefieldAndReturn(player1, new BattleMammoth());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, mammoth.getId());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void doesNotTriggerForItsControllersSpell() {
        Permanent mammoth = harness.addToBattlefieldAndReturn(player1, new BattleMammoth());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, mammoth.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void doesNotTriggerForAnOpponentsPermanent() {
        harness.addToBattlefield(player1, new BattleMammoth());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void triggersOnlyOnceWhenOneSpellTargetsTheSamePermanentThreeTimes() {
        Permanent mammoth = harness.addToBattlefieldAndReturn(player1, new BattleMammoth());
        harness.setHand(player2, List.of(new SeedsOfStrength()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castInstant(player2, 0, List.of(mammoth.getId(), mammoth.getId(), mammoth.getId()));

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    void triggersOnceForEachDistinctPermanentTargetedByOneSpell() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BattleMammoth());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new SeedsOfStrength()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castInstant(player2, 0, List.of(first.getId(), second.getId(), second.getId()));

        assertThat(gd.stack).hasSize(3);
    }

    @Test
    void cannotCastOnTheTurnItWasForetold() {
        BattleMammoth mammoth = new BattleMammoth();
        harness.setHand(player1, List.of(mammoth));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.foretell(player1, 0);

        assertThatThrownBy(() -> harness.castFromExile(player1, mammoth.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(mammoth.getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void eachMammothTriggersForTheSameTarget() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BattleMammoth());
        harness.addToBattlefield(player1, new BattleMammoth());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, first.getId());

        assertThat(gd.stack).hasSize(3);
    }
}
