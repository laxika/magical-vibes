package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.p.Persuasion;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GetOut.class, GrizzlyBears.class, Pacifism.class, GloriousAnthem.class, Persuasion.class})
class GetOutTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a creature spell")
    void countersCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new GetOut()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Counters an enchantment spell")
    void countersEnchantmentSpell() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Pacifism pacifism = new Pacifism();
        harness.setHand(player1, List.of(pacifism));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.setHand(player2, List.of(new GetOut()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 0, pacifism.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Pacifism");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Returns one or two creatures and enchantments you own")
    void returnsOneOrTwoOwnedPermanents() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());

        harness.setHand(player1, List.of(new GetOut()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castModalInstant(player1, 0, 1, List.of(creature.getId(), enchantment.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Glorious Anthem");
    }

    @Test
    @DisplayName("Cannot return a permanent you do not own")
    void cannotReturnPermanentYouDoNotOwn() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new GetOut()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1,
                List.of(opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Returns a single owned creature")
    void returnsSingleCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GetOut()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castModalInstant(player1, 0, 1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Returns a single owned enchantment")
    void returnsSingleEnchantment() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        harness.setHand(player1, List.of(new GetOut()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castModalInstant(player1, 0, 1, List.of(enchantment.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Glorious Anthem");
        harness.assertNotOnBattlefield(player1, "Glorious Anthem");
    }

    @Test
    @DisplayName("Return mode requires at least one target")
    void cannotReturnZeroTargets() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GetOut()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Return mode cannot target three permanents")
    void cannotReturnThreeTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        harness.setHand(player1, List.of(new GetOut()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Return mode cannot choose the same permanent twice")
    void cannotRepeatTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GetOut()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1,
                List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot counter an instant spell")
    void cannotCounterInstant() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        GetOut firstSpell = new GetOut();
        harness.setHand(player1, List.of(firstSpell));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setHand(player2, List.of(new GetOut()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castModalInstant(player1, 0, 1, List.of(creature.getId()));
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, 0, firstSpell.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Returns the remaining legal target when the other target leaves")
    void returnsRemainingLegalTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        harness.setHand(player1, List.of(new GetOut(), new GetOut()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castModalInstant(player1, 0, 1, List.of(creature.getId(), enchantment.getId()));
        harness.castModalInstant(player1, 0, 1, List.of(creature.getId()));
        harness.passBothPriorities();
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Glorious Anthem");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Glorious Anthem");
        harness.assertNotOnBattlefield(player1, "Glorious Anthem");
    }

    @Test
    @DisplayName("Returns an owned creature controlled by an opponent")
    void returnsOwnedCreatureControlledByOpponent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Persuasion()));
        harness.addMana(player2, ManaColor.BLUE, 5);
        harness.castEnchantment(player2, 0, creature.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new GetOut()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castModalInstant(player1, 0, 1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInHand(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Persuasion");
    }
}
