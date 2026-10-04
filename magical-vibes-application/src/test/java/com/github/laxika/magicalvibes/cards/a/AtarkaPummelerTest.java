package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.ColossodonYearling;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AtarkaPummeler.class, ColossodonYearling.class})
class AtarkaPummelerTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot activate formidable below total power eight")
    void cannotActivateBelowTotalPowerEight() {
        Permanent pummeler = addCreatureReady(player1, new AtarkaPummeler());
        addCreatureReady(player1, new ColossodonYearling());
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("total power");
        assertThat(gqs.hasKeyword(gd, pummeler, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Formidable grants menace to all creatures controlled by its controller")
    void grantsMenaceToControlledCreatures() {
        Permanent pummeler = addCreatureReady(player1, new AtarkaPummeler());
        Permanent firstBear = addCreatureReady(player1, new ColossodonYearling());
        Permanent secondBear = addCreatureReady(player1, new ColossodonYearling());
        Permanent opponentBear = addCreatureReady(player2, new ColossodonYearling());
        addManaForAbility();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, pummeler, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, firstBear, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, secondBear, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentBear, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Formidable menace lasts until end of turn")
    void menaceWearsOffAtEndOfTurn() {
        Permanent pummeler = addCreatureReady(player1, new AtarkaPummeler());
        addCreatureReady(player1, new ColossodonYearling());
        addCreatureReady(player1, new ColossodonYearling());
        addManaForAbility();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, pummeler, Keyword.MENACE)).isTrue();

        harness.passUntil(player1, TurnStep.DECLARE_ATTACKERS);
        declareAttackers(player1, java.util.List.of());
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, pummeler, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Opponent creatures do not contribute to formidable")
    void opponentPowerDoesNotEnableActivation() {
        addCreatureReady(player1, new AtarkaPummeler());
        addCreatureReady(player2, new AtarkaPummeler());
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("total power");
    }

    @Test
    @DisplayName("Formidable is not checked again when the ability resolves")
    void resolvesAfterTotalPowerDrops() {
        Permanent pummeler = addCreatureReady(player1, new AtarkaPummeler());
        Permanent support = addCreatureReady(player1, new AtarkaPummeler());
        addManaForAbility();

        harness.activateAbility(player1, 0, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(support);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, pummeler, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("The ability affects creatures present at resolution only")
    void selectsCreaturesAtResolution() {
        Permanent pummeler = addCreatureReady(player1, new AtarkaPummeler());
        addCreatureReady(player1, new AtarkaPummeler());
        addManaForAbility();

        harness.activateAbility(player1, 0, 0, null, null);
        Permanent beforeResolution = addCreatureReady(player1, new ColossodonYearling());
        harness.passBothPriorities();
        Permanent afterResolution = addCreatureReady(player1, new ColossodonYearling());

        assertThat(gqs.hasKeyword(gd, pummeler, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, beforeResolution, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, afterResolution, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Formidable counts current power and can activate while summoning sick")
    void countsCountersAndAllowsSummoningSickness() {
        Permanent pummeler = addCreatureReady(player1, new AtarkaPummeler());
        pummeler.setSummoningSick(true);
        Permanent yearling = addCreatureReady(player1, new ColossodonYearling());
        yearling.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        addManaForAbility();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, pummeler, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, yearling, Keyword.MENACE)).isTrue();
    }

    private void addManaForAbility() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
