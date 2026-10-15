package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RubblebeltMaverick;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ToxinAnalysis.class, Forest.class, RubblebeltMaverick.class})
class ToxinAnalysisTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gains deathtouch and lifelink and you investigate")
    void grantsKeywordsAndCreatesClue() {
        harness.addToBattlefield(player1, new RubblebeltMaverick());
        harness.setHand(player1, List.of(new ToxinAnalysis()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Rubblebelt Maverick"));
        harness.passBothPriorities();

        Permanent maverick = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(maverick.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
        assertThat(maverick.hasKeyword(Keyword.LIFELINK)).isTrue();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Granted keywords wear off at end of turn while the Clue remains")
    void keywordsWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new RubblebeltMaverick());
        harness.setHand(player1, List.of(new ToxinAnalysis()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID maverickId = harness.getPermanentId(player1, "Rubblebelt Maverick");
        harness.castInstant(player1, 0, maverickId);
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        Permanent maverick = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(maverick.hasKeyword(Keyword.DEATHTOUCH)).isFalse();
        assertThat(maverick.hasKeyword(Keyword.LIFELINK)).isFalse();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new ToxinAnalysis()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID forestId = harness.getPermanentId(player1, "Forest");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, forestId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canTargetOpponentsCreatureButCasterInvestigates() {
        harness.addToBattlefield(player2, new RubblebeltMaverick());
        harness.setHand(player1, List.of(new ToxinAnalysis()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Rubblebelt Maverick"));
        harness.passBothPriorities();

        Permanent target = findPermanent(player2, "Rubblebelt Maverick");
        assertThat(target.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
        assertThat(target.hasKeyword(Keyword.LIFELINK)).isTrue();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player2, "Clue")).isEmpty();
    }

    @Test
    void doesNotInvestigateWhenOnlyTargetLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RubblebeltMaverick());
        harness.setHand(player1, List.of(new ToxinAnalysis()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof ToxinAnalysis);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void clueCanBeSacrificedForTwoManaToDrawCard() {
        harness.addToBattlefield(player1, new RubblebeltMaverick());
        harness.setHand(player1, List.of(new ToxinAnalysis()));
        Forest drawnCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Rubblebelt Maverick"));
        harness.passBothPriorities();

        Permanent clue = findPermanent(player1, "Clue");
        int clueIndex = gd.playerBattlefields.get(player1.getId()).indexOf(clue);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, clueIndex, null, null);

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }
}
