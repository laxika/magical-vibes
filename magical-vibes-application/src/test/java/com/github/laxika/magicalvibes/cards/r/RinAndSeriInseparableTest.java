package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.m.MongrelPack;
import com.github.laxika.magicalvibes.cards.p.ProwlingCaracal;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RinAndSeriInseparable.class, MongrelPack.class, ProwlingCaracal.class})
class RinAndSeriInseparableTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Dog and Cat spells creates the opposite tokens")
    void castingDogAndCatSpellsCreatesOppositeTokens() {
        harness.addToBattlefield(player1, new RinAndSeriInseparable());

        harness.setHand(player1, List.of(new MongrelPack()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Cat")).hasSize(1)
                .allSatisfy(cat -> {
                    assertThat(cat.getCard().getColor()).isEqualTo(CardColor.GREEN);
                    assertThat(cat.getCard().getSubtypes()).containsExactly(CardSubtype.CAT);
                    assertThat(gqs.getEffectivePower(gd, cat)).isEqualTo(1);
                    assertThat(gqs.getEffectiveToughness(gd, cat)).isEqualTo(1);
                });

        harness.setHand(player1, List.of(new ProwlingCaracal()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Dog")).hasSize(1)
                .allSatisfy(dog -> {
                    assertThat(dog.getCard().getColor()).isEqualTo(CardColor.WHITE);
                    assertThat(dog.getCard().getSubtypes()).containsExactly(CardSubtype.DOG);
                    assertThat(gqs.getEffectivePower(gd, dog)).isEqualTo(1);
                    assertThat(gqs.getEffectiveToughness(gd, dog)).isEqualTo(1);
                });
    }

    @Test
    @DisplayName("The activated ability counts Dogs and Cats you control")
    void activatedAbilityCountsDogsAndCats() {
        addCreatureReady(player1, new RinAndSeriInseparable());
        addCreatureReady(player1, new MongrelPack());
        addCreatureReady(player1, new ProwlingCaracal());
        addCreatureReady(player1, new ProwlingCaracal());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
    }

    @Test
    void castingRinAndSeriDoesNotTriggerItsOwnAbilities() {
        harness.setHand(player1, List.of(new RinAndSeriInseparable()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Rin and Seri, Inseparable");
        assertThat(findPermanents(player1, "Cat")).isEmpty();
        assertThat(findPermanents(player1, "Dog")).isEmpty();
    }

    @Test
    void dogAndCatSpellTriggersBothAbilitiesBeforeSpellResolves() {
        harness.addToBattlefield(player1, new RinAndSeriInseparable());
        harness.setHand(player1, List.of(new RinAndSeriInseparable()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Cat")).hasSize(1);
        assertThat(findPermanents(player1, "Dog")).hasSize(1);
        assertThat(findPermanents(player1, "Rin and Seri, Inseparable")).hasSize(1);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void activatedAbilityUsesResolutionCountsAndIgnoresOpposingAnimals() {
        var source = addCreatureReady(player1, new RinAndSeriInseparable());
        harness.addToBattlefield(player2, new MongrelPack());
        harness.addToBattlefield(player2, new ProwlingCaracal());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        assertThat(source.isTapped()).isTrue();
        harness.addToBattlefield(player1, new MongrelPack());
        harness.addToBattlefield(player1, new ProwlingCaracal());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
    }

    @Test
    void activatedAbilityCanDamageCreatureAndStillGainLife() {
        var source = addCreatureReady(player1, new RinAndSeriInseparable());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, source.getId());
        harness.passBothPriorities();

        assertThat(source.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
    }
}
