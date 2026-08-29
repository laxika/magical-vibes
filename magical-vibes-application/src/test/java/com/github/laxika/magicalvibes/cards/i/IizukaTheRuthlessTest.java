package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IizukaTheRuthlessTest extends BaseCardTest {

    @Test
    @DisplayName("Bushido 2 triggers when Iizuka blocks")
    void bushidoTriggersWhenBlocking() {
        Permanent attacker = addReadyCreature(player1, creature("Attacker", CardSubtype.SOLDIER));
        attacker.setAttacking(true);
        Permanent iizuka = addReadyCreature(player2, new IizukaTheRuthless());

        prepareDeclareBlockers();
        declareBlocker(player2, 0, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, iizuka)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, iizuka)).isEqualTo(5);
    }

    @Test
    @DisplayName("Sacrificing a Samurai grants double strike to own Samurai until end of turn")
    void sacrificeSamuraiGrantsDoubleStrikeUntilEndOfTurn() {
        Permanent iizuka = addReadyCreature(player1, new IizukaTheRuthless());
        Permanent samurai = addReadyCreature(player1, creature("Samurai", CardSubtype.SAMURAI));
        Permanent nonSamurai = addReadyCreature(player1, creature("Soldier", CardSubtype.SOLDIER));
        Permanent opponentSamurai = addReadyCreature(player2, creature("Opponent Samurai", CardSubtype.SAMURAI));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(player1, iizuka), null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, samurai.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, iizuka, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonSamurai, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentSamurai, Keyword.DOUBLE_STRIKE)).isFalse();
        harness.assertInGraveyard(player1, "Samurai");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, iizuka, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

    private Card creature(String name, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.WHITE);
        card.setPower(2);
        card.setToughness(2);
        card.setSubtypes(List.of(subtype));
        return card;
    }

    private void declareBlocker(Player defendingPlayer, int blockerIndex, int attackerIndex) {
        gs.declareBlockers(gd, defendingPlayer, List.of(new com.github.laxika.magicalvibes.networking.message.BlockerAssignment(blockerIndex, attackerIndex)));
    }
}
