package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed({AmphinMutineer.class, GrizzlyBears.class})
class AmphinMutineerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters exiling a non-Salamander creature and gives its controller a Salamander Warrior")
    void entersExilesCreatureAndCreatesSalamanderWarrior() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AmphinMutineer()));
        addManaForCast();

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(findPermanents(player2, "Salamander Warrior"))
                .anyMatch(token -> token.getCard().isToken()
                        && token.getCard().hasType(CardType.CREATURE)
                        && token.getCard().getColor() == CardColor.BLUE
                        && token.getCard().getPower() == 4
                        && token.getCard().getToughness() == 3
                        && token.getCard().getSubtypes().contains(CardSubtype.SALAMANDER)
                        && token.getCard().getSubtypes().contains(CardSubtype.WARRIOR));
    }

    @Test
    @DisplayName("Cannot target a Salamander creature")
    void cannotTargetSalamanderCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AmphinMutineer());
        harness.setHand(player1, List.of(new AmphinMutineer()));
        addManaForCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("non-Salamander creature");
    }

    @Test
    @DisplayName("Encore creates a hasty attacking token and sacrifices it at the next end step")
    void encoreCreatesAndSacrificesToken() {
        harness.setGraveyard(player1, List.of(new AmphinMutineer()));
        addManaForEncore();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Amphin Mutineer");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
        assertThat(token.isTapped()).isTrue();
        assertThat(token.isAttacking()).isTrue();
        assertThat(token.getAttackTarget()).isEqualTo(player2.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Amphin Mutineer")).isEmpty();
    }

    private void addManaForCast() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void addManaForEncore() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
