package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OathOfMages.class})
class OathOfMagesTest extends BaseCardTest {

    @Test
    @DisplayName("The active player chooses a higher-life opponent and may deal 1 damage to them")
    void activePlayerMayDealDamageToChosenOpponent() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 11);
        harness.addToBattlefield(player1, new OathOfMages());

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 10);
    }

    @Test
    @DisplayName("Declining the may ability deals no damage")
    void decliningDealsNoDamage() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 11);
        harness.addToBattlefield(player1, new OathOfMages());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 11);
    }

    @Test
    @DisplayName("The active player chooses the target on an opponent's upkeep")
    void activePlayerChoosesTargetOnOpponentUpkeep() {
        harness.setLife(player1, 11);
        harness.setLife(player2, 10);
        harness.addToBattlefield(player1, new OathOfMages());

        advanceToUpkeep(player2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player1.getId());
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 10);
    }

    @Test
    @DisplayName("The ability does not trigger when no opponent has more life")
    void doesNotTriggerWithoutHigherLifeOpponent() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);
        harness.addToBattlefield(player1, new OathOfMages());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The target must still have more life when the ability resolves")
    void targetMustStillHaveMoreLifeWhenAbilityResolves() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 11);
        harness.addToBattlefield(player1, new OathOfMages());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.setLife(player2, 10);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertLife(player1, 10);
        harness.assertLife(player2, 10);
    }
}
