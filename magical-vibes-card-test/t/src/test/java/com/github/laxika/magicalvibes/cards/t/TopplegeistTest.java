package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Topplegeist.class, GrizzlyBears.class, Forest.class, Naturalize.class, Pacifism.class})
class TopplegeistTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield and taps a target creature an opponent controls")
    void tapsTargetCreatureOnEnter() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Topplegeist()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0, 0, target.getId());
        resolveAllTriggers();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Delirium taps a target creature controlled by the opponent whose upkeep it is")
    void deliriumTapsCreatureOnOpponentsUpkeep() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Forest(), new Naturalize(), new Pacifism()));
        Permanent topplegeist = addCreatureReady(player1, new Topplegeist());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(topplegeist.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not trigger on an opponent's upkeep without delirium")
    void doesNotTriggerWithoutDelirium() {
        addCreatureReady(player1, new Topplegeist());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a creature controlled by the Topplegeist controller")
    void cannotTargetOwnCreatureOnOpponentsUpkeep() {
        setDelirium();
        addCreatureReady(player1, new Topplegeist());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        advanceToUpkeep(player2);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(opponentCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not trigger on its controller's upkeep")
    void doesNotTriggerOnOwnUpkeep() {
        setDelirium();
        addCreatureReady(player1, new Topplegeist());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Delirium is checked again when the upkeep ability resolves")
    void doesNotTapWhenDeliriumIsLostBeforeResolution() {
        setDelirium();
        addCreatureReady(player1, new Topplegeist());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        advanceToUpkeep(player2);
        harness.handlePermanentChosen(player1, target.getId());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest(), new Naturalize()));
        resolveAllTriggers();

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Four cards sharing fewer than four card types do not enable delirium")
    void doesNotTriggerWithFourCardsOfThreeTypes() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new Forest(), new Naturalize()));
        addCreatureReady(player1, new Topplegeist());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The opponent's graveyard does not enable delirium")
    void doesNotTriggerUsingOpponentsGraveyard() {
        harness.setGraveyard(player2, List.of(
                new GrizzlyBears(), new Forest(), new Naturalize(), new Pacifism()));
        addCreatureReady(player1, new Topplegeist());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(target.isTapped()).isFalse();
    }

    private void setDelirium() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Forest(), new Naturalize(), new Pacifism()));
    }

}
