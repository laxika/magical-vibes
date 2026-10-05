package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.Colossapede;
import com.github.laxika.magicalvibes.cards.l.LuxaRiverShrine;
import com.github.laxika.magicalvibes.cards.s.ScaledBehemoth;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
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

@CardUsed({InitiatesCompanion.class, Forest.class, Colossapede.class,
        LuxaRiverShrine.class, ScaledBehemoth.class})
class InitiatesCompanionTest extends BaseCardTest {

    private Permanent addTappedPermanent(Player player, Card card) {
        harness.addToBattlefield(player, card);
        Permanent perm = findPermanent(player, card.getName());
        perm.tap();
        return perm;
    }

    @Test
    @DisplayName("Combat damage to a player prompts a choice of any creature or land, excluding other permanents")
    void promptsToChooseCreatureOrLand() {
        Permanent companion = addCreatureReady(player1, new InitiatesCompanion());
        companion.setAttacking(true);
        Permanent ownLand = addTappedPermanent(player1, new Forest());
        Permanent enemyCreature = addCreatureReady(player2, new Colossapede());
        Permanent enemyArtifact = addTappedPermanent(player2, new LuxaRiverShrine());

        resolveCombat();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(ownLand.getId(), enemyCreature.getId())
                .doesNotContain(enemyArtifact.getId());
    }

    @Test
    @DisplayName("The chosen creature is untapped and the game advances")
    void untapsChosenCreature() {
        Permanent companion = addCreatureReady(player1, new InitiatesCompanion());
        companion.setAttacking(true);
        Permanent tappedCreature = addCreatureReady(player1, new Colossapede());
        tappedCreature.tap();

        resolveCombat();
        harness.handlePermanentChosen(player1, tappedCreature.getId());
        assertThat(tappedCreature.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(tappedCreature.isTapped()).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }

    @Test
    @DisplayName("The chosen land is untapped")
    void untapsChosenLand() {
        Permanent companion = addCreatureReady(player1, new InitiatesCompanion());
        companion.setAttacking(true);
        Permanent tappedLand = addTappedPermanent(player1, new Forest());

        resolveCombat();
        harness.handlePermanentChosen(player1, tappedLand.getId());
        assertThat(tappedLand.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(tappedLand.isTapped()).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Opponent's hexproof creature cannot be targeted")
    void excludesOpponentsHexproofCreature() {
        Permanent companion = addCreatureReady(player1, new InitiatesCompanion());
        companion.setAttacking(true);
        Permanent enemyCreature = addTappedPermanent(player2, new ScaledBehemoth());
        Permanent ownCreature = addTappedPermanent(player1, new ScaledBehemoth());

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(ownCreature.getId(), companion.getId())
                .doesNotContain(enemyCreature.getId());
    }

    @Test
    @DisplayName("Can untap itself after dealing combat damage")
    void untapsItself() {
        Permanent companion = addCreatureReady(player1, new InitiatesCompanion());
        companion.tap();
        companion.setAttacking(true);

        resolveCombat();
        harness.handlePermanentChosen(player1, companion.getId());
        harness.passBothPriorities();

        assertThat(companion.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can untap a land controlled by the opponent")
    void untapsOpponentsLand() {
        Permanent companion = addCreatureReady(player1, new InitiatesCompanion());
        companion.setAttacking(true);
        Permanent land = addTappedPermanent(player2, new Forest());

        resolveCombat();
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        assertThat(land.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not trigger when combat damage is dealt only to a blocker")
    void doesNotTriggerWhenBlocked() {
        addCreatureReady(player1, new InitiatesCompanion());
        addCreatureReady(player2, new Colossapede());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
