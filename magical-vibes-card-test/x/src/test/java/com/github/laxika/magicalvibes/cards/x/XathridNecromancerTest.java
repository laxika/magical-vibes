package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.cards.m.Mutavault;
import com.github.laxika.magicalvibes.cards.p.PlanarCleansing;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({XathridNecromancer.class, EliteVanguard.class, GrizzlyBears.class, Shock.class,
        Mutavault.class, PlanarCleansing.class, TurnToFrog.class})
class XathridNecromancerTest extends BaseCardTest {

    @Test
    @DisplayName("Another Human you control dying creates a tapped 2/2 black Zombie token")
    void createsZombieWhenOtherHumanDies() {
        harness.addToBattlefield(player1, new XathridNecromancer());
        harness.addToBattlefield(player1, new EliteVanguard());

        killWithShock(player2, player1, "Elite Vanguard");

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        List<Permanent> zombies = zombieTokens(player1);
        assertThat(zombies).hasSize(1);
        Permanent zombie = zombies.getFirst();
        assertThat(zombie.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The Necromancer dying triggers on itself")
    void createsZombieWhenItselfDies() {
        harness.addToBattlefield(player1, new XathridNecromancer());

        killWithShock(player2, player1, "Xathrid Necromancer");

        harness.passBothPriorities();

        assertThat(zombieTokens(player1)).hasSize(1);
    }

    @Test
    @DisplayName("A non-Human creature you control dying does not trigger")
    void doesNotTriggerForNonHuman() {
        harness.addToBattlefield(player1, new XathridNecromancer());
        harness.addToBattlefield(player1, new GrizzlyBears());

        killWithShock(player2, player1, "Grizzly Bears");

        assertThat(gd.stack).isEmpty();
        assertThat(zombieTokens(player1)).isEmpty();
    }

    @Test
    @DisplayName("An opponent's Human dying does not trigger")
    void doesNotTriggerForOpponentHuman() {
        harness.addToBattlefield(player1, new XathridNecromancer());
        harness.addToBattlefield(player2, new EliteVanguard());

        killWithShock(player1, player2, "Elite Vanguard");

        assertThat(gd.stack).isEmpty();
        assertThat(zombieTokens(player1)).isEmpty();
    }

    @Test
    @DisplayName("Two Necromancers dying simultaneously each trigger for both Humans")
    void eachNecromancerSeesBothSimultaneousDeaths() {
        harness.addToBattlefield(player1, new XathridNecromancer());
        harness.addToBattlefield(player1, new XathridNecromancer());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new PlanarCleansing(), "{3}{W}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(4);
        for (int i = 0; i < 4; i++) {
            harness.passBothPriorities();
        }

        assertThat(zombieTokens(player1)).hasSize(4).allSatisfy(zombie -> {
            assertThat(zombie.isTapped()).isTrue();
            assertThat(gqs.isCreature(gd, zombie)).isTrue();
            assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, zombie)).isEqualTo(2);
            assertThat(gqs.getEffectiveColors(gd, zombie)).containsExactly(CardColor.BLACK);
            assertThat(gqs.hasEffectiveSubtype(gd, zombie, CardSubtype.ZOMBIE)).isTrue();
        });
        assertThat(zombieTokens(player2)).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An animated Mutavault dying qualifies as a Human from its last battlefield characteristics")
    void animatedMutavaultDeathCreatesZombie() {
        harness.addToBattlefield(player1, new XathridNecromancer());
        harness.addToBattlefield(player1, new Mutavault());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        killWithShock(player2, player1, "Mutavault");

        harness.assertInGraveyard(player1, "Mutavault");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(zombieTokens(player1)).hasSize(1);
        assertThat(zombieTokens(player1).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("A Necromancer that lost its ability does not trigger for another Human dying")
    void abilityLossPreventsOtherHumanDeathTrigger() {
        harness.addToBattlefield(player1, new XathridNecromancer());
        harness.addToBattlefield(player1, new EliteVanguard());
        turnToFrog("Xathrid Necromancer");

        killWithShock(player2, player1, "Elite Vanguard");

        assertThat(gd.stack).isEmpty();
        assertThat(zombieTokens(player1)).isEmpty();
    }

    @Test
    @DisplayName("A printed Human that became a Frog does not qualify for the other-Human death trigger")
    void humanThatBecameFrogDoesNotCreateZombie() {
        harness.addToBattlefield(player1, new XathridNecromancer());
        harness.addToBattlefield(player1, new EliteVanguard());
        turnToFrog("Elite Vanguard");

        killWithShock(player2, player1, "Elite Vanguard");

        assertThat(gd.stack).isEmpty();
        assertThat(zombieTokens(player1)).isEmpty();
    }

    private void turnToFrog(String targetName) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new TurnToFrog()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, targetName));
    }
    private void killWithShock(Player caster, Player targetController, String targetName) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(targetController, targetName);
        harness.castAndResolveInstant(caster, 0, targetId);
    }

    private List<Permanent> zombieTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .filter(p -> p.getCard().getName().equals("Zombie"))
                .toList();
    }
}
