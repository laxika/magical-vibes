package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VileRedeemer.class})
class VileRedeemerTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {C} creates one Eldrazi Scion for each of your nontoken creature deaths")
    void payingColorlessCreatesScionsForOwnDeaths() {
        gd.nontokenCreatureDeathCountThisTurn.put(player1.getId(), 2);
        gd.nontokenCreatureDeathCountThisTurn.put(player2.getId(), 3);

        castVileRedeemer(3);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        List<Permanent> scions = findPermanents(player1, "Eldrazi Scion");
        assertThat(scions).hasSize(2);

        Permanent scion = scions.getFirst();
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(scion), null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the optional payment creates no Eldrazi Scions")
    void decliningColorlessCreatesNoScions() {
        gd.nontokenCreatureDeathCountThisTurn.put(player1.getId(), 2);

        castVileRedeemer(3);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Eldrazi Scion")).isEmpty();
    }

    private void castVileRedeemer(int colorlessMana) {
        harness.addMana(player1, ManaColor.COLORLESS, colorlessMana - 2);
        harness.castFromHand(player1, new VileRedeemer(), "{2}{G}");
    }

    @Test
    void payingWithNoDeathsCreatesNoTokens() {
        castVileRedeemer(3);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Eldrazi Scion")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void enteringWithoutBeingCastDoesNotCreateTokens() {
        gd.nontokenCreatureDeathCountThisTurn.put(player1.getId(), 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addToBattlefield(player1, new VileRedeemer());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(findPermanents(player1, "Eldrazi Scion")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void coloredManaCannotPayTheColorlessCost() {
        gd.nontokenCreatureDeathCountThisTurn.put(player1.getId(), 1);
        castVileRedeemer(2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Eldrazi Scion")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void countsRealDeathsIncludingDeathsAfterCastingButNotOpponentsOrTokens() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new VileRedeemer());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new VileRedeemer());
        harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, ownCreature);
        harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, opposingCreature);

        castVileRedeemer(3);
        Permanent laterDeath = harness.addToBattlefieldAndReturn(player1, new VileRedeemer());
        harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, laterDeath);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Vile Redeemer");
        harness.handleMayAbilityChosen(player1, true);
        assertThat(findPermanents(player1, "Eldrazi Scion")).hasSize(2);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Vile Redeemer");
        Permanent scion = findPermanents(player1, "Eldrazi Scion").getFirst();
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(scion), null, null);
        assertThat(findPermanents(player1, "Eldrazi Scion")).hasSize(1);

        castVileRedeemer(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(findPermanents(player1, "Eldrazi Scion")).hasSize(3);
    }
}
