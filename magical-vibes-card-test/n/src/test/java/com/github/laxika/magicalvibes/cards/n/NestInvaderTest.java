package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NestInvader.class})
class NestInvaderTest extends BaseCardTest {

    @Test
    @DisplayName("When Nest Invader enters, it creates one Eldrazi Spawn token")
    void enteringCreatesOneSpawnToken() {
        castNestInvader();

        assertThat(findPermanents(player1, "Eldrazi Spawn")).hasSize(1);
    }

    @Test
    @DisplayName("The Eldrazi Spawn can be sacrificed to add colorless mana")
    void spawnSacrificeAddsColorlessMana() {
        castNestInvader();

        Permanent spawn = findPermanent(player1, "Eldrazi Spawn");
        int spawnIndex = gd.playerBattlefields.get(player1.getId()).indexOf(spawn);

        harness.activateAbility(player1, spawnIndex, null, null);

        assertThat(findPermanents(player1, "Eldrazi Spawn")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("The Spawn is created only when the enter trigger resolves")
    void spawnWaitsForEnterTriggerResolution() {
        harness.setHand(player1, List.of(new NestInvader()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        assertThat(findPermanents(player1, "Eldrazi Spawn")).isEmpty();

        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Nest Invader")).hasSize(1);
        assertThat(findPermanents(player1, "Eldrazi Spawn")).isEmpty();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Eldrazi Spawn")).hasSize(1);
        assertThat(findPermanents(player2, "Eldrazi Spawn")).isEmpty();
    }

    @Test
    @DisplayName("A tapped Spawn can be sacrificed immediately without using the stack")
    void tappedNewSpawnProducesManaImmediately() {
        castNestInvader();
        Permanent spawn = findPermanent(player1, "Eldrazi Spawn");
        spawn.tap();
        int spawnIndex = gd.playerBattlefields.get(player1.getId()).indexOf(spawn);

        harness.activateAbility(player1, spawnIndex, null, null);

        assertThat(findPermanents(player1, "Eldrazi Spawn")).isEmpty();
        assertThat(findPermanents(player1, "Nest Invader")).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The created token is a 0/1 colorless Eldrazi Spawn creature")
    void createdSpawnHasOracleCharacteristics() {
        castNestInvader();
        Permanent spawn = findPermanent(player1, "Eldrazi Spawn");

        assertThat(spawn.getCard().isToken()).isTrue();
        assertThat(spawn.getCard().getColors()).isEmpty();
        assertThat(spawn.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(spawn.getCard().getSubtypes()).containsExactlyInAnyOrder(
                CardSubtype.ELDRAZI,
                CardSubtype.SPAWN);
        assertThat(gqs.getEffectivePower(gd, spawn)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, spawn)).isEqualTo(1);
    }

    private void castNestInvader() {
        harness.setHand(player1, List.of(new NestInvader()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
