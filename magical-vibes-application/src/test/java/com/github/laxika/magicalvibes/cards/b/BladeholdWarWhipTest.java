package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.c.Cankerbloom;
import com.github.laxika.magicalvibes.cards.c.CrawlingChorus;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BladeholdWarWhip.class, GrizzlyBears.class, LeoninScimitar.class,
        BarbedBatterfist.class, CrawlingChorus.class, Cankerbloom.class})
class BladeholdWarWhipTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Bladehold War-Whip creates and equips a 2/2 Rebel token with double strike")
    void enteringCreatesAndEquipsRebelWithDoubleStrike() {
        harness.setHand(player1, java.util.List.of(new BladeholdWarWhip()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent whip = findPermanent(player1, "Bladehold War-Whip");
        Permanent rebel = findPermanent(player1, "Rebel");

        assertThat(rebel.getCard().getPower()).isEqualTo(2);
        assertThat(rebel.getCard().getToughness()).isEqualTo(2);
        assertThat(rebel.getCard().getSubtypes()).contains(CardSubtype.REBEL);
        assertThat(whip.getAttachedTo()).isEqualTo(rebel.getId());
        assertThat(gqs.hasKeyword(gd, rebel, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Bladehold War-Whip reduces other Equipment equip abilities, not its own")
    void reducesOnlyOtherEquipmentEquipAbilities() {
        Permanent whip = harness.addToBattlefieldAndReturn(player1, new BladeholdWarWhip());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent scimitar = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        harness.activateAbility(player1, 2, null, creature.getId());
        harness.passBothPriorities();

        assertThat(scimitar.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(whip.getAttachedTo()).isNull();
    }

    @Test
    void equipAtFullCostMovesDoubleStrikeToNewCreature() {
        Permanent whip = harness.enterBattlefieldAndReturn(player1, new BladeholdWarWhip());
        harness.passBothPriorities();
        Permanent rebel = findPermanent(player1, "Rebel");
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CrawlingChorus());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(whip.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, rebel, Keyword.DOUBLE_STRIKE)).isFalse();
        harness.assertOnBattlefield(player1, "Rebel");
    }

    @Test
    void otherEquipmentCanEquipWithNoMana() {
        harness.addToBattlefield(player1, new BladeholdWarWhip());
        Permanent batterfist = harness.addToBattlefieldAndReturn(player1, new BarbedBatterfist());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Cankerbloom());

        harness.activateAbility(player1, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(batterfist.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void multipleCopiesReduceOtherWhipsButNotColoredMana() {
        Permanent whip = harness.addToBattlefieldAndReturn(player1, new BladeholdWarWhip());
        harness.addToBattlefield(player1, new BladeholdWarWhip());
        harness.addToBattlefield(player1, new BladeholdWarWhip());
        harness.addToBattlefield(player1, new BladeholdWarWhip());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CrawlingChorus());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(whip.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void discountDoesNotApplyToOpponent() {
        harness.addToBattlefield(player1, new BladeholdWarWhip());
        Permanent batterfist = harness.addToBattlefieldAndReturn(player2, new BarbedBatterfist());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Cankerbloom());
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(batterfist.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void forMirrodinStillCreatesTokenWhenEquipmentIsDestroyedInResponse() {
        harness.addToBattlefield(player1, new Cankerbloom());
        Permanent whip = harness.enterBattlefieldAndReturn(player1, new BladeholdWarWhip());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, whip.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Bladehold War-Whip");
        harness.passBothPriorities();

        Permanent rebel = findPermanent(player1, "Rebel");
        assertThat(rebel.getCard().getPower()).isEqualTo(2);
        assertThat(rebel.getCard().getToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, rebel, Keyword.DOUBLE_STRIKE)).isFalse();
    }
}
