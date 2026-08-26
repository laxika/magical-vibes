package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OutlawsMerriment.class})
class OutlawsMerrimentTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one of the three token profiles at random on its controller's upkeep")
    void createsRandomTokenProfile() {
        harness.addToBattlefield(player1, new OutlawsMerriment());
        harness.setLife(player2, 100);

        Set<String> observedNames = new HashSet<>();
        for (int attempt = 0; attempt < 100 && observedNames.size() < 3; attempt++) {
            List<Permanent> before = tokens(player1);
            Set<UUID> beforeIds = before.stream().map(Permanent::getId).collect(Collectors.toSet());

            advanceToUpkeep(player1);
            harness.passBothPriorities();

            Permanent token = tokens(player1).stream()
                    .filter(permanent -> !beforeIds.contains(permanent.getId()))
                    .findFirst()
                    .orElseThrow();
            assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(
                    CardColor.RED, CardColor.WHITE);

            observedNames.add(token.getCard().getName());
            switch (token.getCard().getName()) {
                case "Human Warrior" -> {
                    assertThat(token.getCard().getPower()).isEqualTo(3);
                    assertThat(token.getCard().getToughness()).isEqualTo(1);
                    assertThat(token.getCard().getSubtypes())
                            .containsExactly(CardSubtype.HUMAN, CardSubtype.WARRIOR);
                    assertThat(token.getCard().getKeywords()).contains(Keyword.TRAMPLE, Keyword.HASTE);
                }
                case "Human Cleric" -> {
                    assertThat(token.getCard().getPower()).isEqualTo(2);
                    assertThat(token.getCard().getToughness()).isEqualTo(1);
                    assertThat(token.getCard().getSubtypes())
                            .containsExactly(CardSubtype.HUMAN, CardSubtype.CLERIC);
                    assertThat(token.getCard().getKeywords()).contains(Keyword.LIFELINK, Keyword.HASTE);
                }
                case "Human Rogue" -> {
                    assertThat(token.getCard().getPower()).isEqualTo(1);
                    assertThat(token.getCard().getToughness()).isEqualTo(2);
                    assertThat(token.getCard().getSubtypes())
                            .containsExactly(CardSubtype.HUMAN, CardSubtype.ROGUE);
                    assertThat(token.getCard().getKeywords()).contains(Keyword.HASTE);
                    assertThat(gd.interaction.activeInteraction())
                            .isInstanceOf(PendingInteraction.PermanentChoice.class);
                    harness.handlePermanentChosen(player1, player2.getId());
                    harness.passBothPriorities();
                    assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(100);
                }
                default -> throw new AssertionError("Unexpected token: " + token.getCard().getName());
            }
        }

        assertThat(observedNames).containsExactlyInAnyOrder("Human Warrior", "Human Cleric", "Human Rogue");
    }

    @Test
    @DisplayName("Triggers only during its controller's upkeep")
    void triggersOnlyDuringControllerUpkeep() {
        harness.addToBattlefield(player1, new OutlawsMerriment());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(tokens(player1)).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private List<Permanent> tokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }
}
