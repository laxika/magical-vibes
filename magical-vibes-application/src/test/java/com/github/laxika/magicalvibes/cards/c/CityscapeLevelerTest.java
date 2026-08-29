package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CityscapeLevelerTest extends BaseCardTest {

    @Test
    @DisplayName("Cast trigger destroys a nonland permanent and gives its controller a tapped Powerstone")
    void castTriggerDestroysNonlandPermanentAndCreatesPowerstone() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CityscapeLeveler()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(findPermanents(player2, "Powerstone")).singleElement().satisfies(powerstone -> {
            assertThat(powerstone.getCard().getType()).isEqualTo(CardType.ARTIFACT);
            assertThat(powerstone.getCard().getSubtypes()).containsExactly(CardSubtype.POWERSTONE);
            assertThat(powerstone.isTapped()).isTrue();
        });
    }

    @Test
    @DisplayName("Attack trigger destroys a nonland permanent and gives its controller a tapped Powerstone")
    void attackTriggerDestroysNonlandPermanentAndCreatesPowerstone() {
        Permanent leveler = addCreatureReady(player1, new CityscapeLeveler());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(leveler)));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(findPermanents(player2, "Powerstone")).hasSize(1);
    }

    @Test
    @DisplayName("Cast trigger can resolve without choosing a target")
    void castTriggerCanResolveWithoutTarget() {
        harness.setHand(player1, List.of(new CityscapeLeveler()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Cityscape Leveler");
        assertThat(findPermanents(player1, "Powerstone")).isEmpty();
    }

    @Test
    @DisplayName("Cast trigger cannot target a land")
    void castTriggerCannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new CityscapeLeveler()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland permanent");
    }

    @Test
    @DisplayName("Unearth returns Cityscape Leveler with haste and exiles it at the next end step")
    void unearthReturnsWithHasteAndExilesAtEndStep() {
        harness.setGraveyard(player1, List.of(new CityscapeLeveler()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent leveler = findPermanent(player1, "Cityscape Leveler");
        assertThat(leveler.getGrantedKeywords()).contains(Keyword.HASTE);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Cityscape Leveler");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(cardInExile -> cardInExile.getName().equals("Cityscape Leveler"));
    }
}
