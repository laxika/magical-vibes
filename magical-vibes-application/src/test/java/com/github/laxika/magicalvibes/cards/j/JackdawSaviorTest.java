package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.m.Mockingbird;
import com.github.laxika.magicalvibes.cards.s.SalvationSwan;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JackdawSavior.class, GrizzlyBears.class, HillGiant.class, Murder.class, WindDrake.class,
        Mockingbird.class, SalvationSwan.class})
class JackdawSaviorTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a creature with lesser mana value when another flying creature dies")
    void returnsCreatureWhenFlyingCreatureDies() {
        Card eligible = new GrizzlyBears();
        Card tooExpensive = new HillGiant();
        harness.setGraveyard(player1, List.of(eligible, tooExpensive));
        harness.addToBattlefield(player1, new JackdawSavior());
        Permanent windDrake = harness.addToBattlefieldAndReturn(player1, new WindDrake());

        destroy(windDrake);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());

        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Wind Drake");
    }

    @Test
    @DisplayName("Does not trigger when a creature without flying dies")
    void doesNotTriggerForNonFlyingCreature() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new JackdawSavior());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        destroy(bears);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Uses Jackdaw Savior's lesser-mana-value limit when it dies")
    void returnsCreatureWhenJackdawDies() {
        Card eligible = new GrizzlyBears();
        Card tooExpensive = new HillGiant();
        harness.setGraveyard(player1, List.of(eligible, tooExpensive));
        Permanent jackdaw = harness.addToBattlefieldAndReturn(player1, new JackdawSavior());

        destroy(jackdaw);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());
    }

    @Test
    @DisplayName("A larger flying creature allows returning a creature with Savior's mana value")
    void usesDyingCreatureManaValueRatherThanSaviorManaValue() {
        Card eligible = new JackdawSavior();
        Card equalManaValue = new HillGiant();
        Card nonCreature = new Murder();
        harness.setGraveyard(player1, List.of(eligible, equalManaValue, nonCreature));
        harness.addToBattlefield(player1, new JackdawSavior());
        Permanent swan = harness.addToBattlefieldAndReturn(player1, new SalvationSwan());

        destroy(swan);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());
        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getOriginalCard().getId().equals(eligible.getId()));
        harness.assertNotInGraveyard(player1, "Jackdaw Savior");
        harness.assertInGraveyard(player1, "Salvation Swan");
    }

    @Test
    @DisplayName("An opposing flying creature's death does not trigger Savior")
    void doesNotTriggerForOpponentFlyingCreature() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new JackdawSavior());
        Permanent drake = harness.addToBattlefieldAndReturn(player2, new WindDrake());

        destroy(drake);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Wind Drake");
    }

    @Test
    @DisplayName("Savior's death with no lesser-mana-value creature creates no target choice")
    void noLegalTargetForOwnDeath() {
        harness.setGraveyard(player1, List.of(new WindDrake(), new Murder()));
        Permanent savior = harness.addToBattlefieldAndReturn(player1, new JackdawSavior());

        destroy(savior);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Jackdaw Savior");
    }

    @Test
    @DisplayName("A creature target leaving the graveyard before resolution is not returned")
    void doesNotReturnTargetThatLeftGraveyard() {
        Card eligible = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(eligible));
        harness.addToBattlefield(player1, new JackdawSavior());
        Permanent drake = harness.addToBattlefieldAndReturn(player1, new WindDrake());

        destroy(drake);
        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A Mockingbird copying Savior cannot target itself with its own death trigger")
    void copiedSaviorCannotReturnItself() {
        Permanent savior = harness.addToBattlefieldAndReturn(player2, new JackdawSavior());
        Mockingbird mockingbird = new Mockingbird();
        Card eligible = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(eligible));
        harness.setHand(player1, List.of(mockingbird));
        harness.addMana(player1, ManaColor.BLUE, 3);

        gs.playCard(gd, player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, savior.getId());
        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(mockingbird.getId()))
                .findFirst().orElseThrow();

        destroy(copy);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());
        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Mockingbird");
    }

    private void destroy(Permanent permanent) {
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, permanent.getId());
    }
}
