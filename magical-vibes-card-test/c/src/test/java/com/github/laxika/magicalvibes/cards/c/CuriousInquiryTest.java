package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CuriousInquiry.class, GrizzlyBears.class})
class CuriousInquiryTest extends BaseCardTest {

    @Test
    void enchantedCreatureGetsPlusOnePlusOne() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachInquiry(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    void enchantedCreatureInvestigatesWhenItDealsCombatDamage() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachInquiry(creature);

        declareAttackers(player1, List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void enchantedCreatureControllerInvestigates() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new CuriousInquiry());
        aura.setAttachedTo(creature.getId());

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(findPermanents(player2, "Clue")).hasSize(1);
    }

    @Test
    void unenchantedCreatureDoesNotInvestigate() {
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    private void attachInquiry(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new CuriousInquiry());
        aura.setAttachedTo(creature.getId());
    }

    @Test
    void grantedTriggerBelongsToEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attachInquiry(creature);

        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, () -> {
            declareAttackers(player2, List.of(0));
            resolveCombat(player2);
        });

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player2.getId());
        assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(creature.getId());
    }

    @Test
    void clueCanBeSacrificedForTwoManaToDraw() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachInquiry(creature);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(player1, List.of(0));
        resolveCombat();
        resolveAllTriggers();

        Permanent clue = findPermanent(player1, "Clue");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(clue), null, null);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst()).isInstanceOf(GrizzlyBears.class);
    }

    @Test
    void auraCanBeCastOnOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CuriousInquiry()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Curious Inquiry").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }
}
