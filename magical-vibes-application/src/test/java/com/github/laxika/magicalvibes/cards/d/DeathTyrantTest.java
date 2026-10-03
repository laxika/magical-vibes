package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeathTyrant.class, GrizzlyBears.class, Shock.class})
class DeathTyrantTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Zombie when an attacking ally dies")
    void attackingAllyDeathCreatesZombie() {
        harness.addToBattlefield(player1, new DeathTyrant());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        killWithShock(player1, attacker);

        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
    }

    @Test
    @DisplayName("Creates a Zombie when Death Tyrant itself dies while attacking")
    void attackingDeathTyrantDeathCreatesZombie() {
        Permanent tyrant = addCreatureReady(player1, new DeathTyrant());
        tyrant.setAttacking(true);
        tyrant.setMarkedDamage(4);

        killWithShock(player1, tyrant);

        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
    }

    @Test
    @DisplayName("Creates a Zombie when a blocking opponent creature dies")
    void blockingOpponentDeathCreatesZombie() {
        harness.addToBattlefield(player1, new DeathTyrant());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);

        killWithShock(player1, blocker);

        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
    }

    @Test
    @DisplayName("Returns itself from the graveyard to the battlefield tapped")
    void graveyardAbilityReturnsTapped() {
        DeathTyrant tyrant = new DeathTyrant();
        harness.setGraveyard(player1, List.of(tyrant));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Death Tyrant");
        assertThat(returned.isTapped()).isTrue();
        harness.assertNotInGraveyard(player1, "Death Tyrant");
    }

    @Test
    void noncombatAllyDeathDoesNotCreateZombie() {
        harness.addToBattlefield(player1, new DeathTyrant());
        Permanent ally = addCreatureReady(player1, new GrizzlyBears());

        killWithShock(player1, ally);

        assertThat(findPermanents(player1, "Zombie")).isEmpty();
    }

    @Test
    void blockingAllyDeathDoesNotCreateZombie() {
        harness.addToBattlefield(player1, new DeathTyrant());
        Permanent ally = addCreatureReady(player1, new GrizzlyBears());
        ally.setBlocking(true);

        killWithShock(player1, ally);

        assertThat(findPermanents(player1, "Zombie")).isEmpty();
    }

    @Test
    void attackingOpponentDeathDoesNotCreateZombie() {
        harness.addToBattlefield(player1, new DeathTyrant());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());
        opponent.setAttacking(true);

        killWithShock(player1, opponent);

        assertThat(findPermanents(player1, "Zombie")).isEmpty();
    }

    @Test
    void nonattackingDeathTyrantDeathDoesNotCreateZombie() {
        Permanent tyrant = addCreatureReady(player1, new DeathTyrant());
        tyrant.setMarkedDamage(4);

        killWithShock(player1, tyrant);

        assertThat(findPermanents(player1, "Zombie")).isEmpty();
    }

    @Test
    void graveyardAbilityReturnsOnlyActivatedCopy() {
        DeathTyrant activated = new DeathTyrant();
        DeathTyrant other = new DeathTyrant();
        harness.setGraveyard(player1, List.of(activated, other));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Death Tyrant")).hasSize(1);
        Permanent returned = findPermanent(player1, "Death Tyrant");
        assertThat(returned.getCard().getId()).isEqualTo(activated.getId());
        assertThat(returned.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
    }

    private void killWithShock(com.github.laxika.magicalvibes.model.Player caster, Permanent target) {
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castAndResolveInstant(caster, 0, target.getId());
        resolveAllTriggers();
    }
}
