package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AwakeningZone.class})
class AwakeningZoneTest extends BaseCardTest {

    @Test
    @DisplayName("At your upkeep, Awakening Zone may create an Eldrazi Spawn token")
    void upkeepCreatesEldraziSpawnToken() {
        harness.addToBattlefield(player1, new AwakeningZone());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent spawn = findPermanent(player1, "Eldrazi Spawn");
        assertThat(spawn.getCard().isToken()).isTrue();
        assertThat(spawn.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(spawn.getCard().getColor()).isNull();
        assertThat(spawn.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.ELDRAZI, CardSubtype.SPAWN);
        assertThat(gqs.getEffectivePower(gd, spawn)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, spawn)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining Awakening Zone's upkeep trigger creates no token")
    void decliningUpkeepTriggerCreatesNoToken() {
        harness.addToBattlefield(player1, new AwakeningZone());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Eldrazi Spawn")).isEmpty();
    }

    @Test
    @DisplayName("Awakening Zone does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new AwakeningZone());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Eldrazi Spawn")).isEmpty();
    }

    @Test
    @DisplayName("Awakening Zone's Spawn token can be sacrificed for colorless mana")
    void spawnTokenCanBeSacrificedForColorlessMana() {
        harness.addToBattlefield(player1, new AwakeningZone());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent spawn = findPermanent(player1, "Eldrazi Spawn");
        int spawnIndex = gd.playerBattlefields.get(player1.getId()).indexOf(spawn);
        harness.activateAbility(player1, spawnIndex, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(findPermanents(player1, "Eldrazi Spawn")).isEmpty();
    }

    @Test
    @DisplayName("An opponent's Awakening Zone creates its token under that opponent's control")
    void opponentControlledZoneCreatesTokenForOpponent() {
        harness.addToBattlefield(player2, new AwakeningZone());

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(findPermanents(player2, "Eldrazi Spawn")).hasSize(1);
        assertThat(findPermanents(player1, "Eldrazi Spawn")).isEmpty();
    }

    @Test
    @DisplayName("Each Awakening Zone offers an independent optional token")
    void multipleZonesOfferIndependentChoices() {
        harness.addToBattlefield(player1, new AwakeningZone());
        harness.addToBattlefield(player1, new AwakeningZone());

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Eldrazi Spawn")).hasSize(1);
        assertThat(findPermanents(player1, "Awakening Zone")).hasSize(2);
    }

    @Test
    @DisplayName("A tapped Spawn can be sacrificed immediately and its mana ability does not use the stack")
    void tappedNewSpawnCanBeSacrificedImmediately() {
        harness.addToBattlefield(player1, new AwakeningZone());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent spawn = findPermanent(player1, "Eldrazi Spawn");
        spawn.tap();
        int spawnIndex = gd.playerBattlefields.get(player1.getId()).indexOf(spawn);
        harness.activateAbility(player1, spawnIndex, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(findPermanents(player1, "Eldrazi Spawn")).isEmpty();
        assertThat(findPermanents(player1, "Awakening Zone")).hasSize(1);
    }
}
