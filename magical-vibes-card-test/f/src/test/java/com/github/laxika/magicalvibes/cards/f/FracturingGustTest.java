package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.cards.d.DarksteelColossus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.r.RuleOfLaw;
import com.github.laxika.magicalvibes.cards.r.Regeneration;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({FracturingGust.class, GrizzlyBears.class, Ornithopter.class, RuleOfLaw.class,
        Regeneration.class, DarksteelColossus.class})
class FracturingGustTest extends BaseCardTest {

    private static final int STARTING_LIFE = 20;

    @Test
    @DisplayName("Destroys all artifacts and enchantments, gaining 2 life per destroyed")
    void destroysArtifactsAndEnchantmentsAndGainsLife() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new RuleOfLaw());
        harness.setHand(player1, List.of(new FracturingGust()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castAndResolveInstant(player1, 0);

        harness.assertNotOnBattlefield(player1, "Ornithopter");
        harness.assertNotOnBattlefield(player2, "Rule of Law");
        harness.assertLife(player1, STARTING_LIFE + 4);
    }

    @Test
    @DisplayName("Does not destroy nonartifact creatures")
    void doesNotDestroyCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.setHand(player1, List.of(new FracturingGust()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castAndResolveInstant(player1, 0);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Ornithopter");
        harness.assertLife(player1, STARTING_LIFE + 2);
    }

    @Test
    @DisplayName("Gains no life when no artifacts or enchantments are present")
    void gainsNoLifeWhenNothingToDestroy() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FracturingGust()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castAndResolveInstant(player1, 0);

        harness.assertLife(player1, STARTING_LIFE);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Indestructible artifacts survive and do not contribute to life gained")
    void excludesIndestructibleArtifactsFromLifeGain() {
        harness.addToBattlefield(player2, new DarksteelColossus());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.setHand(player1, List.of(new FracturingGust()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castAndResolveInstant(player1, 0);

        harness.assertOnBattlefield(player2, "Darksteel Colossus");
        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertLife(player1, STARTING_LIFE + 2);
        harness.assertLife(player2, STARTING_LIFE);
    }

    @Test
    @DisplayName("Regenerated artifacts survive while their regeneration Aura is destroyed")
    void excludesRegeneratedArtifactsFromLifeGain() {
        Permanent thopter = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Regeneration());
        aura.setAttachedTo(thopter.getId());
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.activateAbility(player2, 1, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new FracturingGust()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castAndResolveInstant(player1, 0);

        harness.assertOnBattlefield(player2, "Ornithopter");
        harness.assertNotOnBattlefield(player2, "Regeneration");
        harness.assertLife(player1, STARTING_LIFE + 2);
        harness.assertLife(player2, STARTING_LIFE);
    }

    @Test
    @DisplayName("An artifact creature and its Aura both count as destroyed permanents")
    void countsDestroyedArtifactAndAttachedAura() {
        Permanent thopter = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Regeneration());
        aura.setAttachedTo(thopter.getId());
        harness.setHand(player1, List.of(new FracturingGust()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castAndResolveInstant(player1, 0);

        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertNotOnBattlefield(player2, "Regeneration");
        harness.assertLife(player1, STARTING_LIFE + 4);
        harness.assertLife(player2, STARTING_LIFE);
    }
}
