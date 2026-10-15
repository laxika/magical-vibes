package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.p.PerceptionBobblehead;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IntelligenceBobblehead.class, PerceptionBobblehead.class, SolRing.class})
class IntelligenceBobbleheadTest extends BaseCardTest {

    @Test
    void manaAbilityAddsChosenColor() {
        Permanent bobblehead = harness.addToBattlefieldAndReturn(player1, new IntelligenceBobblehead());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(bobblehead.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void drawsOneCardPerBobbleheadYouControl() {
        harness.addToBattlefield(player1, new IntelligenceBobblehead());
        harness.addToBattlefield(player1, new IntelligenceBobblehead());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new IntelligenceBobblehead(), new IntelligenceBobblehead()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void countsDifferentBobbleheadsButNotOtherArtifactsOrOpponentsBobbleheads() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new IntelligenceBobblehead());
        harness.addToBattlefield(player1, new PerceptionBobblehead());
        harness.addToBattlefield(player1, new SolRing());
        harness.addToBattlefield(player2, new IntelligenceBobblehead());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new IntelligenceBobblehead(),
                new IntelligenceBobblehead(), new IntelligenceBobblehead()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(source.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void countsBobbleheadsAtResolutionRatherThanActivation() {
        harness.addToBattlefield(player1, new IntelligenceBobblehead());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new IntelligenceBobblehead(), new IntelligenceBobblehead()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.addToBattlefield(player1, new PerceptionBobblehead());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void cannotActivateDrawAbilityWithoutFiveMana() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new IntelligenceBobblehead());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(source.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void manaAbilityAndDrawAbilityShareTheTapCost() {
        harness.addToBattlefield(player1, new IntelligenceBobblehead());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(6);
    }
}
