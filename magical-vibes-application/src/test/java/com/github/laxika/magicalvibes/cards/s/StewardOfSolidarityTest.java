package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StewardOfSolidarityTest extends BaseCardTest {

    @Test
    @DisplayName("Ability creates a 1/1 white Warrior token with vigilance")
    void abilityCreatesWarriorToken() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new StewardOfSolidarity());
        Permanent steward = findPermanent(player1, "Steward of Solidarity");
        steward.setSummoningSick(false);

        int stewardIdx = gd.playerBattlefields.get(player1.getId()).indexOf(steward);
        harness.activateAbility(player1, stewardIdx, 0, null, null);
        harness.passBothPriorities();

        Permanent warrior = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Warrior"))
                .findFirst().orElseThrow();
        assertThat(warrior.getCard().getPower()).isEqualTo(1);
        assertThat(warrior.getCard().getToughness()).isEqualTo(1);
        assertThat(warrior.getCard().getSubtypes()).contains(CardSubtype.WARRIOR);
        assertThat(warrior.getCard().getKeywords()).contains(Keyword.VIGILANCE);
        assertThat(warrior.getCard().getColor()).isEqualTo(CardColor.WHITE);
    }

    @Test
    @DisplayName("Ability taps Steward and exerts it (won't untap next untap step)")
    void abilityTapsAndExertsSteward() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new StewardOfSolidarity());
        Permanent steward = findPermanent(player1, "Steward of Solidarity");
        steward.setSummoningSick(false);

        int stewardIdx = gd.playerBattlefields.get(player1.getId()).indexOf(steward);
        harness.activateAbility(player1, stewardIdx, 0, null, null);
        assertThat(steward.isTapped()).isTrue();

        harness.passBothPriorities();
        assertThat(steward.getSkipUntapCount()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Ability cannot be activated with summoning sickness")
    void abilityCannotActivateWithSummoningSickness() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new StewardOfSolidarity());
        // Steward has summoning sickness (default) — a {T} ability can't be activated.

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
