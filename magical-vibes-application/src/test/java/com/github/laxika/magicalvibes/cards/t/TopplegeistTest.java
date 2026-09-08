package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TopplegeistTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield and taps a target creature an opponent controls")
    void tapsTargetCreatureOnEnter() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Topplegeist()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

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

    private void setDelirium() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Forest(), new Naturalize(), new Pacifism()));
    }

}
