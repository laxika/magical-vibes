package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DiregrafGhoul;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RiseFromTheGrave;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed({HeadlessRider.class, DiregrafGhoul.class, GrizzlyBears.class, Shock.class, RiseFromTheGrave.class})
class HeadlessRiderTest extends BaseCardTest {

    @Test
    @DisplayName("Another nontoken Zombie dying creates a 2/2 black Zombie token")
    void createsZombieWhenAnotherNontokenZombieDies() {
        harness.addToBattlefield(player1, new HeadlessRider());
        harness.addToBattlefield(player1, new DiregrafGhoul());

        killWithShock(player2, player1, "Diregraf Ghoul");

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        List<Permanent> zombies = findPermanents(player1, "Zombie");
        assertThat(zombies).hasSize(1);
        Permanent zombie = zombies.getFirst();
        assertThat(zombie.getCard().isToken()).isTrue();
        assertThat(zombie.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(zombie.getCard().getSubtypes()).containsExactly(CardSubtype.ZOMBIE);
        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, zombie)).isEqualTo(2);
    }

    @Test
    @DisplayName("A non-Zombie creature dying does not trigger")
    void doesNotTriggerForNonZombie() {
        harness.addToBattlefield(player1, new HeadlessRider());
        harness.addToBattlefield(player1, new GrizzlyBears());

        killWithShock(player2, player1, "Grizzly Bears");

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Zombie")).isEmpty();
    }

    @Test
    @DisplayName("A Zombie token dying does not trigger")
    void doesNotTriggerForZombieToken() {
        harness.addToBattlefield(player1, new HeadlessRider());

        Card zombieTokenCard = new Card();
        zombieTokenCard.setName("Zombie");
        zombieTokenCard.setType(CardType.CREATURE);
        zombieTokenCard.setToken(true);
        zombieTokenCard.setColor(CardColor.BLACK);
        zombieTokenCard.setPower(2);
        zombieTokenCard.setToughness(2);
        zombieTokenCard.setSubtypes(List.of(CardSubtype.ZOMBIE));
        Permanent zombieToken = harness.addToBattlefieldAndReturn(player1, zombieTokenCard);

        zombieToken.setMarkedDamage(2);
        harness.runStateBasedActions();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Zombie")).isEmpty();
    }

    @Test
    @DisplayName("Headless Rider dying creates a Zombie token")
    void createsZombieWhenItselfDies() {
        harness.addToBattlefield(player1, new HeadlessRider());

        killWithShock(player2, player1, "Headless Rider");

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
    }

    @Test
    @DisplayName("An opposing Zombie's death creates a token only for its own controller")
    void doesNotTriggerForOpponentsZombie() {
        harness.addToBattlefield(player1, new HeadlessRider());
        Permanent opposingRider = harness.addToBattlefieldAndReturn(player2, new HeadlessRider());

        opposingRider.setMarkedDamage(1);
        harness.runStateBasedActions();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Zombie")).isEmpty();
        assertThat(findPermanents(player2, "Zombie")).hasSize(1);
    }

    @Test
    @DisplayName("Two Headless Riders dying simultaneously each trigger for both deaths")
    void simultaneousRiderDeathsCreateFourZombies() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new HeadlessRider());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new HeadlessRider());

        first.setMarkedDamage(1);
        second.setMarkedDamage(1);
        harness.runStateBasedActions();

        assertThat(gd.stack).hasSize(4);
        for (int i = 0; i < 4; i++) {
            harness.passBothPriorities();
        }

        assertThat(findPermanents(player1, "Headless Rider")).isEmpty();
        assertThat(findPermanents(player1, "Zombie")).hasSize(4);
    }

    @Test
    @DisplayName("A token Headless Rider triggers for itself but not for another token Rider")
    void tokenRiderTriggersOnlyForItsOwnDeath() {
        harness.addToBattlefield(player1, new HeadlessRider());
        HeadlessRider tokenCard = new HeadlessRider();
        tokenCard.setToken(true);
        Permanent tokenRider = harness.addToBattlefieldAndReturn(player1, tokenCard);

        tokenRider.setMarkedDamage(1);
        harness.runStateBasedActions();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Headless Rider")).hasSize(1);
        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
    }

    @Test
    @DisplayName("A creature made a Zombie by Rise from the Grave triggers its controller's Rider")
    void triggersForZombieSubtypeGrantedByReanimation() {
        harness.addToBattlefield(player1, new HeadlessRider());
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new RiseFromTheGrave()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);

        Permanent reanimated = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.hasEffectiveSubtype(gd, reanimated, CardSubtype.ZOMBIE)).isTrue();

        killWithShock(player2, player1, "Grizzly Bears");

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
        assertThat(findPermanents(player2, "Zombie")).isEmpty();
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
}
