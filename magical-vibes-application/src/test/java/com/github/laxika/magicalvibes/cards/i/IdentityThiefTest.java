package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GoblinAssailant;
import com.github.laxika.magicalvibes.cards.g.GoblinKing;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IdentityThief.class, GoblinAssailant.class, GoblinKing.class})
class IdentityThiefTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking offers another nontoken creature as a target")
    void attackOffersMayChoice() {
        addReadyCreature(player1, new IdentityThief());
        Permanent goblin = addReadyCreature(player1, new GoblinAssailant());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, goblin.getId());

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting exiles the target, copies it, and returns it at the next end step")
    void acceptsExileAndCopy() {
        Permanent thief = addReadyCreature(player1, new IdentityThief());
        Permanent goblinKing = addReadyCreature(player1, new GoblinKing());
        Permanent goblin = addReadyCreature(player1, new GoblinAssailant());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, goblinKing.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, goblin, Keyword.MOUNTAINWALK)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(goblinKing);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(goblinKing.getOriginalCard());

        advanceToEndStep();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(thief);
        assertThat(gqs.hasKeyword(gd, thief, Keyword.MOUNTAINWALK)).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(permanent -> permanent == goblinKing);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining leaves the target and Identity Thief unchanged")
    void declinesExile() {
        addReadyCreature(player1, new IdentityThief());
        Permanent goblin = addReadyCreature(player1, new GoblinAssailant());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, goblin.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(goblin);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Token creatures are not legal targets")
    void tokenIsNotLegalTarget() {
        addReadyCreature(player1, new IdentityThief());
        Card tokenCard = new GoblinAssailant();
        tokenCard.setToken(true);
        addReadyCreature(player1, tokenCard);

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
