package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.l.LurkingLizards;
import com.github.laxika.magicalvibes.cards.x.X23DeadlyWeapon;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ColossusSteelStalwart.class, X23DeadlyWeapon.class, LurkingLizards.class})
class ColossusSteelStalwartTest extends BaseCardTest {

    @Test
    @DisplayName("Has indestructible during its controller's turn only")
    void hasIndestructibleDuringItsControllersTurnOnly() {
        Permanent colossus = addCreatureReady(player1, new ColossusSteelStalwart());

        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, colossus, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceActivePlayer(player2);
        assertThat(gqs.hasKeyword(gd, colossus, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Gives other Mutants you control +1/+1")
    void buffsOtherMutantsYouControl() {
        Permanent colossus = addCreatureReady(player1, new ColossusSteelStalwart());
        Permanent otherMutant = addCreatureReady(player1, new X23DeadlyWeapon());
        Permanent opponentMutant = addCreatureReady(player2, new ColossusSteelStalwart());
        Permanent nonMutant = addCreatureReady(player1, new LurkingLizards());

        assertThat(gqs.getEffectivePower(gd, colossus)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, colossus)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, otherMutant)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, otherMutant)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opponentMutant)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opponentMutant)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, nonMutant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, nonMutant)).isEqualTo(3);
    }

    @Test
    @DisplayName("Survives lethal damage during its controller's turn")
    void survivesLethalDamageDuringControllersTurn() {
        Permanent colossus = addCreatureReady(player1, new ColossusSteelStalwart());
        harness.forceActivePlayer(player1);
        colossus.setMarkedDamage(5);

        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Colossus, Steel Stalwart");
        harness.assertNotInGraveyard(player1, "Colossus, Steel Stalwart");
        assertThat(colossus.getMarkedDamage()).isEqualTo(5);
    }

    @Test
    @DisplayName("Dies to lethal damage during an opponent's turn and stops boosting Mutants")
    void diesToLethalDamageDuringOpponentsTurn() {
        Permanent colossus = addCreatureReady(player1, new ColossusSteelStalwart());
        Permanent mutant = addCreatureReady(player1, new X23DeadlyWeapon());
        harness.forceActivePlayer(player2);
        assertThat(gqs.getEffectivePower(gd, mutant)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, mutant)).isEqualTo(4);
        colossus.setMarkedDamage(5);

        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Colossus, Steel Stalwart");
        harness.assertInGraveyard(player1, "Colossus, Steel Stalwart");
        assertThat(gqs.getEffectivePower(gd, mutant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mutant)).isEqualTo(3);
    }
}
