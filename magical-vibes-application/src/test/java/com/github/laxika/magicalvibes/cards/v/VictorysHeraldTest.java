package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VictorysHerald.class, GrizzlyBears.class})
class VictorysHeraldTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with Victory's Herald grants flying and lifelink to all attacking creatures")
    void attackGrantsFlyingAndLifelinkToAllAttackers() {
        addCreatureReady(player1, new VictorysHerald());

        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));

        // Resolve the triggered ability (GrantKeywordEffect triggers go on stack)
        harness.passBothPriorities();

        // Bears should now have flying and lifelink
        assertThat(bears.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(bears.hasKeyword(Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Victory's Herald itself gains lifelink when attacking (already has flying)")
    void heraldGainsLifelinkWhenAttacking() {
        Permanent herald = addCreatureReady(player1, new VictorysHerald());

        declareAttackers(List.of(0));

        // Resolve triggered ability
        harness.passBothPriorities();

        // Herald should have lifelink granted (flying is innate from Scryfall)
        assertThat(herald.hasKeyword(Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Non-attacking creatures do not gain flying or lifelink")
    void nonAttackingCreaturesDoNotGainKeywords() {
        addCreatureReady(player1, new VictorysHerald());

        Permanent stayBack = addCreatureReady(player1, new GrizzlyBears());

        // Only declare herald as attacker (index 0), bears stays back
        declareAttackers(List.of(0));

        // Resolve triggered ability
        harness.passBothPriorities();

        // Bears that didn't attack should NOT gain flying or lifelink
        assertThat(stayBack.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(stayBack.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Attack trigger puts triggered ability on the stack")
    void attackPutsTriggeredAbilityOnStack() {
        addCreatureReady(player1, new VictorysHerald());

        declareAttackers(List.of(0));

        // Triggered ability should be on the stack
        assertThat(gd.stack).isNotEmpty();
        assertThat(gd.stack.stream()
                .anyMatch(entry -> entry.getCard().getName().equals("Victory's Herald")))
                .isTrue();
    }

    @Test
    @DisplayName("Other creatures attacking without the Herald do not trigger the ability")
    void heraldMustAttackToGrantKeywords() {
        addCreatureReady(player1, new VictorysHerald());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));

        assertThat(gd.stack).isEmpty();
        assertThat(bears.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(bears.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Creatures removed from combat before resolution do not gain the abilities")
    void checksAttackingStatusAtResolution() {
        Permanent herald = addCreatureReady(player1, new VictorysHerald());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        declareAttackers(List.of(0, 1));
        assertThat(gd.stack).hasSize(1);
        assertThat(bears.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(bears.hasKeyword(Keyword.LIFELINK)).isFalse();
        bears.setAttacking(false);

        harness.passBothPriorities();

        assertThat(herald.hasKeyword(Keyword.LIFELINK)).isTrue();
        assertThat(bears.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(bears.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("The trigger grants abilities even if the Herald leaves before resolution")
    void triggerResolvesWithoutSourceOnBattlefield() {
        Permanent herald = addCreatureReady(player1, new VictorysHerald());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        declareAttackers(List.of(0, 1));
        gd.playerBattlefields.get(player1.getId()).remove(herald);
        gd.playerGraveyards.get(player1.getId()).add(herald.getCard());

        harness.passBothPriorities();

        assertThat(bears.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(bears.hasKeyword(Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Granted abilities persist after combat and expire at end of turn")
    void grantsLastUntilEndOfTurn() {
        Permanent herald = addCreatureReady(player1, new VictorysHerald());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertLife(player1, 26);
        harness.assertLife(player2, 14);
        assertThat(bears.isAttacking()).isFalse();
        assertThat(bears.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(bears.hasKeyword(Keyword.LIFELINK)).isTrue();
        assertThat(herald.hasKeyword(Keyword.LIFELINK)).isTrue();

        harness.forceStep(TurnStep.CLEANUP);
        harness.passUntil(TurnStep.UNTAP);

        assertThat(bears.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(bears.hasKeyword(Keyword.LIFELINK)).isFalse();
        assertThat(herald.hasKeyword(Keyword.LIFELINK)).isFalse();
    }
}
