package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AtarkaBeastbreaker;
import com.github.laxika.magicalvibes.cards.c.ColossodonYearling;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Silkwrap.class, ColossodonYearling.class, AtarkaBeastbreaker.class, SummitProwler.class, Naturalize.class})
class SilkwrapTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles an opponent creature with mana value 3 or less")
    void etbExilesSmallOpponentCreature() {
        harness.addToBattlefield(player2, new ColossodonYearling());
        UUID targetId = harness.getPermanentId(player2, "Colossodon Yearling");

        castAndResolve(targetId);

        harness.assertNotOnBattlefield(player2, "Colossodon Yearling");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Colossodon Yearling"));
    }

    @Test
    @DisplayName("Exiled creature returns when Silkwrap leaves the battlefield")
    void exiledCreatureReturnsWhenSourceLeaves() {
        harness.addToBattlefield(player2, new AtarkaBeastbreaker());
        UUID targetId = harness.getPermanentId(player2, "Atarka Beastbreaker");
        castAndResolve(targetId);

        UUID sourceId = harness.getPermanentId(player1, "Silkwrap");
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player2, 0, sourceId);

        harness.assertOnBattlefield(player2, "Atarka Beastbreaker");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target an opponent creature with mana value greater than 3")
    void cannotTargetLargeOpponentCreature() {
        harness.addToBattlefield(player2, new SummitProwler());
        UUID targetId = harness.getPermanentId(player2, "Summit Prowler");
        prepareToCast();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature controlled by Silkwrap's controller")
    void cannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new AtarkaBeastbreaker());
        UUID targetId = harness.getPermanentId(player1, "Atarka Beastbreaker");
        prepareToCast();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Removing Silkwrap before its ETB resolves does not exile the creature")
    void sourceLeavesBeforeTriggerResolves() {
        harness.addToBattlefield(player2, new ColossodonYearling());
        UUID targetId = harness.getPermanentId(player2, "Colossodon Yearling");
        prepareToCast();
        harness.castEnchantment(player1, 0, targetId);
        harness.passBothPriorities();

        UUID sourceId = harness.getPermanentId(player1, "Silkwrap");
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player2, 0, sourceId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Silkwrap");
        harness.assertOnBattlefield(player2, "Colossodon Yearling");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Silkwrap can enter when there are no eligible creatures")
    void canCastWithoutEligibleCreature() {
        prepareToCast();
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Silkwrap");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Exiled creature returns to its owner rather than its previous controller")
    void stolenCreatureReturnsToOwner() {
        harness.addToBattlefield(player2, new AtarkaBeastbreaker());
        UUID targetId = harness.getPermanentId(player2, "Atarka Beastbreaker");
        gd.stolenCreatures.put(targetId, player1.getId());
        castAndResolve(targetId);

        harness.assertNotOnBattlefield(player2, "Atarka Beastbreaker");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Atarka Beastbreaker"));
        UUID sourceId = harness.getPermanentId(player1, "Silkwrap");
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player2, 0, sourceId);

        harness.assertOnBattlefield(player1, "Atarka Beastbreaker");
        harness.assertNotOnBattlefield(player2, "Atarka Beastbreaker");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private void prepareToCast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Silkwrap()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void castAndResolve(UUID targetId) {
        prepareToCast();
        harness.castEnchantment(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
