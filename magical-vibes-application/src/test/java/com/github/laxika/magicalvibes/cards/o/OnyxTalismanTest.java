package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OnyxTalisman.class, DarkRitual.class, BalduvianBears.class, Disenchant.class})
class OnyxTalismanTest extends BaseCardTest {

    private void setUpOpponentTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private Permanent addTappedBears(Player owner) {
        Permanent bears = addCreatureReady(owner, new BalduvianBears());
        bears.tap();
        return bears;
    }

    private void castBlackSpell(Player caster) {
        harness.castFromHand(caster, new DarkRitual(), "{B}");
    }

    @Test
    @DisplayName("Opponent's black spell: paying {3} untaps the chosen permanent")
    void payUntapsTarget() {
        harness.addToBattlefield(player1, new OnyxTalisman());
        Permanent bears = addTappedBears(player1);
        setUpOpponentTurn();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        castBlackSpell(player2);

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.validIds()).contains(bears.getId());

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining to pay {3} leaves the permanent tapped")
    void declineLeavesTapped() {
        harness.addToBattlefield(player1, new OnyxTalisman());
        Permanent bears = addTappedBears(player1);
        setUpOpponentTurn();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        castBlackSpell(player2);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Controller's own black spell triggers too — any player casting counts")
    void ownBlackSpellTriggers() {
        harness.addToBattlefield(player1, new OnyxTalisman());
        Permanent bears = addTappedBears(player1);

        castBlackSpell(player1);

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.validIds()).contains(bears.getId());
    }

    @Test
    @DisplayName("A nonblack spell does not trigger")
    void nonBlackDoesNotTrigger() {
        harness.addToBattlefield(player1, new OnyxTalisman());
        addTappedBears(player1);
        setUpOpponentTurn();
        harness.castFromHand(player2, new BalduvianBears(), "{1}{G}");

        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Onyx Talisman"));
    }

    @Test
    @DisplayName("Any permanent may be targeted, including one an opponent controls")
    void untapsOpponentPermanent() {
        harness.addToBattlefield(player1, new OnyxTalisman());
        Permanent bears = addTappedBears(player2);
        setUpOpponentTurn();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        castBlackSpell(player2);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Accepting without three mana leaves the target tapped")
    void cannotPayLeavesTargetTapped() {
        harness.addToBattlefield(player1, new OnyxTalisman());
        Permanent bears = addTappedBears(player1);
        setUpOpponentTurn();

        castBlackSpell(player2);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The Talisman can untap itself by paying three colored mana")
    void untapsItselfWithColoredMana() {
        Permanent talisman = harness.addToBattlefieldAndReturn(player1, new OnyxTalisman());
        talisman.tap();
        setUpOpponentTurn();
        harness.addMana(player1, ManaColor.GREEN, 4);

        castBlackSpell(player2);
        harness.handlePermanentChosen(player1, talisman.getId());
        harness.passBothPriorities();

        assertThat(talisman.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(4);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(talisman.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Removing the Talisman does not stop its already triggered ability")
    void resolvesAfterSourceDestroyed() {
        Permanent talisman = harness.addToBattlefieldAndReturn(player1, new OnyxTalisman());
        Permanent bears = addTappedBears(player1);
        setUpOpponentTurn();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        castBlackSpell(player2);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, talisman.getId());
        harness.assertInGraveyard(player1, "Onyx Talisman");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An ability with a destroyed target does not offer payment")
    void destroyedTargetPreventsPayment() {
        Permanent talisman = harness.addToBattlefieldAndReturn(player1, new OnyxTalisman());
        talisman.tap();
        setUpOpponentTurn();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        castBlackSpell(player2);
        harness.handlePermanentChosen(player1, talisman.getId());
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, talisman.getId());
        harness.assertInGraveyard(player1, "Onyx Talisman");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
    }
}
