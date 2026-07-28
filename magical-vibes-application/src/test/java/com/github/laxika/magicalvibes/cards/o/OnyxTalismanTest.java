package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Facevaulter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OnyxTalismanTest extends BaseCardTest {

    private void setUpOpponentTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private UUID addTappedBears(Player owner) {
        harness.addToBattlefield(owner, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(owner, "Grizzly Bears");
        findPermanent(owner, "Grizzly Bears").tap();
        return bearsId;
    }

    private void castBlackSpell(Player caster) {
        harness.setHand(caster, List.of(new Facevaulter()));
        harness.addMana(caster, ManaColor.BLACK, 1);
        harness.addMana(caster, ManaColor.COLORLESS, 1);
        harness.castCreature(caster, 0);
    }

    @Test
    @DisplayName("Opponent's black spell: paying {3} untaps the chosen permanent")
    void payUntapsTarget() {
        harness.addToBattlefield(player1, new OnyxTalisman());
        UUID bearsId = addTappedBears(player1);
        setUpOpponentTurn();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        castBlackSpell(player2);

        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining to pay {3} leaves the permanent tapped")
    void declineLeavesTapped() {
        harness.addToBattlefield(player1, new OnyxTalisman());
        UUID bearsId = addTappedBears(player1);
        setUpOpponentTurn();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        castBlackSpell(player2);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanent(player1, "Grizzly Bears").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Controller's own black spell triggers too — any player casting counts")
    void ownBlackSpellTriggers() {
        harness.addToBattlefield(player1, new OnyxTalisman());
        addTappedBears(player1);

        castBlackSpell(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
    }

    @Test
    @DisplayName("A nonblack spell does not trigger")
    void nonBlackDoesNotTrigger() {
        harness.addToBattlefield(player1, new OnyxTalisman());
        addTappedBears(player1);
        setUpOpponentTurn();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Onyx Talisman"));
    }

    @Test
    @DisplayName("Any permanent may be targeted, including one an opponent controls")
    void untapsOpponentPermanent() {
        harness.addToBattlefield(player1, new OnyxTalisman());
        UUID bearsId = addTappedBears(player2);
        setUpOpponentTurn();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        castBlackSpell(player2);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Grizzly Bears").isTapped()).isFalse();
    }
}
