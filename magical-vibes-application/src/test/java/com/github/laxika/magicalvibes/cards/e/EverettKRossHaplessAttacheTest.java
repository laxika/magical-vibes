package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EverettKRossHaplessAttache.class, GrizzlyBears.class, Forest.class})
class EverettKRossHaplessAttacheTest extends BaseCardTest {

    @Test
    void boostsCommanderCreaturesOnly() {
        EverettKRossHaplessAttache card = new EverettKRossHaplessAttache();
        gd.makeCommander(player1.getId(), card);
        Permanent commander = addCreatureReady(player1, card);
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, commander)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, commander)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, commander, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void staticBonusReflectsCommanderDesignationChanges() {
        EverettKRossHaplessAttache card = new EverettKRossHaplessAttache();
        Permanent permanent = addCreatureReady(player1, card);

        assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(1);
        gd.makeCommander(player1.getId(), card);
        assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, permanent, Keyword.LIFELINK)).isTrue();
    }

    @Test
    void drawsWhenOpponentAttacksWithTwoCreatures() {
        harness.addToBattlefield(player1, new EverettKRossHaplessAttache());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(player2, List.of(0, 1));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void doesNotBoostOpponentsCommander() {
        harness.addToBattlefield(player1, new EverettKRossHaplessAttache());
        EverettKRossHaplessAttache opponentCard = new EverettKRossHaplessAttache();
        gd.makeCommander(player2.getId(), opponentCard);
        Permanent opponentCommander = addCreatureReady(player2, opponentCard);

        assertThat(gqs.getEffectivePower(gd, opponentCommander)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentCommander)).isEqualTo(4);
    }

    @Test
    void commanderGainsLifeFromCombatDamage() {
        EverettKRossHaplessAttache card = new EverettKRossHaplessAttache();
        gd.makeCommander(player1.getId(), card);
        addCreatureReady(player1, card);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        harness.resolveCombatDamage();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    void doesNotDrawWhenOpponentAttacksWithOnlyOneCreature() {
        harness.addToBattlefield(player1, new EverettKRossHaplessAttache());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotDrawWhenControllerAttacksWithTwoCreatures() {
        harness.addToBattlefield(player1, new EverettKRossHaplessAttache());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(player1, List.of(1, 2));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void drawsOnlyOneCardWhenOpponentAttacksWithThreeCreatures() {
        harness.addToBattlefield(player1, new EverettKRossHaplessAttache());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        declareAttackers(player2, List.of(0, 1, 2));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerLibraries.get(player1.getId())).hasSize(2);
    }
}
