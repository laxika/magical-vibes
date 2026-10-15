package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DragonSniper;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StarryEyedSkyrider.class, DragonSniper.class})
class StarryEyedSkyriderTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking grants flying to another creature you control until end of turn")
    void grantsFlyingToAnotherCreatureYouControl() {
        addCreatureReady(player1, new StarryEyedSkyrider());
        Permanent otherCreature = addCreatureReady(player1, new DragonSniper());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, otherCreature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Attacking tokens you control have flying")
    void attackingTokensYouControlHaveFlying() {
        addCreatureReady(player1, new StarryEyedSkyrider());
        Permanent ownToken = addTokenCreature(player1);
        Permanent opponentToken = addTokenCreature(player2);
        ownToken.setAttacking(true);
        ownToken.setAttackTarget(player2.getId());
        opponentToken.setAttacking(true);
        opponentToken.setAttackTarget(player1.getId());

        assertThat(gqs.hasKeyword(gd, ownToken, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentToken, Keyword.FLYING)).isFalse();

        ownToken.setAttacking(false);
        assertThat(gqs.hasKeyword(gd, ownToken, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Cannot target itself")
    void cannotTargetItself() {
        Permanent skyrider = addCreatureReady(player1, new StarryEyedSkyrider());
        addCreatureReady(player1, new DragonSniper());

        declareAttackers(List.of(0, 1));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, skyrider.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The attack trigger can target a nonattacking creature and expires at end of turn")
    void nonattackingCreatureGainsFlyingUntilEndOfTurn() {
        addCreatureReady(player1, new StarryEyedSkyrider());
        Permanent target = addCreatureReady(player1, new DragonSniper());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentsCreature() {
        addCreatureReady(player1, new StarryEyedSkyrider());
        addCreatureReady(player1, new DragonSniper());
        Permanent opponentCreature = addCreatureReady(player2, new DragonSniper());

        declareAttackers(List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A target that changes controllers before resolution does not gain flying")
    void targetMustStillBeControlledByYouAtResolution() {
        addCreatureReady(player1, new StarryEyedSkyrider());
        Permanent target = addCreatureReady(player1, new DragonSniper());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The attack trigger resolves even if Skyrider leaves the battlefield")
    void attackTriggerSurvivesSourceLeaving() {
        Permanent skyrider = addCreatureReady(player1, new StarryEyedSkyrider());
        Permanent target = addCreatureReady(player1, new DragonSniper());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(skyrider);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Only attacking tokens gain the static flying bonus, which ends when Skyrider leaves")
    void staticBonusRequiresAttackingTokenAndSourceOnBattlefield() {
        Permanent skyrider = addCreatureReady(player1, new StarryEyedSkyrider());
        Permanent token = addTokenCreature(player1);
        Permanent nontoken = addCreatureReady(player1, new DragonSniper());

        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isFalse();
        token.setAttacking(true);
        token.setAttackTarget(player2.getId());
        nontoken.setAttacking(true);
        nontoken.setAttackTarget(player2.getId());

        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, nontoken, Keyword.FLYING)).isFalse();

        gd.playerBattlefields.get(player1.getId()).remove(skyrider);

        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isFalse();
    }

    private Permanent addTokenCreature(Player player) {
        Card tokenCard = new DragonSniper();
        tokenCard.setToken(true);
        return addCreatureReady(player, tokenCard);
    }
}
