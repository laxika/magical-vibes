package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.b.BirdAdmirer;
import com.github.laxika.magicalvibes.cards.w.WingShredder;
import com.github.laxika.magicalvibes.cards.c.Consider;
import com.github.laxika.magicalvibes.cards.d.DelverOfSecrets;
import com.github.laxika.magicalvibes.cards.i.InsectileAberration;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LudevicNecrogenius.class, OlagLudevicsHubris.class, GrizzlyBears.class, HillGiant.class,
        DelverOfSecrets.class, InsectileAberration.class, Consider.class, BirdAdmirer.class, WingShredder.class})
class LudevicNecrogeniusTest extends BaseCardTest {

    @Test
    void entersAndMillsController() {
        GrizzlyBears milled = new GrizzlyBears();
        harness.setLibrary(player1, List.of(milled));
        harness.castFromHand(player1, new LudevicNecrogenius(), "{U}{B}");
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

    @Test
    @CardUsed({DelverOfSecrets.class, InsectileAberration.class})
    void attacksAndMillsOnlyItsControllerOnce() {
        addCreatureReady(player1, new LudevicNecrogenius());
        DelverOfSecrets top = new DelverOfSecrets();
        DelverOfSecrets next = new DelverOfSecrets();
        DelverOfSecrets opposingTop = new DelverOfSecrets();
        harness.setLibrary(player1, List.of(top, next));
        harness.setLibrary(player2, List.of(opposingTop));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(top);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opposingTop);
    }

    @Test
    @CardUsed({DelverOfSecrets.class, InsectileAberration.class})
    void cannotActivateOutsideMainPhase() {
        Permanent ludevic = addCreatureReady(player1, new LudevicNecrogenius());
        DelverOfSecrets creature = new DelverOfSecrets();
        harness.setGraveyard(player1, List.of(creature));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
        addTransformMana(1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(ludevic.isTransformed()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(5);
    }

    @Test
    @CardUsed(Consider.class)
    void noncreatureCardsCannotPayExileCost() {
        addCreatureReady(player1, new LudevicNecrogenius());
        Consider instant = new Consider();
        harness.setGraveyard(player1, List.of(instant));
        forceMainPhase();
        addTransformMana(1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(instant);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(5);
    }

    @Test
    @CardUsed({DelverOfSecrets.class, InsectileAberration.class, Consider.class})
    void copiedDelverTransformsWithoutLosingItsCopyCharacteristics() {
        Permanent ludevic = addCreatureReady(player1, new LudevicNecrogenius());
        DelverOfSecrets creature = new DelverOfSecrets();
        harness.setGraveyard(player1, List.of(creature));
        forceMainPhase();
        addTransformMana(1);
        harness.activateAbility(player1, 0, 1, null);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(ludevic.isTransformed()).isFalse();
        harness.passBothPriorities();
        assertThat(ludevic.isTransformed()).isTrue();

        Consider instant = new Consider();
        harness.setLibrary(player1, List.of(instant));
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(ludevic.isTransformed()).isFalse();
        assertThat(ludevic.getCard().getName()).isEqualTo("Olag, Ludevic's Hubris");
        assertThat(gqs.getEffectivePower(gd, ludevic)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ludevic)).isEqualTo(5);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(instant);
    }

    @Test
    @CardUsed({BirdAdmirer.class, WingShredder.class})
    void copiedDayboundDoesNotTransformBackFaceOrAddMoreCountersAtNight() {
        gd.dayNight = DayNight.DAY;
        Permanent ludevic = addCreatureReady(player1, new LudevicNecrogenius());
        BirdAdmirer creature = new BirdAdmirer();
        harness.setGraveyard(player1, List.of(creature));
        forceMainPhase();
        addTransformMana(1);
        harness.activateAbility(player1, 0, 1, null);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();
        int transformationSequence = ludevic.getTransformationSequence();

        gd.spellsCastLastTurn.clear();
        gd.previousTurnActivePlayerId = player2.getId();
        harness.performUntapStep(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(ludevic.getTransformationSequence()).isEqualTo(transformationSequence);
        assertThat(ludevic.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, ludevic)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ludevic)).isEqualTo(5);
    }

    private void addTransformMana(int x) {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, x);
    }

    private void forceMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
