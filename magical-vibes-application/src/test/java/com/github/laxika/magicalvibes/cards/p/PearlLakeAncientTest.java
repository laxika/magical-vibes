package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PearlLakeAncientTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast with flash and cannot be countered")
    void castsWithFlashAndCannotBeCountered() {
        PearlLakeAncient ancient = new PearlLakeAncient();
        harness.setHand(player1, List.of(ancient));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passPriority(player2);

        harness.castCreature(player1, 0);
        harness.castInstant(player2, 0, ancient.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(hasPermanentOfType(player1, PearlLakeAncient.class)).isTrue();
        assertThat(hasCardOfType(player1, PearlLakeAncient.class, "graveyard")).isFalse();
    }

    @Test
    @DisplayName("Prowess boosts it for each noncreature spell until end of turn")
    void prowessBoostsUntilEndOfTurn() {
        Permanent ancient = harness.addToBattlefieldAndReturn(player1, new PearlLakeAncient());
        int basePower = gqs.getEffectivePower(gd, ancient);
        int baseToughness = gqs.getEffectiveToughness(gd, ancient);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ancient)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, ancient)).isEqualTo(baseToughness + 1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ancient)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, ancient)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("Returns three controlled lands and itself to hand")
    void returnsThreeLandsAndItselfToHand() {
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        Permanent ancient = harness.addToBattlefieldAndReturn(player1, new PearlLakeAncient());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(ancient), null, null);
        harness.passBothPriorities();

        assertThat(countLands(player1)).isZero();
        assertThat(hasPermanentOfType(player1, PearlLakeAncient.class)).isFalse();
        assertThat(hasCardOfType(player1, PearlLakeAncient.class, "hand")).isTrue();
        assertThat(gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card.hasType(CardType.LAND))
                .count()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot activate without three lands it controls")
    void cannotActivateWithoutThreeControlledLands() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Island());
        Permanent ancient = harness.addToBattlefieldAndReturn(player1, new PearlLakeAncient());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(ancient),
                null,
                null
        )).isInstanceOf(IllegalStateException.class);

        assertThat(countLands(player1)).isEqualTo(2);
        assertThat(hasPermanentOfType(player1, PearlLakeAncient.class)).isTrue();
    }

    private long countLands(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().hasType(CardType.LAND))
                .count();
    }

    private boolean hasPermanentOfType(Player player, Class<?> cardType) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .anyMatch(permanent -> cardType.isInstance(permanent.getCard()));
    }

    private boolean hasCardOfType(Player player, Class<?> cardType, String zone) {
        List<?> cards = switch (zone) {
            case "hand" -> gd.playerHands.get(player.getId());
            case "graveyard" -> gd.playerGraveyards.get(player.getId());
            default -> throw new IllegalArgumentException("Unknown zone: " + zone);
        };
        return cards.stream().anyMatch(cardType::isInstance);
    }
}
