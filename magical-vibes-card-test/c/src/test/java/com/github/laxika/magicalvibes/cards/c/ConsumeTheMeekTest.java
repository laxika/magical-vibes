package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BoarUmbra;
import com.github.laxika.magicalvibes.cards.d.DeathlessAngel;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.n.NestInvader;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ConsumeTheMeek.class, GrizzlyBears.class, CentaurCourser.class, HillGiant.class,
        Forest.class, DrudgeSkeletons.class, NestInvader.class, BoarUmbra.class, DeathlessAngel.class})
class ConsumeTheMeekTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys creatures with mana value 3 or less on both sides")
    void destroysCreaturesWithinManaValueLimit() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new CentaurCourser());
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player2, new Forest());

        castConsumeTheMeek();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Centaur Courser");
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Destroyed creatures cannot be regenerated")
    void cannotBeRegenerated() {
        Permanent skeletons = harness.addToBattlefieldAndReturn(player2, new DrudgeSkeletons());
        skeletons.setRegenerationShield(1);

        castConsumeTheMeek();

        harness.assertInGraveyard(player2, "Drudge Skeletons");
    }

    @Test
    @DisplayName("Destroys zero-mana-value creature tokens")
    void destroysSpawnTokens() {
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new NestInvader(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(findPermanents(player2, "Eldrazi Spawn")).hasSize(1);

        castConsumeTheMeek();

        harness.assertInGraveyard(player2, "Nest Invader");
        harness.assertNotOnBattlefield(player2, "Eldrazi Spawn");
    }

    @Test
    @DisplayName("Umbra armor saves an eligible creature despite the regeneration prohibition")
    void umbraArmorStillApplies() {
        Permanent invader = harness.addToBattlefieldAndReturn(player2, new NestInvader());
        Permanent umbra = harness.addToBattlefieldAndReturn(player2, new BoarUmbra());
        umbra.setAttachedTo(invader.getId());
        invader.setMarkedDamage(1);

        castConsumeTheMeek();

        harness.assertOnBattlefield(player2, "Nest Invader");
        harness.assertNotInGraveyard(player2, "Nest Invader");
        harness.assertInGraveyard(player2, "Boar Umbra");
        harness.assertNotOnBattlefield(player2, "Boar Umbra");
        assertThat(invader.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("An eligible creature with indestructible survives while other eligible creatures die")
    void indestructibleCreatureSurvives() {
        harness.addToBattlefield(player2, new DeathlessAngel());
        Permanent invader = harness.addToBattlefieldAndReturn(player2, new NestInvader());
        harness.addToBattlefield(player1, new NestInvader());
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.activateAbility(player2, 0, null, invader.getId());
        harness.passBothPriorities();

        castConsumeTheMeek();

        harness.assertOnBattlefield(player2, "Nest Invader");
        harness.assertNotInGraveyard(player2, "Nest Invader");
        harness.assertOnBattlefield(player2, "Deathless Angel");
        harness.assertInGraveyard(player1, "Nest Invader");
    }

    @Test
    @DisplayName("Resolves without any creatures or targets")
    void resolvesOnEmptyBattlefield() {
        castConsumeTheMeek();

        harness.assertInGraveyard(player1, "Consume the Meek");
        assertThat(gd.stack).isEmpty();
    }

    private void castConsumeTheMeek() {
        harness.castFromHand(player1, new ConsumeTheMeek(), "{3}{B}{B}");
        harness.passBothPriorities();
    }
}
