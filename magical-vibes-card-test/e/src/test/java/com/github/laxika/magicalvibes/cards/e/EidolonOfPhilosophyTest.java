package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EidolonOfPhilosophy.class})
class EidolonOfPhilosophyTest extends BaseCardTest {

    @Test
    void activatingAbilitySacrificesSourceAsCost() {
        Permanent permanent = addEidolonToBattlefield();
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(permanent);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void resolvingAbilityDrawsThreeCards() {
        addEidolonToBattlefield();
        addAbilityMana();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 3);
    }

    @Test
    void cannotActivateWithoutEnoughMana() {
        addEidolonToBattlefield();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWithoutBlueMana() {
        addEidolonToBattlefield();
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canActivateWhileTappedAndSummoningSick() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new EidolonOfPhilosophy());
        permanent.setSummoningSick(true);
        permanent.tap();
        addAbilityMana();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        int opponentHandSizeBefore = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(permanent);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(permanent.getCard());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 3);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSizeBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithBlueButInsufficientTotalMana() {
        Permanent permanent = addEidolonToBattlefield();
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(permanent);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(permanent.getCard());
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addEidolonToBattlefield() {
        return addCreatureReady(player1, new EidolonOfPhilosophy());
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }
}
