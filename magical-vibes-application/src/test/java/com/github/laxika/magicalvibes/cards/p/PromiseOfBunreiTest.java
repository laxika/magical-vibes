package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.h.HandOfHonor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PromiseOfBunrei.class, HandOfHonor.class})
class PromiseOfBunreiTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and creates four colorless Spirit tokens when your creature dies")
    void creatureDeathCreatesFourSpirits() {
        harness.addToBattlefield(player1, new PromiseOfBunrei());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HandOfHonor());
        putIntoGraveyard(creature);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Promise of Bunrei");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Spirit"))
                .hasSize(4)
                .allSatisfy(permanent -> {
                    assertThat(permanent.getCard().getPower()).isEqualTo(1);
                    assertThat(permanent.getCard().getToughness()).isEqualTo(1);
                    assertThat(permanent.getCard().getColor()).isNull();
                    assertThat(permanent.getCard().getSubtypes()).containsExactly(CardSubtype.SPIRIT);
                });
    }

    @Test
    @DisplayName("Does not trigger when an opponent's creature dies")
    void opponentCreatureDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new PromiseOfBunrei());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HandOfHonor());
        putIntoGraveyard(creature);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Promise of Bunrei");
        harness.assertInGraveyard(player2, "Hand of Honor");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Spirit"))
                .isEmpty();
    }

    @Test
    @DisplayName("Does not create tokens if the enchantment leaves before its trigger resolves")
    void sourceLeavingBeforeTriggerResolutionPreventsTokens() {
        Permanent promise = harness.addToBattlefieldAndReturn(player1, new PromiseOfBunrei());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HandOfHonor());

        putIntoGraveyard(creature);
        putIntoGraveyard(promise);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Promise of Bunrei");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Spirit"))
                .isEmpty();
    }

    @Test
    @DisplayName("Creates only one batch when multiple creatures die before the trigger resolves")
    void multipleDeathsCreateOnlyOneBatch() {
        harness.addToBattlefield(player1, new PromiseOfBunrei());
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new HandOfHonor());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new HandOfHonor());

        putIntoGraveyard(firstCreature);
        putIntoGraveyard(secondCreature);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Spirit"))
                .hasSize(4);
    }

    private void putIntoGraveyard(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, permanent));
    }
}
