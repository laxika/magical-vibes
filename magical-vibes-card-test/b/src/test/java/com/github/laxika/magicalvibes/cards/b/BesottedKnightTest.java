package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BesottedKnight.class, BetrothTheBeast.class, GrizzlyBears.class, Shock.class})
class BesottedKnightTest extends BaseCardTest {

    @Test
    void adventureCreatesRoyalRoleAttachedToTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BesottedKnight()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAdventure(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent role = findPermanent(player1, "Royal");
        assertThat(role.getCard().isToken()).isTrue();
        assertThat(role.getCard().isAura()).isTrue();
        assertThat(role.getCard().getSubtypes()).contains(CardSubtype.ROLE);
        assertThat(role.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    void royalRoleGrantsWardToEnchantedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BesottedKnight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAdventure(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, creature.getId());

        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
    }

    @Test
    void adventureCanOnlyTargetCreatureYouControl() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BesottedKnight()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void onlyOneRoleControlledByTheSamePlayerStaysAttachedToApermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BesottedKnight(), new BesottedKnight()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAdventure(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.castAdventure(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.ROLE))
                .hasSize(1);
    }

    @Test
    void resolvedAdventureAllowsCastingKnightFromExile() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        BesottedKnight knight = new BesottedKnight();
        harness.setHand(player1, List.of(knight));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAdventure(player1, 0, creature.getId());
        harness.passBothPriorities();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(knight);

        harness.castFromExile(player1, knight.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Besotted Knight");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(knight);
        assertThat(findPermanent(player1, "Royal").getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void adventureWithRemovedTargetGoesToGraveyardWithoutCreatingRole() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BesottedKnight()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAdventure(player1, 0, creature.getId());
        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Besotted Knight");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertNotOnBattlefield(player1, "Royal");
    }

    @Test
    void payingRoyalWardAllowsOpponentsSpellToResolve() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BesottedKnight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAdventure(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void royalWardDoesNotCounterControllersOwnSpell() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BesottedKnight(), new Shock()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAdventure(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Shock");
    }
}
