package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JadeMage.class})
class JadeMageTest extends BaseCardTest {

    @Test
    @DisplayName("{2}{G} creates a 1/1 green Saproling token")
    void createsSaprolingToken() {
        harness.addToBattlefield(player1, new JadeMage());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Saproling")
                        && p.getCard().getPower() == 1
                        && p.getCard().getToughness() == 1
                        && p.getCard().getColor() == CardColor.GREEN
                        && p.getCard().getSubtypes().contains(CardSubtype.SAPROLING));
    }

    @Test
    @DisplayName("Ability can be activated repeatedly (no tap cost)")
    void createsMultipleTokens() {
        harness.addToBattlefield(player1, new JadeMage());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Saproling")).hasSize(2);
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new JadeMage());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Tapped, summoning-sick Jade Mage can activate its ability")
    void activatesWhileTappedAndSummoningSick() {
        var mage = harness.addToBattlefieldAndReturn(player1, new JadeMage());
        mage.setSummoningSick(true);
        mage.tap();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Saproling")).hasSize(1);
        assertThat(findPermanents(player2, "Saproling")).isEmpty();
    }

    @Test
    @DisplayName("Jade Mage can create a token during an opponent's upkeep")
    void activatesDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new JadeMage());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Saproling")).hasSize(1);
        assertThat(findPermanents(player2, "Saproling")).isEmpty();
    }

    @Test
    @DisplayName("Each activation uses the stack and pays its own mana cost")
    void stacksMultipleActivationsAndPaysForEach() {
        harness.addToBattlefield(player1, new JadeMage());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null);
        harness.activateAbility(player1, 0, 0, null);

        assertThat(findPermanents(player1, "Saproling")).isEmpty();
        assertThat(gd.stack).hasSize(2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null))
                .isInstanceOf(IllegalStateException.class);

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Saproling")).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }
}
