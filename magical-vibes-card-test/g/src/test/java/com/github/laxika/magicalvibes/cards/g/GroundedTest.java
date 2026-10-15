package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.Cloudshift;
import com.github.laxika.magicalvibes.cards.l.LeapOfFaith;
import com.github.laxika.magicalvibes.cards.n.NaturalEnd;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.cards.t.ThrabenValiant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Grounded.class, SuntailHawk.class, Plains.class, NaturalEnd.class,
        Cloudshift.class, ThrabenValiant.class, LeapOfFaith.class})
class GroundedTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature loses flying")
    void enchantedCreatureLosesFlying() {
        Permanent hawk = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        assertThat(gqs.hasKeyword(gd, hawk, Keyword.FLYING)).isTrue();

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Grounded());
        aura.setAttachedTo(hawk.getId());

        assertThat(gqs.hasKeyword(gd, hawk, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Other creatures keep flying")
    void otherCreaturesKeepFlying() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Grounded());
        aura.setAttachedTo(enchanted.getId());

        assertThat(gqs.hasKeyword(gd, enchanted, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new SuntailHawk());
        harness.addToBattlefield(player1, new Plains());
        harness.setHand(player1, List.of(new Grounded()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        Permanent land = findPermanent(player1, "Plains");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void castingOnOpposingCreatureRemovesFlyingUntilAuraIsDestroyed() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent hawk = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        harness.setHand(player1, List.of(new Grounded(), new NaturalEnd()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castEnchantment(player1, 0, hawk.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Grounded");
        assertThat(aura.getAttachedTo()).isEqualTo(hawk.getId());
        assertThat(gqs.hasKeyword(gd, hawk, Keyword.FLYING)).isFalse();

        harness.castAndResolveInstant(player1, 0, aura.getId());

        harness.assertInGraveyard(player1, "Grounded");
        harness.assertNotOnBattlefield(player1, "Grounded");
        assertThat(gqs.hasKeyword(gd, hawk, Keyword.FLYING)).isTrue();
    }

    @Test
    void canEnchantOwnCreatureWithoutFlyingAndPreservesVigilance() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ThrabenValiant());
        harness.setHand(player1, List.of(new Grounded()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grounded").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void blinkedTargetMakesAuraSpellFailToResolve() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent hawk = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        harness.setHand(player1, List.of(new Grounded()));
        harness.setHand(player2, List.of(new Cloudshift()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, hawk.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, hawk.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Grounded");
        harness.assertInGraveyard(player1, "Grounded");
        Permanent returned = findPermanent(player2, "Suntail Hawk");
        assertThat(returned.getId()).isNotEqualTo(hawk.getId());
        assertThat(gqs.hasKeyword(gd, returned, Keyword.FLYING)).isTrue();
    }

    @Test
    void laterFlyingGrantWorksWhileGroundedRemainsAttached() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ThrabenValiant());
        harness.setHand(player1, List.of(new Grounded(), new LeapOfFaith()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(findPermanent(player1, "Grounded").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    void groundedRemovesFlyingGrantedBeforeItResolves() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ThrabenValiant());
        harness.setHand(player1, List.of(new LeapOfFaith(), new Grounded()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grounded").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
    }
}
