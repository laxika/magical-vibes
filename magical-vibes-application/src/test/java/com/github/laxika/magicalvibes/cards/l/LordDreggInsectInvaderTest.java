package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LordDreggInsectInvader.class, GrizzlyBears.class})
class LordDreggInsectInvaderTest extends BaseCardTest {

    @Test
    void createsFlyingInsectWarriorAfterControlledPermanentLeaves() {
        harness.addToBattlefield(player1, new LordDreggInsectInvader());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, bears));
        advanceToEndStep(player1);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Insect Warrior");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.INSECT, CardSubtype.WARRIOR);
        assertThat(token.getEffectivePower()).isEqualTo(1);
        assertThat(token.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
    }

    @Test
    void doesNotCreateTokenWithoutPermanentLeaving() {
        harness.addToBattlefield(player1, new LordDreggInsectInvader());

        advanceToEndStep(player1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void opponentPermanentLeavingDoesNotCreateToken() {
        harness.addToBattlefield(player1, new LordDreggInsectInvader());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, bears));
        advanceToEndStep(player1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void sacrificesTokenToDrawCard() {
        harness.addToBattlefield(player1, new LordDreggInsectInvader());
        Permanent token = harness.addToBattlefieldAndReturn(player1, createToken());
        harness.setHand(player1, new ArrayList<>());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    void cannotActivateWithoutToken() {
        harness.addToBattlefield(player1, new LordDreggInsectInvader());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Card createToken() {
        Card token = new Card() {
        };
        token.setName("Token");
        token.setType(CardType.CREATURE);
        token.setPower(1);
        token.setToughness(1);
        token.setToken(true);
        return token;
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
