package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VictorysHeraldTest extends BaseCardTest {

    // ===== Attack trigger: granting keywords =====

    @Test
    @DisplayName("Attacking with Victory's Herald grants flying and lifelink to all attacking creatures")
    void attackGrantsFlyingAndLifelinkToAllAttackers() {
        Permanent herald = new Permanent(new VictorysHerald());
        herald.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(herald);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(0, 1));

        // Resolve the triggered ability (GrantKeywordEffect triggers go on stack)
        harness.passBothPriorities();

        // Bears should now have flying and lifelink
        assertThat(bears.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(bears.hasKeyword(Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Victory's Herald itself gains lifelink when attacking (already has flying)")
    void heraldGainsLifelinkWhenAttacking() {
        Permanent herald = new Permanent(new VictorysHerald());
        herald.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(herald);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(0));

        // Resolve triggered ability
        harness.passBothPriorities();

        // Herald should have lifelink granted (flying is innate from Scryfall)
        assertThat(herald.hasKeyword(Keyword.LIFELINK)).isTrue();
        assertThat(herald.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Non-attacking creatures do not gain flying or lifelink")
    void nonAttackingCreaturesDoNotGainKeywords() {
        Permanent herald = new Permanent(new VictorysHerald());
        herald.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(herald);

        Permanent stayBack = new Permanent(new GrizzlyBears());
        stayBack.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(stayBack);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        // Only declare herald as attacker (index 0), bears stays back
        gs.declareAttackers(gd, player1, List.of(0));

        // Resolve triggered ability
        harness.passBothPriorities();

        // Bears that didn't attack should NOT gain flying or lifelink
        assertThat(stayBack.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(stayBack.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Attack trigger puts triggered ability on the stack")
    void attackPutsTriggeredAbilityOnStack() {
        Permanent herald = new Permanent(new VictorysHerald());
        herald.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(herald);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(0));

        // Triggered ability should be on the stack
        assertThat(gd.stack).isNotEmpty();
        assertThat(gd.stack.stream()
                .anyMatch(entry -> entry.getCard().getName().equals("Victory's Herald")))
                .isTrue();
    }
}
