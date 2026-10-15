package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.k.KrumarInitiate;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DragonbroodsRelic.class, KrumarInitiate.class})
class DragonbroodsRelicTest extends BaseCardTest {

    @Test
    @DisplayName("Taps itself and an untapped creature to add mana of the chosen color")
    void tapsCreatureForMana() {
        Permanent relic = harness.addToBattlefieldAndReturn(player1, new DragonbroodsRelic());
        Permanent creature = addCreatureReady(player1, new KrumarInitiate());

        harness.activateAbility(player1, battlefieldIndex(player1, relic), 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(relic.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Creates an all-colored Dragon whose enter trigger deals 3 damage")
    void createsReliquaryDragon() {
        Permanent relic = harness.addToBattlefieldAndReturn(player1, new DragonbroodsRelic());
        Permanent target = addCreatureReady(player2, new KrumarInitiate());
        harness.setLife(player1, 20);
        addRequiredMana(player1);

        harness.activateAbility(player1, battlefieldIndex(player1, relic), 1, null, null);
        harness.assertInGraveyard(player1, "Dragonbroods' Relic");
        assertThat(countPermanents(player1, "Reliquary Dragon")).isZero();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dragonbroods' Relic");
        Permanent dragon = findPermanent(player1, "Reliquary Dragon");
        assertThat(dragon.getEffectivePower()).isEqualTo(4);
        assertThat(dragon.getEffectiveToughness()).isEqualTo(4);
        assertThat(dragon.getCard().getColors()).containsExactlyInAnyOrder(
                CardColor.WHITE, CardColor.BLUE, CardColor.BLACK, CardColor.RED, CardColor.GREEN);
        assertThat(dragon.getCard().getKeywords()).contains(Keyword.FLYING, Keyword.LIFELINK);
        harness.assertNotOnBattlefield(player2, "Krumar Initiate");
        harness.assertInGraveyard(player2, "Krumar Initiate");
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("The Dragon-making ability cannot be activated outside sorcery speed")
    void dragonAbilityRequiresSorcerySpeed() {
        Permanent relic = harness.addToBattlefieldAndReturn(player1, new DragonbroodsRelic());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        addRequiredMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(player1, relic), 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("The mana ability requires an untapped creature to tap")
    void manaAbilityRequiresCreature() {
        Permanent relic = harness.addToBattlefieldAndReturn(player1, new DragonbroodsRelic());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(player1, relic), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A summoning-sick creature can pay the mana ability's creature tap cost")
    void tapsSummoningSickCreatureWithoutUsingStack() {
        Permanent relic = harness.addToBattlefieldAndReturn(player1, new DragonbroodsRelic());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new KrumarInitiate());
        creature.setSummoningSick(true);

        harness.activateAbility(player1, battlefieldIndex(player1, relic), 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(creature.isTapped()).isTrue();
        assertThat(relic.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's creature cannot pay the mana ability's cost")
    void cannotTapOpponentsCreature() {
        Permanent relic = harness.addToBattlefieldAndReturn(player1, new DragonbroodsRelic());
        Permanent creature = addCreatureReady(player2, new KrumarInitiate());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(player1, relic), 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(creature.isTapped()).isFalse();
        assertThat(relic.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An already tapped creature cannot pay the mana ability's cost")
    void cannotTapTappedCreature() {
        Permanent relic = harness.addToBattlefieldAndReturn(player1, new DragonbroodsRelic());
        Permanent creature = addCreatureReady(player1, new KrumarInitiate());
        creature.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(player1, relic), 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(relic.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A tapped Relic can be sacrificed and its Dragon can damage a player with lifelink")
    void tappedRelicCreatesDragonThatDamagesPlayer() {
        Permanent relic = harness.addToBattlefieldAndReturn(player1, new DragonbroodsRelic());
        relic.tap();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addRequiredMana(player1);

        harness.activateAbility(player1, battlefieldIndex(player1, relic), 1, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Dragonbroods' Relic");
        assertThat(countPermanents(player1, "Reliquary Dragon")).isEqualTo(1);
        harness.assertLife(player2, 17);
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Insufficient mana cannot sacrifice the Relic")
    void insufficientManaDoesNotSacrificeRelic() {
        Permanent relic = harness.addToBattlefieldAndReturn(player1, new DragonbroodsRelic());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(player1, relic), 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Dragonbroods' Relic");
        assertThat(countPermanents(player1, "Reliquary Dragon")).isZero();
    }

    private void addRequiredMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 3);
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.GREEN, 1);
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
