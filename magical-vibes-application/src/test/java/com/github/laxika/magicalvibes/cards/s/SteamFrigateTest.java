package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SteamFrigateTest extends BaseCardTest {

    // ===== Attack restriction =====

    @Test
    @DisplayName("Steam Frigate can attack when defending player controls an Island")
    void canAttackWhenDefenderControlsIsland() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new Island());

        Permanent frigate = new Permanent(new SteamFrigate());
        frigate.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(frigate);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        // Combat auto-advances; verify attack went through by checking damage dealt (3/3)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Steam Frigate cannot attack when defending player does not control an Island")
    void cannotAttackWhenDefenderDoesNotControlIsland() {
        Permanent frigate = new Permanent(new SteamFrigate());
        frigate.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(frigate);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Steam Frigate cannot attack if defender controls only a changeling creature")
    void cannotAttackWhenDefenderOnlyControlsChangelingCreature() {
        Card changeling = new Card();
        changeling.setName("Test Changeling");
        changeling.setType(CardType.CREATURE);
        changeling.setSubtypes(List.of(CardSubtype.SHAPESHIFTER));
        changeling.setKeywords(Set.of(Keyword.CHANGELING));
        gd.playerBattlefields.get(player2.getId()).add(new Permanent(changeling));

        Permanent frigate = new Permanent(new SteamFrigate());
        frigate.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(frigate);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }
}
