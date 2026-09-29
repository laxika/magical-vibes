package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BurstOfStrength;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PutridHexhag.class, BurstOfStrength.class, Murder.class, GrizzlyBears.class})
class PutridHexhagTest extends BaseCardTest {

    @Test
    @DisplayName("A counter put on Putrid Hexhag lets it perpetually curse an opponent creature")
    void cursesOpponentCreatureAndItsControllerLosesLifeWhenItDies() {
        Permanent hexhag = addCreatureReady(player1, new PutridHexhag());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        putCounterOn(hexhag);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        destroy(player1, target);
        int lifeBeforeTrigger = gd.getLife(player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBeforeTrigger - 2);
    }

    @Test
    @DisplayName("The counter trigger cannot target a creature you control")
    void cannotTargetOwnCreature() {
        Permanent hexhag = addCreatureReady(player1, new PutridHexhag());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        putCounterOn(hexhag);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    private void putCounterOn(Permanent hexhag) {
        harness.setHand(player1, List.of(new BurstOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, hexhag.getId());
    }

    private void destroy(com.github.laxika.magicalvibes.model.Player caster, Permanent target) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Murder()));
        harness.addMana(caster, ManaColor.BLACK, 2);
        harness.addMana(caster, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(caster, 0, target.getId());
    }
}
