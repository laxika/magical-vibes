package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
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
        Permanent aura = new Permanent(new CuriousInquiry());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

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
        Permanent aura = new Permanent(new CuriousInquiry());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
    }
}
