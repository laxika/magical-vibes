package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FeedTheSerpent;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BeskirShieldmate.class, Shock.class, GrizzlyBears.class, FeedTheSerpent.class})
class BeskirShieldmateTest extends BaseCardTest {

    @Test
    @DisplayName("When Beskir Shieldmate dies, it creates a 1/1 white Human Warrior token")
    void deathCreatesHumanWarriorToken() {
        harness.addToBattlefield(player1, new BeskirShieldmate());

        killWithShock(player2, player1, "Beskir Shieldmate");

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.WARRIOR);
        assertThat(token.getEffectivePower()).isEqualTo(1);
        assertThat(token.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Another creature dying does not trigger Beskir Shieldmate")
    void anotherCreatureDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new BeskirShieldmate());
        harness.addToBattlefield(player1, new GrizzlyBears());

        killWithShock(player2, player1, "Grizzly Bears");

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(permanent -> permanent.getCard().isToken()).isEmpty();
    }

    @Test
    @DisplayName("An opponent's Shieldmate creates exactly one token for that opponent")
    void opponentDeathCreatesOneTokenForItsController() {
        harness.addToBattlefield(player2, new BeskirShieldmate());

        killWithShock(player1, player2, "Beskir Shieldmate");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        Permanent token = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.isTapped()).isFalse();
        harness.assertInGraveyard(player2, "Beskir Shieldmate");
    }

    @Test
    @DisplayName("The Human Warrior token does not inherit the Shieldmate's death trigger")
    void tokenDeathDoesNotCreateAnotherToken() {
        harness.addToBattlefield(player1, new BeskirShieldmate());
        killWithShock(player2, player1, "Beskir Shieldmate");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Permanent token = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(token.getCard().isToken()).isTrue();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, token.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Exiling Beskir Shieldmate does not create a token")
    void exileDoesNotTriggerDeathAbility() {
        harness.addToBattlefield(player1, new BeskirShieldmate());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new FeedTheSerpent()));
        harness.addMana(player2, ManaColor.BLACK, 4);

        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Beskir Shieldmate"));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card instanceof BeskirShieldmate);
        harness.assertNotInGraveyard(player1, "Beskir Shieldmate");
    }
    private void killWithShock(Player caster, Player targetController, String targetName) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(targetController, targetName);
        harness.castAndResolveInstant(caster, 0, targetId);
    }
}
