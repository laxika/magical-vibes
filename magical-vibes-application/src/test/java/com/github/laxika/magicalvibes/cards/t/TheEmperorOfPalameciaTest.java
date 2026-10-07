package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FireMagic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hurricane;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({TheEmperorOfPalamecia.class, GrizzlyBears.class, Hurricane.class, Mountain.class, Shock.class, FireMagic.class})
class TheEmperorOfPalameciaTest extends BaseCardTest {

    @Test
    void choosesRestrictedManaColorAndCastsNoncreatureSpell() {
        Permanent emperor = addReadyEmperor();

        harness.activateAbility(player1, 0, null, null);

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).containsExactlyInAnyOrder("BLUE", "RED");
        harness.handleListChoice(player1, "RED");

        harness.setHand(player1, List.of(new Shock()));
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(emperor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(emperor.isTapped()).isTrue();
    }

    @Test
    void restrictedManaCannotCastCreatureSpell() {
        addReadyEmperor();
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void fourManaNoncreatureSpellsAddCountersAndTheThirdTransforms() {
        Permanent emperor = addReadyEmperor();
        harness.setHand(player1, List.of(new Hurricane(), new Hurricane(), new Hurricane()));
        harness.addMana(player1, ManaColor.GREEN, 12);

        for (int i = 0; i < 3; i++) {
            harness.castAndResolveSorcery(player1, 0, 3);
            resolveAllTriggers();
            assertThat(emperor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(i + 1);
            assertThat(emperor.isTransformed()).isEqualTo(i == 2);
        }

        assertThat(emperor.isTransformed()).isTrue();
        assertThat(emperor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void starfallCountsOnlyNoncreatureNonlandCardsInItsControllersGraveyard() {
        TheEmperorOfPalamecia card = new TheEmperorOfPalamecia();
        Permanent lord = addCreatureReady(player1, card);
        lord.setCard(card.getBackFaceCard());
        lord.setTransformed(true);
        harness.setGraveyard(player1, List.of(new Shock(), new GrizzlyBears(), new Mountain()));
        harness.setLife(player2, 20);

        declareAttackers(player1, List.of(0));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    void threeManaNoncreatureSpellDoesNotAddCounterOrTransform() {
        Permanent emperor = addReadyEmperor();
        emperor.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setHand(player1, List.of(new Hurricane()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, 2);
        resolveAllTriggers();

        assertThat(emperor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(emperor.isTransformed()).isFalse();
    }

    @Test
    void qualifyingOpponentsSpellDoesNotAddCounter() {
        Permanent emperor = addReadyEmperor();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Hurricane()));
        harness.addMana(player2, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player2, 0, 3);
        resolveAllTriggers();

        assertThat(emperor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(emperor.isTransformed()).isFalse();
    }

    @Test
    void additionalManaCostsCountTowardsFourManaThreshold() {
        Permanent emperor = addReadyEmperor();
        harness.setHand(player1, List.of(new FireMagic()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castModalInstant(player1, 0, 2, List.of());
        harness.passBothPriorities();

        assertThat(emperor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(emperor.isTransformed()).isFalse();
        resolveAllTriggers();
    }

    @Test
    void pendingCastTriggersDoNotTransformAlreadyTransformedSourceBack() {
        Permanent emperor = addReadyEmperor();
        emperor.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new FireMagic(), new FireMagic()));
        harness.addMana(player1, ManaColor.RED, 12);

        harness.castModalInstant(player1, 0, 2, List.of());
        harness.castModalInstant(player1, 0, 2, List.of());
        resolveAllTriggers();

        assertThat(emperor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(emperor.isTransformed()).isTrue();
    }

    @Test
    void starfallUsesGraveyardAtResolutionAndIgnoresOpponentsGraveyard() {
        TheEmperorOfPalamecia card = new TheEmperorOfPalamecia();
        Permanent lord = addCreatureReady(player1, card);
        lord.setCard(card.getBackFaceCard());
        lord.setTransformed(true);
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setGraveyard(player2, List.of(new Shock(), new Hurricane()));

        declareAttackers(player1, List.of(0));
        harness.setGraveyard(player1, List.of(new Shock(), new Hurricane(), new GrizzlyBears(), new Mountain()));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    void blueRestrictedManaPaysGenericCostOfNoncreatureSpell() {
        addReadyEmperor();
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");
        harness.setHand(player1, List.of(new Hurricane()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 1);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    void summoningSickEmperorCannotActivateTapAbility() {
        harness.addToBattlefield(player1, new TheEmperorOfPalamecia());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
    private Permanent addReadyEmperor() {
        Permanent emperor = addCreatureReady(player1, new TheEmperorOfPalamecia());
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return emperor;
    }
}
