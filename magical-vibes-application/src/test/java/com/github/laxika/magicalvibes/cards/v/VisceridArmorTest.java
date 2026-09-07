package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.s.SchoolOfTheUnseen;
import com.github.laxika.magicalvibes.cards.s.StormCrow;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VisceridArmor.class, StormCrow.class, SchoolOfTheUnseen.class})
class VisceridArmorTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Viscerid Armor attaches it and gives the creature +1/+1")
    void resolvingAttachesAndBoosts() {
        Permanent creature = addCreatureReady(player1, new StormCrow());

        harness.setHand(player1, List.of(new VisceridArmor()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Viscerid Armor")
                        && p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Viscerid Armor does not boost other creatures")
    void doesNotBoostOtherCreatures() {
        Permanent creature = addCreatureReady(player1, new StormCrow());

        Permanent otherCreature = addCreatureReady(player1, new StormCrow());

        Permanent armor = harness.addToBattlefieldAndReturn(player1, new VisceridArmor());
        armor.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Viscerid Armor can enchant an opponent's creature")
    void canEnchantOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new StormCrow());

        harness.setHand(player1, List.of(new VisceridArmor()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Activating {1}{U} returns Viscerid Armor to hand and the boost wears off")
    void activatedAbilityBouncesAura() {
        Permanent creature = addCreatureReady(player1, new StormCrow());

        Permanent armor = harness.addToBattlefieldAndReturn(player1, new VisceridArmor());
        armor.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Viscerid Armor");
        harness.assertNotOnBattlefield(player1, "Viscerid Armor");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Activating Viscerid Armor returns it to its owner's hand when another player controls it")
    void activatedAbilityReturnsAuraToOwnerHand() {
        Permanent creature = addCreatureReady(player2, new StormCrow());

        VisceridArmor auraCard = new VisceridArmor();
        auraCard.setOwnerId(player1.getId());
        Permanent armor = harness.addToBattlefieldAndReturn(player2, auraCard);
        armor.setAttachedTo(creature.getId());
        gd.stolenCreatures.put(armor.getId(), player1.getId());

        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 1, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Viscerid Armor");
        assertThat(gd.playerHands.get(player2.getId()))
                .noneMatch(card -> card.getName().equals("Viscerid Armor"));
        harness.assertNotOnBattlefield(player2, "Viscerid Armor");
    }

    @Test
    @DisplayName("Viscerid Armor fizzles if the target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent creature = addCreatureReady(player1, new StormCrow());

        harness.setHand(player1, List.of(new VisceridArmor()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Viscerid Armor");
        harness.assertNotOnBattlefield(player1, "Viscerid Armor");
    }

    @Test
    @DisplayName("Cannot enchant a land")
    void cannotEnchantALand() {
        harness.addToBattlefield(player1, new SchoolOfTheUnseen());
        harness.setHand(player1, List.of(new VisceridArmor()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent school = findPermanent(player1, "School of the Unseen");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, school.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
