package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.cards.i.IntangibleVirtue;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RuleOfLaw;
import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({Paraselene.class, RuleOfLaw.class, AngelicChorus.class, GrizzlyBears.class, IntangibleVirtue.class})
class ParaseleneTest extends BaseCardTest {

    private static final int STARTING_LIFE = 20;

    @Test
    @DisplayName("Destroys all enchantments and gains 1 life per destroyed")
    void destroysAllEnchantmentsAndGainsLife() {
        harness.addToBattlefield(player1, new RuleOfLaw());
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.setHand(player1, List.of(new Paraselene()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player1, "Rule of Law");
        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertLife(player1, STARTING_LIFE + 2);
    }

    @Test
    @DisplayName("Gains no life when no enchantments are on the battlefield")
    void gainsNoLifeWhenNoEnchantments() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Paraselene()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertLife(player1, STARTING_LIFE);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Gains 1 life for a single destroyed enchantment")
    void gainsOneLifeForSingleEnchantment() {
        harness.addToBattlefield(player1, new RuleOfLaw());
        harness.setHand(player1, List.of(new Paraselene()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInGraveyard(player1, "Rule of Law");
        harness.assertLife(player1, STARTING_LIFE + 1);
    }

    @Test
    @DisplayName("Does not destroy creatures")
    void doesNotDestroyCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new RuleOfLaw());
        harness.setHand(player1, List.of(new Paraselene()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Rule of Law");
    }

    @Test
    @DisplayName("Paraselene goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new Paraselene()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInGraveyard(player1, "Paraselene");
    }

    @Test
    @DisplayName("Only destroyed enchantments count toward life gain")
    void survivingIndestructibleEnchantmentDoesNotCount() {
        var survivor = harness.addToBattlefieldAndReturn(player1, new IntangibleVirtue());
        survivor.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        harness.addToBattlefield(player2, new IntangibleVirtue());
        harness.setHand(player1, List.of(new Paraselene()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertOnBattlefield(player1, "Intangible Virtue");
        harness.assertInGraveyard(player2, "Intangible Virtue");
        harness.assertLife(player1, STARTING_LIFE + 1);
        harness.assertLife(player2, STARTING_LIFE);
    }

    @Test
    @DisplayName("Gains no life when all enchantments are indestructible")
    void gainsNoLifeWhenNoEnchantmentIsDestroyed() {
        var enchantment = harness.addToBattlefieldAndReturn(player2, new IntangibleVirtue());
        enchantment.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        harness.setHand(player1, List.of(new Paraselene()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertOnBattlefield(player2, "Intangible Virtue");
        harness.assertLife(player1, STARTING_LIFE);
        harness.assertLife(player2, STARTING_LIFE);
        harness.assertInGraveyard(player1, "Paraselene");
    }

    @Test
    @DisplayName("Destroys opposing enchantments with hexproof without targeting")
    void destroysHexproofEnchantment() {
        var enchantment = harness.addToBattlefieldAndReturn(player2, new IntangibleVirtue());
        enchantment.getGrantedKeywords().add(Keyword.HEXPROOF);
        harness.setHand(player1, List.of(new Paraselene()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInGraveyard(player2, "Intangible Virtue");
        harness.assertLife(player1, STARTING_LIFE + 1);
        harness.assertLife(player2, STARTING_LIFE);
    }
}
