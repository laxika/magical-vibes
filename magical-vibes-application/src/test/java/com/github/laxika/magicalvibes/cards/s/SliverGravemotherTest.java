package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BonescytheSliver;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SliverGravemother.class, BonescytheSliver.class, GrizzlyBears.class})
@DisplayName("Sliver Gravemother")
class SliverGravemotherTest extends BaseCardTest {

    @Test
    @DisplayName("Slivers you control survive the legend rule")
    void sliverDuplicatesSurviveLegendRule() {
        harness.addToBattlefield(player1, new SliverGravemother());
        harness.addToBattlefield(player1, new SliverGravemother());

        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Sliver creature cards in your graveyard gain encore for their mana value")
    void grantsEncoreToSliversInGraveyard() {
        harness.addToBattlefield(player1, new SliverGravemother());
        harness.setGraveyard(player1, List.of(new BonescytheSliver()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent token = findPermanents(player1, "Bonescythe Sliver").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.isTapped()).isTrue();
        assertThat(token.isAttacking()).isTrue();
        assertThat(token.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
        harness.assertNotInGraveyard(player1, "Bonescythe Sliver");
    }

    @Test
    @DisplayName("Encore is not granted to non-Sliver creature cards")
    void doesNotGrantEncoreToNonSlivers() {
        harness.addToBattlefield(player1, new SliverGravemother());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
