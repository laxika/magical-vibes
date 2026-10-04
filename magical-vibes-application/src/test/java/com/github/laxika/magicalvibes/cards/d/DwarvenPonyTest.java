package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.w.WillowFaerie;
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

@CardUsed({DwarvenPony.class, DwarvenSeaClan.class, WillowFaerie.class})
class DwarvenPonyTest extends BaseCardTest {

    private Permanent addPony() {
        return addCreatureReady(player1, new DwarvenPony());
    }

    private void addManaForAbility() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Grants mountainwalk to a Dwarf, then it wears off at end of turn")
    void grantsMountainwalkToDwarf() {
        Permanent pony = addPony();
        Permanent dwarf = addCreatureReady(player1, new DwarvenSeaClan());
        addManaForAbility();

        harness.activateAbility(player1, 0, 0, null, dwarf.getId());
        assertThat(pony.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, dwarf, Keyword.MOUNTAINWALK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, dwarf, Keyword.MOUNTAINWALK)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-Dwarf creature")
    void cannotTargetNonDwarf() {
        addPony();
        Permanent faerie = addCreatureReady(player1, new WillowFaerie());
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, faerie.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target a Dwarf creature an opponent controls")
    void canTargetOpponentsDwarf() {
        addPony();
        Permanent opponentDwarf = addCreatureReady(player2, new DwarvenSeaClan());
        addManaForAbility();

        harness.activateAbility(player1, 0, 0, null, opponentDwarf.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, opponentDwarf, Keyword.MOUNTAINWALK)).isTrue();
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent pony = addPony();
        pony.setSummoningSick(true);
        Permanent dwarf = addCreatureReady(player1, new DwarvenSeaClan());
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, dwarf.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        Permanent pony = addPony();
        pony.setTapped(true);
        Permanent dwarf = addCreatureReady(player1, new DwarvenSeaClan());
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, dwarf.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot pay the activation cost without red mana")
    void cannotActivateWithoutRedMana() {
        Permanent pony = addPony();
        Permanent dwarf = addCreatureReady(player1, new DwarvenSeaClan());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, dwarf.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(pony.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability resolves even if the Pony leaves the battlefield")
    void resolvesWithoutSource() {
        Permanent pony = addPony();
        Permanent dwarf = addCreatureReady(player1, new DwarvenSeaClan());
        addManaForAbility();

        harness.activateAbility(player1, 0, 0, null, dwarf.getId());
        gd.playerBattlefields.get(player1.getId()).remove(pony);
        gd.playerGraveyards.get(player1.getId()).add(pony.getCard());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, dwarf, Keyword.MOUNTAINWALK)).isTrue();
    }

    @Test
    @DisplayName("The ability does not affect a replacement permanent when its target leaves")
    void doesNotGrantToReplacementTarget() {
        addPony();
        Permanent dwarf = addCreatureReady(player1, new DwarvenSeaClan());
        addManaForAbility();

        harness.activateAbility(player1, 0, 0, null, dwarf.getId());
        gd.playerBattlefields.get(player1.getId()).remove(dwarf);
        Permanent returnedDwarf = addCreatureReady(player1, dwarf.getCard());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, returnedDwarf, Keyword.MOUNTAINWALK)).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
