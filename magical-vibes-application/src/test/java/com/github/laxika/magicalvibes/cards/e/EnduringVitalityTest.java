package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OneWithTheStars;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EnduringVitality.class, GrizzlyBears.class, DoomBlade.class,
        Disenchant.class, OneWithTheStars.class})
class EnduringVitalityTest extends BaseCardTest {

    @Test
    @DisplayName("Gives creatures you control the ability to add one mana of any color")
    void givesControlledCreaturesAnyColorManaAbility() {
        Permanent vitality = addCreatureReady(player1, new EnduringVitality());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(vitality.isTapped()).isTrue();

        harness.activateAbility(player1, 1, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Returns from the graveyard as an enchantment and not a creature")
    void returnsAsEnchantmentOnly() {
        Permanent vitality = harness.addToBattlefieldAndReturn(player1, new EnduringVitality());

        harness.setHand(player2, java.util.List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);
        harness.castAndResolveInstant(player2, 0, vitality.getId());
        resolveAllTriggers();

        Permanent returned = findPermanent(player1, "Enduring Vitality");
        assertThat(gqs.getEffectiveCardTypes(gd, returned)).containsExactly(CardType.ENCHANTMENT);
        assertThat(gqs.isCreature(gd, returned)).isFalse();
        assertThat(gqs.isEnchantment(gd, returned)).isTrue();
        assertThat(gs.getEffectiveActivatedAbilities(gd, returned)).isEmpty();
    }

    @Test
    @DisplayName("Does not return when it dies as a noncreature")
    void doesNotReturnWhenItWasNotACreature() {
        Permanent vitality = harness.addToBattlefieldAndReturn(player1, new EnduringVitality());

        harness.setHand(player1, List.of(new OneWithTheStars()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0, vitality.getId());
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, vitality)).isFalse();

        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, vitality.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Enduring Vitality");
        harness.assertNotOnBattlefield(player1, "Enduring Vitality");
    }

    @Test
    @DisplayName("Returned enchantment still grants mana abilities and stops granting them after destruction")
    void returnedEnchantmentGrantsManaUntilDestroyed() {
        Permanent vitality = harness.addToBattlefieldAndReturn(player1, new EnduringVitality());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new DoomBlade(), new Disenchant()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.forceActivePlayer(player2);
        harness.castAndResolveInstant(player2, 0, vitality.getId());
        resolveAllTriggers();

        Permanent returned = findPermanent(player1, "Enduring Vitality");
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");
        assertThat(bears.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);

        harness.castAndResolveInstant(player2, 0, returned.getId());
        resolveAllTriggers();
        harness.assertInGraveyard(player1, "Enduring Vitality");
        harness.assertNotOnBattlefield(player1, "Enduring Vitality");
        assertThat(gs.getEffectiveActivatedAbilities(gd, bears)).isEmpty();
    }

    @Test
    @DisplayName("Does not grant mana abilities to opposing creatures")
    void doesNotGrantManaToOpponents() {
        addCreatureReady(player1, new EnduringVitality());
        addCreatureReady(player2, new GrizzlyBears());

        assertThat(org.assertj.core.api.Assertions.catchThrowable(
                () -> harness.activateAbility(player2, 0, null, null)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanent(player2, "Grizzly Bears").isTapped()).isFalse();
    }
}
