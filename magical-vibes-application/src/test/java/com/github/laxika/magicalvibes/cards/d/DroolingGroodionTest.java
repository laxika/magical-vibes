package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SelesnyaGuildmage;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DroolingGroodion.class, Forest.class, SelesnyaGuildmage.class})
class DroolingGroodionTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature, then pumps the first target and weakens the second")
    void sacrificesAndModifiesBothTargets() {
        addCreatureReady(player1, new DroolingGroodion());
        Permanent fodder = addCreatureReady(player1, new SelesnyaGuildmage());
        Permanent firstTarget = addCreatureReady(player1, new SelesnyaGuildmage());
        Permanent secondTarget = addCreatureReady(player2, new SelesnyaGuildmage());
        addAbilityMana();

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(firstTarget.getId(), secondTarget.getId()));
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Drooling Groodion");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(fodder.getCard());
        assertThat(firstTarget.getPowerModifier()).isEqualTo(2);
        assertThat(firstTarget.getToughnessModifier()).isEqualTo(2);
        assertThat(secondTarget.getPowerModifier()).isEqualTo(-2);
        assertThat(secondTarget.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    @DisplayName("Cannot target the same creature for both target groups")
    void targetsMustBeDistinct() {
        addCreatureReady(player1, new DroolingGroodion());
        addCreatureReady(player1, new SelesnyaGuildmage());
        Permanent target = addCreatureReady(player2, new SelesnyaGuildmage());
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(target.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("different");
    }

    @Test
    @DisplayName("Can target only creatures")
    void targetsMustBeCreatures() {
        addCreatureReady(player1, new DroolingGroodion());
        Permanent creatureTarget = addCreatureReady(player2, new SelesnyaGuildmage());
        Permanent landTarget = harness.addToBattlefieldAndReturn(player2, new Forest());
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(creatureTarget.getId(), landTarget.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Can sacrifice itself after being chosen as the first target")
    void sourceCanBeSacrificedAfterBeingTargeted() {
        Permanent source = addCreatureReady(player1, new DroolingGroodion());
        Permanent secondTarget = addCreatureReady(player2, new SelesnyaGuildmage());
        addAbilityMana();

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(source.getId(), secondTarget.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(source.getCard());

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Drooling Groodion");
        assertThat(secondTarget.getPowerModifier()).isEqualTo(-2);
        assertThat(secondTarget.getToughnessModifier()).isEqualTo(-2);
        harness.assertInGraveyard(player2, "Selesnya Guildmage");
    }

    @Test
    @DisplayName("-2/-2 kills a 2/2 creature")
    void debuffKillsCreature() {
        addCreatureReady(player1, new DroolingGroodion());
        Permanent fodder = addCreatureReady(player1, new SelesnyaGuildmage());
        Permanent firstTarget = addCreatureReady(player1, new SelesnyaGuildmage());
        Permanent secondTarget = addCreatureReady(player2, new SelesnyaGuildmage());
        addAbilityMana();

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(firstTarget.getId(), secondTarget.getId()));
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Selesnya Guildmage");
        harness.assertInGraveyard(player2, "Selesnya Guildmage");
    }

    @Test
    @DisplayName("Sacrificing the second target still pumps the first target")
    void secondTargetCanBeSacrificed() {
        Permanent source = addCreatureReady(player1, new DroolingGroodion());
        Permanent firstTarget = addCreatureReady(player2, new SelesnyaGuildmage());
        addAbilityMana();

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(firstTarget.getId(), source.getId()));
        harness.assertInGraveyard(player1, "Drooling Groodion");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Selesnya Guildmage");
        assertThat(firstTarget.getPowerModifier()).isEqualTo(2);
        assertThat(firstTarget.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Both modifiers expire at end of turn")
    void modifiersExpireAtEndOfTurn() {
        Permanent source = addCreatureReady(player1, new DroolingGroodion());
        Permanent fodder = addCreatureReady(player1, new SelesnyaGuildmage());
        Permanent firstTarget = addCreatureReady(player2, new SelesnyaGuildmage());
        addAbilityMana();

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(firstTarget.getId(), source.getId()));
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        assertThat(firstTarget.getPowerModifier()).isEqualTo(2);
        assertThat(firstTarget.getToughnessModifier()).isEqualTo(2);
        assertThat(source.getPowerModifier()).isEqualTo(-2);
        assertThat(source.getToughnessModifier()).isEqualTo(-2);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        harness.assertOnBattlefield(player1, "Drooling Groodion");
        harness.assertOnBattlefield(player2, "Selesnya Guildmage");
        assertThat(firstTarget.getPowerModifier()).isZero();
        assertThat(firstTarget.getToughnessModifier()).isZero();
        assertThat(source.getPowerModifier()).isZero();
        assertThat(source.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Cannot activate with only one target")
    void requiresTwoTargets() {
        Permanent source = addCreatureReady(player1, new DroolingGroodion());
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(source.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Drooling Groodion");
        harness.assertNotInGraveyard(player1, "Drooling Groodion");
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
