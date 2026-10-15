package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.o.OonasGatewarden;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WingrattleScarecrow.class, FugitiveWizard.class, WalkingCorpse.class, DoomBlade.class,
        OonasGatewarden.class})
class WingrattleScarecrowTest extends BaseCardTest {

    private void resolveUntilInputOrEmpty() {
        for (int i = 0; i < 12; i++) {
            if (gd.interaction.isAwaitingInput() || gd.stack.isEmpty()) {
                return;
            }
            harness.passBothPriorities();
        }
    }

    private Permanent scarecrow() {
        return findPermanent(player1, "Wingrattle Scarecrow");
    }

    @Test
    @DisplayName("No flying and no persist without blue or black creatures")
    void noKeywordsWithoutColoredCreatures() {
        harness.addToBattlefield(player1, new WingrattleScarecrow());

        Permanent scarecrow = scarecrow();
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.PERSIST)).isFalse();
    }

    @Test
    @DisplayName("Has flying while controller controls a blue creature")
    void flyingWithBlueCreature() {
        harness.addToBattlefield(player1, new WingrattleScarecrow());
        harness.addToBattlefield(player1, new FugitiveWizard());

        Permanent scarecrow = scarecrow();
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.PERSIST)).isFalse();
    }

    @Test
    @DisplayName("Has persist while controller controls a black creature")
    void persistWithBlackCreature() {
        harness.addToBattlefield(player1, new WingrattleScarecrow());
        harness.addToBattlefield(player1, new WalkingCorpse());

        Permanent scarecrow = scarecrow();
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.PERSIST)).isTrue();
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Opponent's blue creature does not grant flying")
    void opponentBlueCreatureDoesNotCount() {
        harness.addToBattlefield(player1, new WingrattleScarecrow());
        harness.addToBattlefield(player2, new FugitiveWizard());

        assertThat(gqs.hasKeyword(gd, scarecrow(), Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Persist returns the Scarecrow with a -1/-1 counter when it dies controlling a black creature")
    void persistReturnsWhenControllingBlackCreature() {
        harness.addToBattlefield(player1, new WingrattleScarecrow());
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Wingrattle Scarecrow"));
        resolveUntilInputOrEmpty();

        Permanent scarecrow = scarecrow();
        assertThat(scarecrow.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A blue and black hybrid creature grants both abilities, which disappear when it leaves")
    void hybridCreatureGrantsBothAbilitiesUntilItLeaves() {
        Permanent scarecrow = harness.addToBattlefieldAndReturn(player1, new WingrattleScarecrow());
        Permanent gatewarden = harness.addToBattlefieldAndReturn(player1, new OonasGatewarden());

        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.PERSIST)).isTrue();

        gatewarden.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Oona's Gatewarden");
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.PERSIST)).isFalse();
    }

    @Test
    @DisplayName("An opponent's blue and black creature grants neither ability")
    void opponentHybridCreatureGrantsNeitherAbility() {
        Permanent scarecrow = harness.addToBattlefieldAndReturn(player1, new WingrattleScarecrow());
        harness.addToBattlefield(player2, new OonasGatewarden());

        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.PERSIST)).isFalse();
    }

    @Test
    @DisplayName("Dying without a black creature does not trigger persist")
    void deathWithoutBlackCreatureDoesNotReturn() {
        Permanent scarecrow = harness.addToBattlefieldAndReturn(player1, new WingrattleScarecrow());
        scarecrow.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveUntilInputOrEmpty();

        harness.assertNotOnBattlefield(player1, "Wingrattle Scarecrow");
        harness.assertInGraveyard(player1, "Wingrattle Scarecrow");
    }

    @Test
    @DisplayName("A -1/-1 counter at death prevents persist even while a black creature is controlled")
    void minusCounterAtDeathPreventsReturn() {
        Permanent scarecrow = harness.addToBattlefieldAndReturn(player1, new WingrattleScarecrow());
        harness.addToBattlefield(player1, new OonasGatewarden());
        scarecrow.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        scarecrow.setMarkedDamage(1);
        harness.runStateBasedActions();
        resolveUntilInputOrEmpty();

        harness.assertNotOnBattlefield(player1, "Wingrattle Scarecrow");
        harness.assertInGraveyard(player1, "Wingrattle Scarecrow");
    }

    @Test
    @DisplayName("Persist still resolves if the last black creature dies after the trigger")
    void persistResolvesAfterBlackCreatureLeaves() {
        Permanent scarecrow = harness.addToBattlefieldAndReturn(player1, new WingrattleScarecrow());
        Permanent gatewarden = harness.addToBattlefieldAndReturn(player1, new OonasGatewarden());
        scarecrow.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Wingrattle Scarecrow");

        gatewarden.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.runStateBasedActions();
        resolveUntilInputOrEmpty();

        harness.assertInGraveyard(player1, "Oona's Gatewarden");
        harness.assertNotInGraveyard(player1, "Wingrattle Scarecrow");
        Permanent returned = scarecrow();
        assertThat(returned.getId()).isNotEqualTo(scarecrow.getId());
        assertThat(returned.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, returned, Keyword.PERSIST)).isFalse();
    }

    @Test
    @DisplayName("Persist triggers when the last black creature dies simultaneously, regardless of battlefield order")
    void persistUsesAbilitiesBeforeSimultaneousDeaths() {
        Permanent gatewarden = harness.addToBattlefieldAndReturn(player1, new OonasGatewarden());
        Permanent scarecrow = harness.addToBattlefieldAndReturn(player1, new WingrattleScarecrow());
        gatewarden.setMarkedDamage(1);
        scarecrow.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveUntilInputOrEmpty();

        harness.assertInGraveyard(player1, "Oona's Gatewarden");
        harness.assertNotInGraveyard(player1, "Wingrattle Scarecrow");
        assertThat(scarecrow().getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }
}
