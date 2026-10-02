package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.m.Mossdog;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AngelicFavor.class, Mossdog.class, Plains.class})
class AngelicFavorTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 4/4 white Angel with flying during combat")
    void createsAngelToken() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player1, List.of(new AngelicFavor()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0);

        Permanent angel = findPermanent(player1, "Angel");
        assertThat(angel.getCard().getPower()).isEqualTo(4);
        assertThat(angel.getCard().getToughness()).isEqualTo(4);
        assertThat(angel.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The Angel token is exiled at the beginning of the next end step")
    void angelTokenIsExiledAtNextEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player1, List.of(new AngelicFavor()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        assertThat(findPermanents(player1, "Angel")).hasSize(1);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Angel");
    }

    @Test
    @DisplayName("May cast for the alternate cost by tapping an untapped creature with a Plains")
    void castsForAlternateCost() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.addToBattlefield(player1, new Plains());
        Permanent creature = addCreatureReady(player1, new Mossdog());
        harness.setHand(player1, List.of(new AngelicFavor()));

        harness.castWithAlternateCost(player1, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(findPermanents(player1, "Angel")).hasSize(1);
    }

    @Test
    @DisplayName("May pay the normal mana cost even when the alternate cost is available")
    void mayPayNormalManaCost() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.addToBattlefield(player1, new Plains());
        Permanent creature = addCreatureReady(player1, new Mossdog());
        harness.setHand(player1, List.of(new AngelicFavor()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0);

        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(findPermanents(player1, "Angel")).hasSize(1);
    }

    @Test
    @DisplayName("Alternate cost requires a Plains")
    void alternateCostRequiresPlains() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        Permanent creature = addCreatureReady(player1, new Mossdog());
        harness.setHand(player1, List.of(new AngelicFavor()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("condition is not met");
    }

    @Test
    @DisplayName("Alternate cost requires an untapped creature")
    void alternateCostRequiresUntappedCreature() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.addToBattlefield(player1, new Plains());
        Permanent creature = addCreatureReady(player1, new Mossdog());
        creature.tap();
        harness.setHand(player1, List.of(new AngelicFavor()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot cast outside combat, including with the alternate cost")
    void cannotCastOutsideCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new Plains());
        Permanent creature = addCreatureReady(player1, new Mossdog());
        harness.setHand(player1, List.of(new AngelicFavor()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Exile waits for the delayed end-step trigger to resolve")
    void exileUsesTheStack() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.setHand(player1, List.of(new AngelicFavor()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0);
        harness.passUntil(TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Angel");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Angel");
    }

    @Test
    @DisplayName("A summoning-sick creature can pay the alternate cost during an opponent's combat")
    void summoningSickCreatureCanPayAlternateCost() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.addToBattlefield(player1, new Plains());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Mossdog());
        creature.setSummoningSick(true);
        harness.setHand(player1, List.of(new AngelicFavor()));

        harness.castWithAlternateCost(player1, 0, List.of(creature.getId()));
        assertThat(creature.isTapped()).isTrue();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Angel");
        harness.assertNotOnBattlefield(player2, "Angel");
    }

    @Test
    @DisplayName("An opponent's Plains does not enable the alternate cost")
    void opponentsPlainsDoesNotEnableAlternateCost() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.addToBattlefield(player2, new Plains());
        Permanent creature = addCreatureReady(player1, new Mossdog());
        harness.setHand(player1, List.of(new AngelicFavor()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot tap an opponent's creature to pay the alternate cost")
    void cannotTapOpponentsCreature() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.addToBattlefield(player1, new Plains());
        addCreatureReady(player1, new Mossdog());
        Permanent opponentCreature = addCreatureReady(player2, new Mossdog());
        harness.setHand(player1, List.of(new AngelicFavor()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of(opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(opponentCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot cast for mana after combat")
    void cannotCastForManaAfterCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player1, List.of(new AngelicFavor()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
