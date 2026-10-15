package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrabUmbra.class, GrizzlyBears.class})
class CrabUmbraTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Crab Umbra untaps the enchanted creature")
    void activatingAbilityUntapsEnchantedCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.tap();
        attachAura(creature);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Umbra armor saves an enchanted creature and destroys Crab Umbra")
    void umbraArmorSavesEnchantedCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = attachAura(creature);
        creature.setMarkedDamage(3);

        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Crab Umbra");
        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aura);
    }

    private Permanent attachAura(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new CrabUmbra());
        aura.setAttachedTo(creature.getId());
        return aura;
    }

    @Test
    @DisplayName("Crab Umbra can enchant and untap an opponent's creature")
    void enchantsAndUntapsOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.tap();
        harness.setHand(player1, List.of(new CrabUmbra()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Crab Umbra").getAttachedTo()).isEqualTo(creature.getId());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        assertThat(creature.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Untap ability resolves after umbra armor destroys the Aura")
    void untapsAfterAuraLeavesBattlefield() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.tap();
        attachAura(creature);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, null, null);

        creature.setMarkedDamage(3);
        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Crab Umbra");
        assertThat(creature.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Umbra armor does not save a creature with zero toughness")
    void doesNotPreventZeroToughnessDeath() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachAura(creature);
        creature.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);

        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Crab Umbra");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The creature's controller chooses which umbra armor replacement to apply")
    void choosesBetweenMultipleUmbraAuras() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent firstAura = attachAura(creature);
        Permanent secondAura = attachAura(creature);
        creature.setMarkedDamage(3);

        harness.runStateBasedActions();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(firstAura, secondAura);
    }
}
