package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PiratedCopy.class, GrizzlyBears.class})
class PiratedCopyTest extends BaseCardTest {

    @Test
    @DisplayName("Copies a creature and adds Pirate while retaining the copied creature's abilities")
    void copiesCreatureWithPirateSubtype() {
        Permanent copied = castCopyOfGrizzlyBears(player1, player2);

        assertThat(copied.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(copied.getCard().getPower()).isEqualTo(2);
        assertThat(copied.getCard().getToughness()).isEqualTo(2);
        assertThat(copied.getCard().getSubtypes()).contains(CardSubtype.BEAR, CardSubtype.PIRATE);
    }

    @Test
    @DisplayName("Draws when this creature deals combat damage to a player")
    void drawsWhenThisCreatureDealsCombatDamage() {
        Permanent copied = castCopyOfGrizzlyBears(player1, player2);
        copied.setSummoningSick(false);
        copied.setAttacking(true);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Draws when another creature with the same name deals combat damage")
    void drawsWhenAnotherSameNameCreatureDealsCombatDamage() {
        castCopyOfGrizzlyBears(player1, player2);
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());
        opponentBears.setAttacking(true);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Declining to copy leaves the original 0/0 to die")
    void diesWhenCopyIsDeclined() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new PiratedCopy(), "{4}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Pirated Copy");
        harness.assertInGraveyard(player1, "Pirated Copy");
    }

    private Permanent castCopyOfGrizzlyBears(com.github.laxika.magicalvibes.model.Player controller,
                                             com.github.laxika.magicalvibes.model.Player targetController) {
        harness.addToBattlefield(targetController, new GrizzlyBears());
        harness.castFromHand(controller, new PiratedCopy(), "{4}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(controller, true);
        harness.handlePermanentChosen(controller, harness.getPermanentId(targetController, "Grizzly Bears"));

        return gd.playerBattlefields.get(controller.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Pirated Copy"))
                .findFirst()
                .orElseThrow();
    }
}
