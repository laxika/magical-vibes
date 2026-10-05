package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BorosLocket;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LegionGuildmage.class, BorosLocket.class})
class LegionGuildmageTest extends BaseCardTest {

    @Test
    @DisplayName("Damage ability deals 3 damage to each opponent")
    void damageAbilityDealsThreeToEachOpponent() {
        addCreatureReady(player1, new LegionGuildmage());
        harness.addMana(player1, ManaColor.RED, 6);
        int opponentLife = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLife - 3);
    }

    @Test
    @DisplayName("Tap ability taps another target creature")
    void tapAbilityTapsAnotherCreature() {
        Permanent guildmage = addCreatureReady(player1, new LegionGuildmage());
        Permanent target = addCreatureReady(player2, new LegionGuildmage());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(guildmage.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tap ability cannot target Legion Guildmage itself")
    void tapAbilityCannotTargetItself() {
        Permanent guildmage = addCreatureReady(player1, new LegionGuildmage());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, guildmage.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another creature");
    }

    @Test
    @DisplayName("Tap ability can target another Guildmage controlled by its controller")
    void tapAbilityCanTargetAnotherFriendlyGuildmage() {
        Permanent source = addCreatureReady(player1, new LegionGuildmage());
        Permanent target = addCreatureReady(player1, new LegionGuildmage());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        assertThat(source.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An already tapped creature is a legal target")
    void tapAbilityCanTargetTappedCreature() {
        Permanent source = addCreatureReady(player1, new LegionGuildmage());
        Permanent target = addCreatureReady(player2, new LegionGuildmage());
        target.setTapped(true);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Both tap costs are restricted by summoning sickness")
    void summoningSicknessPreventsActivation(int abilityIndex) {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new LegionGuildmage());
        Permanent target = addCreatureReady(player2, new LegionGuildmage());
        harness.addMana(player1, ManaColor.RED, 6);
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null,
                abilityIndex == 1 ? target.getId() : null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(source.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("An already tapped Guildmage cannot pay either tap cost")
    void tappedSourceCannotActivate(int abilityIndex) {
        Permanent source = addCreatureReady(player1, new LegionGuildmage());
        source.setTapped(true);
        Permanent target = addCreatureReady(player2, new LegionGuildmage());
        harness.addMana(player1, ManaColor.RED, 6);
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null,
                abilityIndex == 1 ? target.getId() : null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(target.isTapped()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Each ability requires its full mana cost")
    void insufficientManaPreventsActivation(int abilityIndex) {
        Permanent source = addCreatureReady(player1, new LegionGuildmage());
        Permanent target = addCreatureReady(player2, new LegionGuildmage());
        harness.addMana(player1, abilityIndex == 0 ? ManaColor.RED : ManaColor.WHITE,
                abilityIndex == 0 ? 5 : 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null,
                abilityIndex == 1 ? target.getId() : null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(source.isTapped()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Each ability requires its colored mana component")
    void wrongManaColorPreventsActivation(int abilityIndex) {
        Permanent source = addCreatureReady(player1, new LegionGuildmage());
        Permanent target = addCreatureReady(player2, new LegionGuildmage());
        harness.addMana(player1, abilityIndex == 0 ? ManaColor.WHITE : ManaColor.RED,
                abilityIndex == 0 ? 6 : 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null,
                abilityIndex == 1 ? target.getId() : null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(source.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Damage ability resolves after its source leaves the battlefield")
    void damageAbilityResolvesWithoutSource() {
        Permanent source = addCreatureReady(player1, new LegionGuildmage());
        harness.addMana(player1, ManaColor.RED, 6);
        int controllerLife = gd.playerLifeTotals.get(player1.getId());
        int opponentLife = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(source.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLife);
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLife - 3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLife);
    }
    @Test
    @DisplayName("Tap ability cannot target a noncreature artifact")
    void tapAbilityCannotTargetNoncreature() {
        Permanent source = addCreatureReady(player1, new LegionGuildmage());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BorosLocket());
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another creature");
        assertThat(source.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tap ability still resolves after its source leaves the battlefield")
    void tapAbilityResolvesWithoutSource() {
        Permanent source = addCreatureReady(player1, new LegionGuildmage());
        Permanent target = addCreatureReady(player2, new LegionGuildmage());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tap ability does not affect a creature that has left and returned")
    void tapAbilityDoesNotFollowTargetToNewPermanent() {
        Permanent source = addCreatureReady(player1, new LegionGuildmage());
        Permanent target = addCreatureReady(player2, new LegionGuildmage());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        Permanent returned = addCreatureReady(player2, target.getCard());
        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
        assertThat(returned.isTapped()).isFalse();
    }
}
