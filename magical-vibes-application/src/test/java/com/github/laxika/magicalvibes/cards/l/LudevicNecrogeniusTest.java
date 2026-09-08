package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LudevicNecrogenius.class, OlagLudevicsHubris.class, GrizzlyBears.class, HillGiant.class})
class LudevicNecrogeniusTest extends BaseCardTest {

    @Test
    void entersAndMillsController() {
        GrizzlyBears milled = new GrizzlyBears();
        harness.setLibrary(player1, List.of(milled));
        harness.setHand(player1, List.of(new LudevicNecrogenius()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(milled);
    }

    @Test
    void cannotTransformWithZeroExiledCards() {
        Permanent ludevic = addCreatureReady(player1, new LudevicNecrogenius());
        forceMainPhase();
        addTransformMana(1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(ludevic.isTransformed()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(5);
    }

    @Test
    void transformsIntoCopyAndAddsCounterForOneExiledCreature() {
        Permanent ludevic = addCreatureReady(player1, new LudevicNecrogenius());
        GrizzlyBears exiledCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(exiledCreature));
        forceMainPhase();
        addTransformMana(1);

        harness.activateAbility(player1, 0, 1, null);
        harness.handleMultipleCardsChosen(player1, List.of(exiledCreature.getId()));
        harness.passBothPriorities();

        assertThat(ludevic.isTransformed()).isTrue();
        assertThat(ludevic.getCard().getName()).isEqualTo("Olag, Ludevic's Hubris");
        assertThat(ludevic.getCard().getPower()).isEqualTo(4);
        assertThat(ludevic.getCard().getToughness()).isEqualTo(4);
        assertThat(ludevic.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ludevic.getCard().getColors()).containsExactlyInAnyOrder(
                CardColor.GREEN, CardColor.BLUE, CardColor.BLACK);
        assertThat(ludevic.getCard().getSupertypes()).contains(CardSupertype.LEGENDARY);
        assertThat(ludevic.getCard().getSubtypes()).contains(CardSubtype.BEAR, CardSubtype.ZOMBIE);
    }

    @Test
    void choosesWhichExiledCreatureToCopy() {
        Permanent ludevic = addCreatureReady(player1, new LudevicNecrogenius());
        GrizzlyBears bears = new GrizzlyBears();
        HillGiant giant = new HillGiant();
        harness.setGraveyard(player1, List.of(bears, giant));
        forceMainPhase();
        addTransformMana(2);

        harness.activateAbility(player1, 0, 2, null);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), giant.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LudevicCopyChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(giant.getId()));

        assertThat(ludevic.getCard().getName()).isEqualTo("Olag, Ludevic's Hubris");
        assertThat(ludevic.getCard().getSubtypes()).contains(CardSubtype.GIANT, CardSubtype.ZOMBIE);
        assertThat(ludevic.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private void addTransformMana(int x) {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, x);
    }

    private void forceMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
