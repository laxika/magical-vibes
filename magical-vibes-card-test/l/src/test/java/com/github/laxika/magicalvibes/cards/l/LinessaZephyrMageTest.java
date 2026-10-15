package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.CoalitionRelic;
import com.github.laxika.magicalvibes.cards.d.DakmorSalvage;
import com.github.laxika.magicalvibes.cards.e.EmblemOfTheWarmind;
import com.github.laxika.magicalvibes.cards.n.NessianCourser;
import com.github.laxika.magicalvibes.cards.r.RitesOfFlourishing;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LinessaZephyrMage.class, NessianCourser.class, CoalitionRelic.class,
        RitesOfFlourishing.class, DakmorSalvage.class, EmblemOfTheWarmind.class})
class LinessaZephyrMageTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability cannot be activated while Linessa is summoning sick")
    void tapAbilityRequiresLinessaToBeReady() {
        Permanent linessa = harness.addToBattlefieldAndReturn(player1, new LinessaZephyrMage());
        linessa.setSummoningSick(true);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new NessianCourser());
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 3, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Nessian Courser");
        assertThat(linessa.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Grandeur continues to the land when all earlier permanent types are missing")
    void grandeurReturnsLandWithoutEarlierPermanentTypes() {
        harness.addToBattlefield(player1, new LinessaZephyrMage());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new DakmorSalvage());
        harness.setHand(player1, List.of(new LinessaZephyrMage()));

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, land.getId());

        harness.assertNotOnBattlefield(player2, "Dakmor Salvage");
        harness.assertInHand(player2, "Dakmor Salvage");
        harness.assertInGraveyard(player1, "Linessa, Zephyr Mage");
    }

    @Test
    @DisplayName("Grandeur can return an Aura after returning the creature it enchanted")
    void grandeurReturnsAuraAfterItsEnchantedCreature() {
        harness.addToBattlefield(player1, new LinessaZephyrMage());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new NessianCourser());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new EmblemOfTheWarmind());
        aura.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new LinessaZephyrMage()));

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, creature.getId());
        harness.handlePermanentChosen(player2, aura.getId());

        harness.assertInHand(player2, "Nessian Courser");
        harness.assertInHand(player2, "Emblem of the Warmind");
        harness.assertNotInGraveyard(player2, "Emblem of the Warmind");
        harness.assertNotOnBattlefield(player2, "Emblem of the Warmind");
    }

    @Test
    @DisplayName("Tap ability returns a creature with mana value X")
    void returnsCreatureWithManaValueX() {
        Permanent linessa = harness.addToBattlefieldAndReturn(player1, new LinessaZephyrMage());
        linessa.setSummoningSick(false);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new NessianCourser());
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.activateAbility(player1, 0, 3, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Nessian Courser");
        harness.assertInHand(player2, "Nessian Courser");
        assertThat(linessa.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tap ability rejects a creature whose mana value is not X")
    void rejectsCreatureWithDifferentManaValue() {
        Permanent linessa = harness.addToBattlefieldAndReturn(player1, new LinessaZephyrMage());
        linessa.setSummoningSick(false);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new NessianCourser());
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Nessian Courser");
    }

    @Test
    @DisplayName("Tap ability rejects a noncreature with mana value X")
    void rejectsNonCreatureWithMatchingManaValue() {
        Permanent linessa = harness.addToBattlefieldAndReturn(player1, new LinessaZephyrMage());
        linessa.setSummoningSick(false);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new CoalitionRelic());
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 3, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Coalition Relic");
        assertThat(linessa.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Grandeur requires another Linessa in hand")
    void grandeurRequiresAnotherLinessa() {
        harness.addToBattlefield(player1, new LinessaZephyrMage());
        harness.setHand(player1, List.of(new NessianCourser()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Nessian Courser");
    }

    @Test
    @DisplayName("Grandeur returns a creature, artifact, enchantment, and land controlled by the target player")
    void grandeurReturnsEachPermanentType() {
        harness.addToBattlefield(player1, new LinessaZephyrMage());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new NessianCourser());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new CoalitionRelic());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new RitesOfFlourishing());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new DakmorSalvage());
        harness.setHand(player1, List.of(new LinessaZephyrMage()));

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, creature.getId());
        harness.handlePermanentChosen(player2, artifact.getId());
        harness.handlePermanentChosen(player2, enchantment.getId());
        harness.handlePermanentChosen(player2, land.getId());

        harness.assertNotOnBattlefield(player2, "Nessian Courser");
        harness.assertNotOnBattlefield(player2, "Coalition Relic");
        harness.assertNotOnBattlefield(player2, "Rites of Flourishing");
        harness.assertNotOnBattlefield(player2, "Dakmor Salvage");
        harness.assertInHand(player2, "Nessian Courser");
        harness.assertInHand(player2, "Coalition Relic");
        harness.assertInHand(player2, "Rites of Flourishing");
        harness.assertInHand(player2, "Dakmor Salvage");
        harness.assertInGraveyard(player1, "Linessa, Zephyr Mage");
    }

    @Test
    @DisplayName("Grandeur skips permanent types the target player does not control")
    void grandeurSkipsMissingPermanentTypes() {
        harness.addToBattlefield(player1, new LinessaZephyrMage());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NessianCourser());
        harness.setHand(player1, List.of(new LinessaZephyrMage()));

        harness.activateAbility(player1, 0, 1, null, player1.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());

        harness.assertNotOnBattlefield(player1, "Nessian Courser");
        harness.assertInHand(player1, "Nessian Courser");
        harness.assertInGraveyard(player1, "Linessa, Zephyr Mage");
    }
}
