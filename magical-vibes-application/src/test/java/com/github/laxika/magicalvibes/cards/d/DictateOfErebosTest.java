package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.e.ExtinguishAllHope;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DictateOfErebos.class, GrizzlyBears.class, GiantSpider.class, DiabolicEdict.class})
class DictateOfErebosTest extends BaseCardTest {

    @Test
    @DisplayName("Flash allows Dictate of Erebos to be cast during an opponent's combat")
    void flashAllowsCastingDuringOpponentsCombat() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new DictateOfErebos(), "{3}{B}{B}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
    }

    @Test
    @DisplayName("A creature you control dying triggers each opponent to sacrifice a creature")
    void triggersWhenControllerCreatureDies() {
        harness.addToBattlefield(player1, new DictateOfErebos());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());

        castDiabolicEdictAt(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    @DisplayName("An opponent with multiple creatures chooses which creature to sacrifice")
    void opponentChoosesCreatureToSacrifice() {
        harness.addToBattlefield(player1, new DictateOfErebos());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());

        UUID spiderId = harness.getPermanentId(player2, "Giant Spider");
        castDiabolicEdictAt(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.interaction.permanentChoiceContext()).isInstanceOf(PermanentChoiceContext.SacrificeCreature.class);

        harness.handleMultiplePermanentsChosen(player2, List.of( spiderId));

        harness.assertInGraveyard(player2, "Giant Spider");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A creature an opponent controls dying does not trigger Dictate of Erebos")
    void doesNotTriggerForOpponentCreature() {
        harness.addToBattlefield(player1, new DictateOfErebos());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new DiabolicEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Each Dictate triggers separately for the same creature death")
    void multipleCopiesTriggerSeparately() {
        harness.addToBattlefield(player1, new DictateOfErebos());
        harness.addToBattlefield(player1, new DictateOfErebos());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        castDiabolicEdictAt(player1);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player2, List.of( bearsId));
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Giant Spider");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Giant Spider");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Opposing Dictates cause successive sacrifice triggers until no creatures remain")
    void opposingDictatesCauseChainReaction() {
        harness.addToBattlefield(player1, new DictateOfErebos());
        harness.addToBattlefield(player2, new DictateOfErebos());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GiantSpider());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        castDiabolicEdictAt(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Giant Spider");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Dictate of Erebos");
        harness.assertOnBattlefield(player2, "Dictate of Erebos");
    }

    @Test
    @CardUsed({Naturalize.class})
    @DisplayName("An already triggered ability resolves after Dictate is destroyed")
    void triggerResolvesAfterSourceLeavesBattlefield() {
        harness.addToBattlefield(player1, new DictateOfErebos());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());
        UUID dictateId = harness.getPermanentId(player1, "Dictate of Erebos");
        castDiabolicEdictAt(player1);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, dictateId);
        harness.assertInGraveyard(player1, "Dictate of Erebos");
        harness.assertOnBattlefield(player2, "Giant Spider");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Giant Spider");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({Opalescence.class, Naturalize.class})
    @DisplayName("Dictate animated by Opalescence triggers for its own death")
    void animatedDictateTriggersForItsOwnDeath() {
        harness.addToBattlefield(player1, new DictateOfErebos());
        harness.addToBattlefield(player1, new Opalescence());
        harness.addToBattlefield(player2, new GiantSpider());
        UUID dictateId = harness.getPermanentId(player1, "Dictate of Erebos");
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player2, 0, dictateId);

        harness.assertInGraveyard(player1, "Dictate of Erebos");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    @CardUsed({ExtinguishAllHope.class})
    @DisplayName("Simultaneous deaths trigger once per creature even when the opponent has no creatures")
    void simultaneousDeathsEachTrigger() {
        harness.addToBattlefield(player1, new DictateOfErebos());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GiantSpider());

        harness.castFromHand(player1, new ExtinguishAllHope(), "{4}{B}{B}");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Giant Spider");
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({Opalescence.class, Deicide.class})
    @DisplayName("Exiling an animated Dictate is not a death and does not trigger")
    void exilingAnimatedDictateDoesNotTrigger() {
        harness.addToBattlefield(player1, new DictateOfErebos());
        harness.addToBattlefield(player1, new Opalescence());
        harness.addToBattlefield(player2, new GiantSpider());
        UUID dictateId = harness.getPermanentId(player1, "Dictate of Erebos");
        harness.setHand(player2, List.of(new Deicide()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player2, 0, dictateId);

        harness.assertNotOnBattlefield(player1, "Dictate of Erebos");
        harness.assertNotInGraveyard(player1, "Dictate of Erebos");
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Giant Spider");
    }

    private void castDiabolicEdictAt(com.github.laxika.magicalvibes.model.Player player) {
        harness.setHand(player, List.of(new DiabolicEdict()));
        harness.addMana(player, ManaColor.BLACK, 2);
        harness.castInstant(player, 0, player1.getId());
    }
}
