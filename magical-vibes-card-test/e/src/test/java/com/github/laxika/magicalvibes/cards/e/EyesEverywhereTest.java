package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.FlickerOfFate;
import com.github.laxika.magicalvibes.cards.g.GrotesqueDemise;
import com.github.laxika.magicalvibes.cards.s.SaruliCaretaker;
import com.github.laxika.magicalvibes.cards.s.SauroformHybrid;
import com.github.laxika.magicalvibes.cards.s.SimicGuildgate;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EyesEverywhere.class, SauroformHybrid.class, SaruliCaretaker.class,
        SimicGuildgate.class, GrotesqueDemise.class, FlickerOfFate.class})
class EyesEverywhereTest extends BaseCardTest {

    @Test
    @DisplayName("Scry 1 at the beginning of its controller's upkeep")
    void scriesAtBeginningOfUpkeep() {
        Card top = new SauroformHybrid();
        Card bottom = new SaruliCaretaker();
        harness.setLibrary(player1, List.of(top, bottom));
        harness.addToBattlefield(player1, new EyesEverywhere());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bottom, top);
    }

    @Test
    @DisplayName("Exchanges control of itself and the target nonland permanent")
    void exchangesControl() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new EyesEverywhere());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SauroformHybrid());
        addActivationMana();
        prepareForSorcerySpeedActivation();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId).contains(source.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getId).contains(target.getId());
    }

    @Test
    @DisplayName("Cannot activate the exchange at instant speed")
    void exchangeRequiresSorcerySpeed() {
        harness.addToBattlefield(player1, new EyesEverywhere());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SauroformHybrid());
        addActivationMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player1, new EyesEverywhere());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SimicGuildgate());
        addActivationMana();
        prepareForSorcerySpeedActivation();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland permanent");
    }

    @Test
    @DisplayName("Does not trigger on its opponent's upkeep")
    void doesNotScryOnOpponentsUpkeep() {
        harness.addToBattlefield(player1, new EyesEverywhere());
        harness.setLibrary(player2, List.of(new SauroformHybrid(), new SaruliCaretaker()));

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Scry allows keeping the card on top")
    void canKeepTopCard() {
        Card top = new SauroformHybrid();
        Card bottom = new SaruliCaretaker();
        harness.setLibrary(player1, List.of(top, bottom));
        harness.addToBattlefield(player1, new EyesEverywhere());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, bottom);
    }

    @Test
    @DisplayName("Targeting a permanent you control is legal and changes no control")
    void canTargetOwnPermanentWithoutExchanging() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new EyesEverywhere());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SauroformHybrid());
        addActivationMana();
        prepareForSorcerySpeedActivation();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(source, target);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot exchange when the target leaves before resolution")
    void doesNotExchangeWhenTargetLeaves() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new EyesEverywhere());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SauroformHybrid());
        addActivationMana();
        prepareForSorcerySpeedActivation();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.setHand(player2, List.of(new GrotesqueDemise()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(source);
        harness.assertNotOnBattlefield(player2, "Sauroform Hybrid");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
    }

    @Test
    @DisplayName("A returned Eyes Everywhere is not the source of the old exchange ability")
    void doesNotExchangeAfterSourceLeavesAndReturns() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new EyesEverywhere());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SauroformHybrid());
        addActivationMana();
        prepareForSorcerySpeedActivation();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.setHand(player2, List.of(new FlickerOfFate()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player2, 0, source.getId());
        Permanent returned = findPermanent(player1, "Eyes Everywhere");
        assertThat(returned.getId()).isNotEqualTo(source.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(returned).doesNotContain(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target).doesNotContain(returned);
    }

    @Test
    @DisplayName("After an exchange the new controller scries on their upkeep")
    void newControllerScriesAfterExchange() {
        harness.addToBattlefield(player1, new EyesEverywhere());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SauroformHybrid());
        Card top = new SaruliCaretaker();
        Card bottom = new SimicGuildgate();
        harness.setLibrary(player2, List.of(top, bottom));
        addActivationMana();
        prepareForSorcerySpeedActivation();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(bottom, top);
    }

    @Test
    @DisplayName("Scry with an empty library needs no interaction")
    void scriesWithEmptyLibrary() {
        harness.addToBattlefield(player1, new EyesEverywhere());
        harness.setLibrary(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The recipient can activate Eyes Everywhere to exchange it back")
    void newControllerCanExchangeItBack() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new EyesEverywhere());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SauroformHybrid());
        addActivationMana();
        prepareForSorcerySpeedActivation();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.COLORLESS, 5);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.activateAbility(player2, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(source).doesNotContain(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target).doesNotContain(source);
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }

    private void prepareForSorcerySpeedActivation() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
