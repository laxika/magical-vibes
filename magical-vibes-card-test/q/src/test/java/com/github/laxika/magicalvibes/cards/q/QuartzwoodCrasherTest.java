package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.AlmightyBrushwagg;
import com.github.laxika.magicalvibes.cards.m.MosscoatGoriak;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({QuartzwoodCrasher.class, GrizzlyBears.class, AlmightyBrushwagg.class, MosscoatGoriak.class})
class QuartzwoodCrasherTest extends BaseCardTest {

    @Test
    void createsMatchingTokenForTrampleDamage() {
        harness.addToBattlefield(player1, new QuartzwoodCrasher());
        addAttacker(3, true);

        resolveCombat();
        resolveAllTriggers();

        List<Permanent> tokens = findPermanents(player1, "Dinosaur Beast");
        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(token.getCard().getPower()).isEqualTo(3);
        assertThat(token.getCard().getToughness()).isEqualTo(3);
        assertThat(token.getCard().getColor()).isEqualTo(com.github.laxika.magicalvibes.model.CardColor.GREEN);
        assertThat(token.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.DINOSAUR, CardSubtype.BEAST);
        assertThat(token.getCard().getKeywords()).contains(Keyword.TRAMPLE);
    }

    @Test
    void combinesDamageFromAllMatchingCreaturesAndIgnoresNonTrampleDamage() {
        harness.addToBattlefield(player1, new QuartzwoodCrasher());
        addAttacker(2, true);
        addAttacker(3, true);
        addAttacker(4, false);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Dinosaur Beast")).hasSize(1);
        Permanent token = findPermanent(player1, "Dinosaur Beast");
        assertThat(token.getCard().getPower()).isEqualTo(5);
        assertThat(token.getCard().getToughness()).isEqualTo(5);
    }

    @Test
    void doesNotTriggerForCombatDamageWithoutTrample() {
        harness.addToBattlefield(player1, new QuartzwoodCrasher());
        addAttacker(3, false);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Dinosaur Beast")).isEmpty();
    }

    @Test
    void triggersForItsOwnCombatDamage() {
        addCreatureReady(player1, new QuartzwoodCrasher());

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Dinosaur Beast")).hasSize(1);
        Permanent token = findPermanent(player1, "Dinosaur Beast");
        assertThat(token.getCard().getPower()).isEqualTo(6);
        assertThat(token.getCard().getToughness()).isEqualTo(6);
    }

    @Test
    void countsOnlyDamageThatTramplesOverTheBlocker() {
        addCreatureReady(player1, new QuartzwoodCrasher());
        Permanent blocker = addCreatureReady(player2, new MosscoatGoriak());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 4, player2.getId(), 2));
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        assertThat(findPermanents(player1, "Dinosaur Beast")).hasSize(1);
        Permanent token = findPermanent(player1, "Dinosaur Beast");
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    void stillTriggersWhenItDiesDuringTheSameCombatDamageEvent() {
        addCreatureReady(player1, new QuartzwoodCrasher());
        addCreatureReady(player1, new AlmightyBrushwagg());
        Permanent blocker = addCreatureReady(player2, new QuartzwoodCrasher());

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 6));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Quartzwood Crasher");
        harness.assertLife(player2, 19);
        assertThat(findPermanents(player1, "Dinosaur Beast")).hasSize(1);
        Permanent token = findPermanent(player1, "Dinosaur Beast");
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(findPermanents(player2, "Dinosaur Beast")).isEmpty();
    }

    private void addAttacker(int power, boolean trample) {
        Card card = new GrizzlyBears();
        card.setPower(power);
        card.setKeywords(trample ? EnumSet.of(Keyword.TRAMPLE) : EnumSet.noneOf(Keyword.class));
        Permanent attacker = addCreatureReady(player1, card);
        attacker.setAttacking(true);
    }
}
