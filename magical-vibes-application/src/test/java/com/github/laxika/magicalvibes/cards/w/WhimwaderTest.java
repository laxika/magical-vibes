package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WhimwaderTest extends BaseCardTest {

    private Permanent addWhimwaderReadyToAttack() {
        Permanent perm = new Permanent(new Whimwader());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        return perm;
    }

    private void beginAttackDeclaration() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

    /** A clearly non-blue permanent: the loader treats basic lands as their mana color, so use a white creature. */
    private void addNonBluePermanentTo(com.github.laxika.magicalvibes.model.Player owner) {
        Card white = new Card();
        white.setName("Test White Bear");
        white.setType(CardType.CREATURE);
        white.setColor(CardColor.WHITE);
        white.setColors(List.of(CardColor.WHITE));
        gd.playerBattlefields.get(owner.getId()).add(new Permanent(white));
    }

    @Test
    @DisplayName("Whimwader can attack when defending player controls a blue permanent")
    void canAttackWhenDefenderControlsBluePermanent() {
        harness.addToBattlefield(player2, new FugitiveWizard());
        addWhimwaderReadyToAttack();

        beginAttackDeclaration();

        assertThatCode(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .doesNotThrowAnyException();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Whimwader cannot attack when defending player controls only a non-blue permanent")
    void cannotAttackWhenDefenderControlsOnlyNonBluePermanent() {
        addNonBluePermanentTo(player2);
        addWhimwaderReadyToAttack();

        beginAttackDeclaration();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Whimwader cannot attack when defending player controls no permanents")
    void cannotAttackWhenDefenderControlsNothing() {
        addWhimwaderReadyToAttack();

        beginAttackDeclaration();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }
}
