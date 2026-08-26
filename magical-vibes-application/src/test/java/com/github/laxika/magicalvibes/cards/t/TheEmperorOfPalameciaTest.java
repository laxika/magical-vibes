package com.github.laxika.magicalvibes.cards.t;

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

@CardUsed({TheEmperorOfPalamecia.class, GrizzlyBears.class, Hurricane.class, Mountain.class, Shock.class})
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

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void fourManaNoncreatureSpellsAddCountersAndTheThirdTransforms() {
        Permanent emperor = addReadyEmperor();
        harness.setHand(player1, List.of(new Hurricane(), new Hurricane(), new Hurricane()));
        harness.addMana(player1, ManaColor.GREEN, 12);

        for (int i = 0; i < 3; i++) {
            harness.castSorcery(player1, 0, 3);
            harness.passBothPriorities();
        }

        assertThat(emperor.isTransformed()).isTrue();
        assertThat(emperor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void starfallCountsOnlyNoncreatureNonlandCardsInItsControllersGraveyard() {
        TheEmperorOfPalamecia card = new TheEmperorOfPalamecia();
        Permanent lord = new Permanent(card);
        lord.setCard(card.getBackFaceCard());
        lord.setTransformed(true);
        lord.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(lord);
        harness.setGraveyard(player1, List.of(new Shock(), new GrizzlyBears(), new Mountain()));
        harness.setLife(player2, 20);

        declareAttackers(player1, List.of(0));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    private Permanent addReadyEmperor() {
        Permanent emperor = harness.addToBattlefieldAndReturn(player1, new TheEmperorOfPalamecia());
        emperor.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return emperor;
    }
}
