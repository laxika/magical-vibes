package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.FaithlessLooting;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HashepOasis;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DesertWarfare.class, Desert.class, HashepOasis.class, AirElemental.class,
        Millstone.class, FaithlessLooting.class, Forest.class})
class DesertWarfareTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one hasty multicolored Sand Warrior for each Desert when you control five")
    void createsSandWarriorsForFiveDeserts() {
        harness.addToBattlefield(player1, new DesertWarfare());
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new Desert());
        }

        advanceToCombat(player1);
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Sand Warrior");
        assertThat(tokens).hasSize(5);
        for (Permanent token : tokens) {
            assertThat(token.getCard().getColors())
                    .containsExactlyInAnyOrder(CardColor.RED, CardColor.GREEN, CardColor.WHITE);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.WARRIOR);
            assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
        }
    }

    @Test
    @DisplayName("Does not create Sand Warriors with fewer than five Deserts")
    void doesNotCreateSandWarriorsWithFourDeserts() {
        harness.addToBattlefield(player1, new DesertWarfare());
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new Desert());
        }

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Sand Warrior")).isEmpty();
    }

    @Test
    @DisplayName("Returns a sacrificed Desert under your control at the next end step")
    void returnsSacrificedDesertAtNextEndStep() {
        harness.addToBattlefield(player1, new DesertWarfare());
        Permanent oasis = harness.addToBattlefieldAndReturn(player1, new HashepOasis());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(oasis),
                2, null, target.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(oasis.getCard().getId()));

        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(oasis.getCard().getId()));
    }

    @Test
    @DisplayName("Returns a Desert discarded from hand at the next end step")
    void returnsDiscardedDesertAtNextEndStep() {
        harness.addToBattlefield(player1, new DesertWarfare());
        harness.setHand(player1, List.of(new FaithlessLooting(), new Desert(), new Forest()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Desert"));
    }

    @Test
    @DisplayName("Returns a Desert milled from the library at the next end step")
    void returnsMilledDesertAtNextEndStep() {
        harness.addToBattlefield(player1, new DesertWarfare());
        Permanent millstone = harness.addToBattlefieldAndReturn(player1, new Millstone());
        millstone.setSummoningSick(false);
        harness.setLibrary(player1, List.of(new Desert(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(millstone),
                0, null, player1.getId());
        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Desert"));
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
