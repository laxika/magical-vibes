package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.p.PrizefighterConstruct;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AlleyEvasion.class, PrizefighterConstruct.class})
class AlleyEvasionTest extends BaseCardTest {

    @Test
    void boostsTargetCreatureYouControl() {
        Permanent construct = harness.addToBattlefieldAndReturn(player1, new PrizefighterConstruct());
        harness.setHand(player1, List.of(new AlleyEvasion()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, 0, construct.getId());
        harness.passBothPriorities();

        assertThat(construct.getPowerModifier()).isEqualTo(1);
        assertThat(construct.getToughnessModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(construct.getPowerModifier()).isZero();
        assertThat(construct.getToughnessModifier()).isZero();
    }

    @Test
    void returnsTargetCreatureYouControlToItsOwnersHand() {
        Permanent construct = harness.addToBattlefieldAndReturn(player1, new PrizefighterConstruct());
        harness.setHand(player1, List.of(new AlleyEvasion()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, 1, construct.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Prizefighter Construct");
        harness.assertInHand(player1, "Prizefighter Construct");
    }

    @Test
    void boostModeCannotTargetCreatureOpponentControls() {
        harness.addToBattlefield(player2, new PrizefighterConstruct());
        harness.setHand(player1, List.of(new AlleyEvasion()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0,
                harness.getPermanentId(player2, "Prizefighter Construct")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnModeCannotTargetCreatureOpponentControls() {
        harness.addToBattlefield(player2, new PrizefighterConstruct());
        harness.setHand(player1, List.of(new AlleyEvasion()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1,
                harness.getPermanentId(player2, "Prizefighter Construct")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnsBorrowedCreatureToOwnerRatherThanController() {
        PrizefighterConstruct card = new PrizefighterConstruct();
        card.setOwnerId(player2.getId());
        Permanent construct = harness.addToBattlefieldAndReturn(player1, card);
        harness.setHand(player1, List.of(new AlleyEvasion()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, 1, construct.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Prizefighter Construct");
        harness.assertInHand(player2, "Prizefighter Construct");
        harness.assertNotInHand(player1, "Prizefighter Construct");
    }

    @Test
    void boostDoesNotResolveAfterTargetChangesController() {
        Permanent construct = harness.addToBattlefieldAndReturn(player1, new PrizefighterConstruct());
        harness.setHand(player1, List.of(new AlleyEvasion()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, 0, construct.getId());

        gd.playerBattlefields.get(player1.getId()).remove(construct);
        gd.playerBattlefields.get(player2.getId()).add(construct);
        harness.passBothPriorities();

        assertThat(construct.getPowerModifier()).isZero();
        assertThat(construct.getToughnessModifier()).isZero();
        harness.assertOnBattlefield(player2, "Prizefighter Construct");
        harness.assertInGraveyard(player1, "Alley Evasion");
    }

    @Test
    void returnDoesNotResolveAfterTargetChangesController() {
        Permanent construct = harness.addToBattlefieldAndReturn(player1, new PrizefighterConstruct());
        harness.setHand(player1, List.of(new AlleyEvasion()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, 1, construct.getId());

        gd.playerBattlefields.get(player1.getId()).remove(construct);
        gd.playerBattlefields.get(player2.getId()).add(construct);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Prizefighter Construct");
        harness.assertNotInHand(player1, "Prizefighter Construct");
        harness.assertNotInHand(player2, "Prizefighter Construct");
        harness.assertInGraveyard(player1, "Alley Evasion");
    }
}
