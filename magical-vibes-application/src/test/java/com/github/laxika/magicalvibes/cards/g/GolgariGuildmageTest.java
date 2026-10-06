package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.cards.g.GolgariSignet;
import com.github.laxika.magicalvibes.cards.g.GolgariThug;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GolgariGuildmage.class, BorosRecruit.class, GolgariThug.class, GolgariSignet.class})
class GolgariGuildmageTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature returns a target creature card from the graveyard to hand")
    void sacrificesCreatureAndReturnsTargetCreature() {
        addCreatureReady(player1, new GolgariGuildmage());
        Permanent sacrificed = addCreatureReady(player1, new BorosRecruit());
        Card returned = new GolgariThug();
        harness.setGraveyard(player1, List.of(returned));
        addBlackActivationMana();

        harness.activateAbility(player1, 0, 0, null, returned.getId(), Zone.GRAVEYARD);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, sacrificed.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Golgari Thug");
        harness.assertInGraveyard(player1, "Boros Recruit");
        harness.assertOnBattlefield(player1, "Golgari Guildmage");
    }

    @Test
    @DisplayName("The first ability can sacrifice Golgari Guildmage itself")
    void canSacrificeItself() {
        addCreatureReady(player1, new GolgariGuildmage());
        Card returned = new GolgariThug();
        harness.setGraveyard(player1, List.of(returned));
        addBlackActivationMana();

        harness.activateAbility(player1, 0, 0, null, returned.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Golgari Thug");
        harness.assertInGraveyard(player1, "Golgari Guildmage");
    }

    @Test
    @DisplayName("The first ability only targets creature cards in its controller's graveyard")
    void cannotTargetNonCreatureOrOpponentGraveyardCard() {
        addCreatureReady(player1, new GolgariGuildmage());
        Card ownCreature = new GolgariThug();
        Card ownNonCreature = new GolgariSignet();
        Card opponentCreature = new GolgariThug();
        harness.setGraveyard(player1, List.of(ownCreature, ownNonCreature));
        harness.setGraveyard(player2, List.of(opponentCreature));
        addBlackActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 0, null, ownNonCreature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 0, null, opponentCreature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The second ability puts a +1/+1 counter on target creature")
    void putsCounterOnTargetCreature() {
        addCreatureReady(player1, new GolgariGuildmage());
        Permanent target = addCreatureReady(player1, new BorosRecruit());
        addGreenActivationMana();

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The second ability can target an opponent's creature")
    void putsCounterOnOpponentsCreature() {
        addCreatureReady(player1, new GolgariGuildmage());
        Permanent target = addCreatureReady(player2, new BorosRecruit());
        addGreenActivationMana();

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The second ability cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new GolgariGuildmage());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GolgariSignet());
        addGreenActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A creature on the battlefield cannot become the target by being sacrificed for the cost")
    void cannotTargetCreatureThatWillBeSacrificed() {
        addCreatureReady(player1, new GolgariGuildmage());
        Permanent creature = addCreatureReady(player1, new BorosRecruit());
        addBlackActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 0, null, creature.getCard().getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Boros Recruit");
        harness.assertOnBattlefield(player1, "Golgari Guildmage");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The first ability requires a graveyard target even when the Guildmage can sacrifice itself")
    void cannotActivateWithoutGraveyardTarget() {
        addCreatureReady(player1, new GolgariGuildmage());
        addBlackActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 0, null, null, Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Golgari Guildmage");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrifice is paid before resolution and remains paid when the graveyard target disappears")
    void targetLeavingGraveyardDoesNotRefundSacrifice() {
        addCreatureReady(player1, new GolgariGuildmage());
        Permanent sacrificed = addCreatureReady(player1, new BorosRecruit());
        Card returned = new GolgariThug();
        harness.setGraveyard(player1, List.of(returned));
        addBlackActivationMana();

        harness.activateAbility(player1, 0, 0, null, returned.getId(), Zone.GRAVEYARD);
        harness.handlePermanentChosen(player1, sacrificed.getId());

        harness.assertNotOnBattlefield(player1, "Boros Recruit");
        harness.assertInGraveyard(player1, "Boros Recruit");
        harness.assertNotInHand(player1, "Golgari Thug");
        gd.playerGraveyards.get(player1.getId()).remove(returned);
        harness.setExile(player1, List.of(returned));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Golgari Thug");
        harness.assertInGraveyard(player1, "Boros Recruit");
        assertThat(gd.findExiledCard(returned.getId())).isSameAs(returned);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped summoning-sick Guildmage can activate its counter ability repeatedly")
    void tappedSummoningSickGuildmageCanActivateRepeatedly() {
        Permanent guildmage = harness.addToBattlefieldAndReturn(player1, new GolgariGuildmage());
        guildmage.setSummoningSick(true);
        guildmage.tap();
        Permanent target = addCreatureReady(player1, new BorosRecruit());
        addGreenActivationMana();
        addGreenActivationMana();

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(guildmage.isTapped()).isTrue();
    }
    private void addBlackActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }

    private void addGreenActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
