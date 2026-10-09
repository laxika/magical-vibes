package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GruulTurf;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DuneBroodNephilim.class, GruulTurf.class})
class DuneBroodNephilimTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage creates one colorless Sand token for each land you control")
    void combatDamageCreatesSandTokensForEachLandYouControl() {
        Permanent nephilim = addCreatureReady(player1, new DuneBroodNephilim());
        nephilim.setAttacking(true);
        addLands(player1, 3);
        addLands(player2, 2);

        resolveCombat();
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Sand");
        assertThat(tokens).hasSize(3);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getEffectivePower()).isEqualTo(1);
            assertThat(token.getEffectiveToughness()).isEqualTo(1);
            assertThat(token.getCard().getColor()).isNull();
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SAND);
        });
    }

    @Test
    @DisplayName("The Sand token count uses lands controlled when the trigger resolves")
    void tokenCountUsesLandsAtResolution() {
        Permanent nephilim = addCreatureReady(player1, new DuneBroodNephilim());
        nephilim.setAttacking(true);
        addLands(player1, 1);

        harness.withAutoStop(com.github.laxika.magicalvibes.model.TurnStep.COMBAT_DAMAGE,
                this::resolveCombat);
        addLands(player1, 2);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Sand")).hasSize(3);
    }

    @Test
    @DisplayName("A blocked Dune-Brood Nephilim does not create Sand tokens")
    void blockedCombatDamageCreatesNoSandTokens() {
        Permanent nephilim = addCreatureReady(player1, new DuneBroodNephilim());
        nephilim.setAttacking(true);
        addLands(player1, 3);

        Permanent blocker = addCreatureReady(player2, new DuneBroodNephilim());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(findPermanents(player1, "Sand")).isEmpty();
    }

    @Test
    @DisplayName("Combat damage with no lands creates no Sand tokens")
    void combatDamageWithNoLandsCreatesNoTokens() {
        Permanent nephilim = addCreatureReady(player1, new DuneBroodNephilim());
        nephilim.setAttacking(true);
        addLands(player2, 3);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Sand")).isEmpty();
        assertThat(findPermanents(player2, "Sand")).isEmpty();
    }

    @Test
    @DisplayName("The second player's Nephilim creates tokens for its own lands")
    void secondPlayerCreatesTokensForTheirOwnLands() {
        Permanent nephilim = addCreatureReady(player2, new DuneBroodNephilim());
        nephilim.setAttacking(true);
        addLands(player1, 3);
        addLands(player2, 1);

        resolveCombat(player2);
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Sand")).hasSize(1);
        assertThat(findPermanents(player1, "Sand")).isEmpty();
    }

    @Test
    @DisplayName("The trigger resolves even after the Nephilim leaves the battlefield")
    void triggerResolvesAfterSourceLeavesBattlefield() {
        Permanent nephilim = addCreatureReady(player1, new DuneBroodNephilim());
        nephilim.setAttacking(true);
        addLands(player1, 2);

        resolveCombat();
        gd.playerBattlefields.get(player1.getId()).remove(nephilim);
        gd.playerGraveyards.get(player1.getId()).add(nephilim.getCard());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Sand")).hasSize(2);
    }

    private void addLands(com.github.laxika.magicalvibes.model.Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new GruulTurf());
        }
    }
}
