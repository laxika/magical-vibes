package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.model.CounterType;

class FalkenrathAristocratTest extends BaseCardTest {

    // ===== Ability structure =====

    @Test
    @DisplayName("Falkenrath Aristocrat has two activated abilities")
    void hasTwoActivatedAbilities() {
        FalkenrathAristocrat card = new FalkenrathAristocrat();
        assertThat(card.getActivatedAbilities()).hasSize(2);
    }

    

    

    // ===== Sacrifice a Human: indestructible + counter =====

    @Test
    @DisplayName("Sacrificing a Human grants indestructible and a +1/+1 counter")
    void sacrificeHumanGivesIndestructibleAndCounter() {
        Permanent aristocrat = addCreatureReady(player1, new FalkenrathAristocrat());
        harness.addToBattlefield(player1, createHumanToken());

        // Ability 0 = sacrifice a Human; only 1 Human → auto-sacrifice
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Human Soldier"));

        assertThat(aristocrat.getGrantedKeywords()).contains(Keyword.INDESTRUCTIBLE);
        assertThat(aristocrat.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    // ===== Sacrifice a non-Human creature: indestructible, no counter =====

    @Test
    @DisplayName("Sacrificing a non-Human creature grants indestructible but no counter")
    void sacrificeNonHumanGivesIndestructibleNoCounter() {
        Permanent aristocrat = addCreatureReady(player1, new FalkenrathAristocrat());
        harness.addToBattlefield(player1, new GrizzlyBears());

        // Ability 1 = sacrifice a non-Human creature; only the Bears qualifies → auto-sacrifice
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        assertThat(aristocrat.getGrantedKeywords()).contains(Keyword.INDESTRUCTIBLE);
        assertThat(aristocrat.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    // ===== A Human cannot be fed to the no-counter ability =====

    @Test
    @DisplayName("The non-Human ability cannot sacrifice a Human")
    void nonHumanAbilityCannotSacrificeHuman() {
        addCreatureReady(player1, new FalkenrathAristocrat());
        harness.addToBattlefield(player1, createHumanToken());

        // Only creature available is a Human, which ability 1 may not sacrifice
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Cannot sacrifice itself to the non-Human ability =====

    @Test
    @DisplayName("Cannot activate the non-Human ability with no other creature to sacrifice")
    void cannotSacrificeSelfToNonHumanAbility() {
        addCreatureReady(player1, new FalkenrathAristocrat());

        // No sacrifice fodder other than the source itself → ability cannot be paid
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== No Human available =====

    @Test
    @DisplayName("Human-sacrifice ability cannot be activated without a Human")
    void humanAbilityRequiresHuman() {
        addCreatureReady(player1, new FalkenrathAristocrat());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Indestructible wears off at end of turn =====

    @Test
    @DisplayName("Granted indestructible is removed at end of turn")
    void indestructibleResetsAtEndOfTurn() {
        Permanent aristocrat = addCreatureReady(player1, new FalkenrathAristocrat());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(aristocrat.getGrantedKeywords()).contains(Keyword.INDESTRUCTIBLE);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(aristocrat.getGrantedKeywords()).doesNotContain(Keyword.INDESTRUCTIBLE);
    }

    // ===== Helper methods =====

    private Card createHumanToken() {
        Card card = new Card();
        card.setName("Human Soldier");
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.WHITE);
        card.setPower(1);
        card.setToughness(1);
        card.setSubtypes(List.of(CardSubtype.HUMAN, CardSubtype.SOLDIER));
        return card;
    }
}
