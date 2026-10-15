package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YevaNaturesHerald;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CommandersPlate.class, GrizzlyBears.class, YevaNaturesHerald.class})
class CommandersPlateTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +3/+3")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent plate = harness.addToBattlefieldAndReturn(player1, new CommandersPlate());
        plate.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Protection excludes colors in the commander's color identity")
    void protectionUsesCommanderColorIdentity() {
        YevaNaturesHerald commander = new YevaNaturesHerald();
        gd.makeCommander(player1.getId(), commander);
        gd.playerCommandZones.get(player1.getId()).add(commander);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent plate = harness.addToBattlefieldAndReturn(player1, new CommandersPlate());
        plate.setAttachedTo(creature.getId());

        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.GREEN)).isFalse();

        gd.playerCommandZones.get(player1.getId()).clear();
        harness.setGraveyard(player1, List.of(commander));

        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.GREEN)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.RED)).isTrue();
    }

    @Test
    @DisplayName("Equip commander only targets a commander")
    void restrictedEquipTargetsCommander() {
        Permanent plate = harness.addToBattlefieldAndReturn(player1, new CommandersPlate());
        Permanent nonCommander = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, nonCommander.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("commander");
        assertThat(plate.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip commander attaches to a commander")
    void restrictedEquipAttachesToCommander() {
        Permanent plate = harness.addToBattlefieldAndReturn(player1, new CommandersPlate());
        Permanent commander = addCreatureReady(player1, new YevaNaturesHerald());
        commander.setCommander(true);
        gd.makeCommander(player1.getId(), commander.getCard());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, commander.getId());
        harness.passBothPriorities();

        assertThat(plate.getAttachedTo()).isEqualTo(commander.getId());
    }

    @Test
    @DisplayName("The generic equip ability attaches to any creature you control")
    void genericEquipAttachesToNonCommander() {
        Permanent plate = harness.addToBattlefieldAndReturn(player1, new CommandersPlate());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(plate.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Without a commander the Plate boosts the creature but grants no protection")
    void noCommanderGrantsNoProtection() {
        Permanent creature = addCreatureReady(player1, new YevaNaturesHerald());
        Permanent plate = harness.addToBattlefieldAndReturn(player1, new CommandersPlate());
        plate.setAttachedTo(creature.getId());

        for (CardColor color : CardColor.values()) {
            assertThat(gqs.hasProtectionFrom(gd, creature, color)).isFalse();
        }
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(7);
    }

    @ParameterizedTest
    @ValueSource(strings = {"hand", "library", "graveyard", "exile"})
    @DisplayName("The commander's color identity applies outside the command zone and battlefield")
    void commanderIdentityAppliesInOtherZones(String zone) {
        YevaNaturesHerald commander = new YevaNaturesHerald();
        switch (zone) {
            case "hand" -> harness.setHand(player1, List.of(commander));
            case "library" -> harness.setLibrary(player1, List.of(commander));
            case "graveyard" -> harness.setGraveyard(player1, List.of(commander));
            case "exile" -> harness.setExile(player1, List.of(commander));
            default -> throw new IllegalArgumentException(zone);
        }
        gd.makeCommander(player1.getId(), commander);
        Permanent creature = addCreatureReady(player1, new YevaNaturesHerald());
        Permanent plate = harness.addToBattlefieldAndReturn(player1, new CommandersPlate());
        plate.setAttachedTo(creature.getId());

        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.GREEN)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.WHITE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.BLUE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.BLACK)).isTrue();
    }

    @Test
    @DisplayName("Unattached Plate grants neither a boost nor protection")
    void unattachedPlateDoesNotAffectCreatures() {
        YevaNaturesHerald commander = new YevaNaturesHerald();
        gd.makeCommander(player1.getId(), commander);
        gd.playerCommandZones.get(player1.getId()).add(commander);
        Permanent creature = addCreatureReady(player1, new YevaNaturesHerald());
        harness.addToBattlefield(player1, new CommandersPlate());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.RED)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Equip requires the full three or five mana cost")
    void equipRequiresFullManaCost(int abilityIndex) {
        Permanent plate = harness.addToBattlefieldAndReturn(player1, new CommandersPlate());
        Permanent commander = addCreatureReady(player1, new YevaNaturesHerald());
        commander.setCommander(true);
        gd.makeCommander(player1.getId(), commander.getCard());
        harness.addMana(player1, ManaColor.COLORLESS, abilityIndex == 0 ? 2 : 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, commander.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(plate.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Protection uses the Equipment controller's commander even on an opponent's creature")
    void protectionUsesEquipmentController() {
        YevaNaturesHerald commander = new YevaNaturesHerald();
        gd.makeCommander(player1.getId(), commander);
        gd.playerCommandZones.get(player1.getId()).add(commander);
        Permanent creature = addCreatureReady(player2, new YevaNaturesHerald());
        Permanent plate = harness.addToBattlefieldAndReturn(player1, new CommandersPlate());
        plate.setAttachedTo(creature.getId());

        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.GREEN)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.RED)).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(7);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Neither equip ability can target a creature an opponent controls")
    void equipCannotTargetOpponentsCreature(int abilityIndex) {
        Permanent plate = harness.addToBattlefieldAndReturn(player1, new CommandersPlate());
        Permanent commander = addCreatureReady(player2, new YevaNaturesHerald());
        commander.setCommander(true);
        gd.makeCommander(player2.getId(), commander.getCard());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, commander.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(plate.getAttachedTo()).isNull();
    }

}
