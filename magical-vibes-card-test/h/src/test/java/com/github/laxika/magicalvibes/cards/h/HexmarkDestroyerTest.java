package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.m.Mortify;
import com.github.laxika.magicalvibes.cards.s.SicarianInfiltrator;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HexmarkDestroyer.class, SicarianInfiltrator.class, Mortify.class, Humility.class})
class HexmarkDestroyerTest extends BaseCardTest {

    @Test
    @DisplayName("Hexmark Destroyer cannot be blocked by fewer than six creatures")
    void cannotBeBlockedByFewerThanSixCreatures() {
        addCreatureReady(player1, new HexmarkDestroyer());
        for (int i = 0; i < 5; i++) {
            addCreatureReady(player2, new SicarianInfiltrator());
        }

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0),
                new BlockerAssignment(3, 0),
                new BlockerAssignment(4, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("6 or more creatures");
    }

    @Test
    @DisplayName("Hexmark Destroyer can be blocked by six creatures")
    void canBeBlockedBySixCreatures() {
        addCreatureReady(player1, new HexmarkDestroyer());
        for (int i = 0; i < 6; i++) {
            addCreatureReady(player2, new SicarianInfiltrator());
        }

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0),
                new BlockerAssignment(3, 0),
                new BlockerAssignment(4, 0),
                new BlockerAssignment(5, 0)));

        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .filter(Permanent::isBlocking)
                .count()).isEqualTo(6);
    }

    @Test
    @DisplayName("Unearth returns Hexmark Destroyer with haste and exiles it at the next end step")
    void unearthReturnsWithHasteAndExilesAtEndStep() {
        harness.setGraveyard(player1, List.of(new HexmarkDestroyer()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent destroyer = findPermanent(player1, "Hexmark Destroyer");
        assertThat(destroyer.getGrantedKeywords()).contains(Keyword.HASTE);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Hexmark Destroyer");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(cardInExile -> cardInExile.getName().equals("Hexmark Destroyer"));
    }

    @Test
    void canBeBlockedByOneCreatureAfterLosingAbilities() {
        addCreatureReady(player1, new HexmarkDestroyer());
        addCreatureReady(player2, new SicarianInfiltrator());
        harness.addToBattlefield(player1, new Humility());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().isBlocking()).isTrue();
    }

    @Test
    void unearthCannotBeActivatedDuringCombat() {
        harness.setGraveyard(player1, List.of(new HexmarkDestroyer()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        harness.assertInGraveyard(player1, "Hexmark Destroyer");
    }

    @Test
    void unearthedDestroyerCanAttackImmediatelyAndRemainUnblocked() {
        harness.setGraveyard(player1, List.of(new HexmarkDestroyer()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        harness.assertLife(player2, 14);
    }

    @Test
    void unearthedDestroyerIsExiledInsteadOfGoingToGraveyardWhenDestroyed() {
        harness.setGraveyard(player1, List.of(new HexmarkDestroyer()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        Permanent destroyer = findPermanent(player1, "Hexmark Destroyer");
        harness.setHand(player1, List.of(new Mortify()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, destroyer.getId());

        harness.assertNotOnBattlefield(player1, "Hexmark Destroyer");
        harness.assertNotInGraveyard(player1, "Hexmark Destroyer");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Hexmark Destroyer"));
    }
}
