package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AllureOfPower;
import com.github.laxika.magicalvibes.cards.o.OrdinaryBear;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MyPrecious.class, AllureOfPower.class, OrdinaryBear.class, Forest.class, Mountain.class})
class MyPreciousTest extends BaseCardTest {

    @Test
    void equippedCreatureHasHexproofAndCannotBeBlocked() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new OrdinaryBear());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new MyPrecious());
        equipment.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasCantBeBlocked(gd, creature)).isTrue();
    }

    @Test
    void equipPaysLifeAndAttachesToTargetCreature() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new MyPrecious());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new OrdinaryBear());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(equipment), null,
                creature.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
        harness.assertLife(player1, 18);
    }

    @Test
    void adventureSacrificesCreatureDrawsTwoAndExilesTheCard() {
        MyPrecious card = new MyPrecious();
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new OrdinaryBear());
        Forest forest = new Forest();
        Mountain mountain = new Mountain();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(forest, mountain));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, sacrifice.getId(), List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(forest, mountain);
        harness.assertInGraveyard(player1, "Ordinary Bear");
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void adventureCannotBeCastWithoutAChosenCreature() {
        MyPrecious card = new MyPrecious();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, null, List.of()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    void equipCannotBeActivatedWithoutTwoMana() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new MyPrecious());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new OrdinaryBear());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(equipment.getAttachedTo()).isNull();
        harness.assertLife(player1, 20);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void equipPaysBothManaAndLifeBeforeResolution() {
        harness.addToBattlefield(player1, new MyPrecious());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new OrdinaryBear());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());

        harness.assertLife(player1, 18);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void adventurePaysCostsBeforeDrawingAndAllowsCastingEquipmentFromExile() {
        MyPrecious card = new MyPrecious();
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new OrdinaryBear());
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(new Forest(), new Mountain()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, sacrifice.getId(), List.of());

        harness.assertInGraveyard(player1, "Ordinary Bear");
        harness.assertNotOnBattlefield(player1, "Ordinary Bear");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "My Precious");
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void equipCannotBeActivatedWithoutEnoughLife() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new MyPrecious());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new OrdinaryBear());
        harness.setLife(player1, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(equipment.getAttachedTo()).isNull();
        harness.assertLife(player1, 1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    void equipCannotTargetAnOpponentsCreature() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new MyPrecious());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new OrdinaryBear());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(equipment.getAttachedTo()).isNull();
        harness.assertLife(player1, 20);
    }

    @Test
    void unattachedEquipmentDoesNotGrantAbilitiesAndDetachingRemovesThem() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new OrdinaryBear());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new MyPrecious());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasCantBeBlocked(gd, creature)).isFalse();

        equipment.setAttachedTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasCantBeBlocked(gd, creature)).isTrue();

        equipment.setAttachedTo(null);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasCantBeBlocked(gd, creature)).isFalse();
    }

    @Test
    void adventureCannotSacrificeAnOpponentsCreature() {
        MyPrecious card = new MyPrecious();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new OrdinaryBear());
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, creature.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Ordinary Bear");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }
}
