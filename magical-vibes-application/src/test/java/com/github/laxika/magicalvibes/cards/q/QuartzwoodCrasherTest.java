package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({QuartzwoodCrasher.class, GrizzlyBears.class})
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

    private Permanent addAttacker(int power, boolean trample) {
        Card card = new GrizzlyBears();
        card.setPower(power);
        card.setKeywords(trample ? EnumSet.of(Keyword.TRAMPLE) : EnumSet.noneOf(Keyword.class));
        Permanent attacker = new Permanent(card);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);
        return attacker;
    }
}
