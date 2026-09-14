package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.p.PlatedSpider;
import com.github.laxika.magicalvibes.cards.y.YavimayaHollow;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ApprenticeNecromancer.class, PlatedSpider.class, YavimayaHollow.class})
class ApprenticeNecromancerTest extends BaseCardTest {

    @Test
    @DisplayName("Reanimates a creature with haste and sacrifices it at the next end step")
    void reanimatesWithHasteAndSacrificesAtNextEndStep() {
        addCreatureReady(player1, new ApprenticeNecromancer());
        Card creature = new PlatedSpider();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        Permanent reanimated = findPermanent(player1, "Plated Spider");
        assertThat(reanimated.getGrantedKeywords()).contains(Keyword.HASTE);
        harness.assertNotInGraveyard(player1, "Plated Spider");
        harness.assertNotOnBattlefield(player1, "Apprentice Necromancer");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Plated Spider");
        harness.assertInGraveyard(player1, "Plated Spider");
    }

    @Test
    @DisplayName("Requires a creature card as the graveyard target")
    void rejectsNonCreatureGraveyardTarget() {
        addCreatureReady(player1, new ApprenticeNecromancer());
        Card land = new YavimayaHollow();
        harness.setGraveyard(player1, List.of(land));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, land.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Apprentice Necromancer");
        harness.assertInGraveyard(player1, "Yavimaya Hollow");
    }

    @Test
    @DisplayName("Cannot target a creature card in an opponent's graveyard")
    void rejectsOpponentsGraveyardTarget() {
        addCreatureReady(player1, new ApprenticeNecromancer());
        Card creature = new PlatedSpider();
        harness.setGraveyard(player2, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 0, null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Apprentice Necromancer");
        harness.assertInGraveyard(player2, "Plated Spider");
    }

    @Test
    @DisplayName("Cannot activate while the Necromancer has summoning sickness")
    void rejectsSummoningSickSource() {
        harness.addToBattlefield(player1, new ApprenticeNecromancer());
        Card creature = new PlatedSpider();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 0, null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Apprentice Necromancer");
        harness.assertInGraveyard(player1, "Plated Spider");
    }

    @Test
    @DisplayName("Cannot activate without the black mana in its cost")
    void rejectsActivationWithoutBlackMana() {
        addCreatureReady(player1, new ApprenticeNecromancer());
        Card creature = new PlatedSpider();
        harness.setGraveyard(player1, List.of(creature));

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 0, null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Apprentice Necromancer");
        harness.assertInGraveyard(player1, "Plated Spider");
    }

    @Test
    @DisplayName("Cannot activate while the Necromancer is tapped")
    void rejectsTappedSource() {
        Permanent necromancer = addCreatureReady(player1, new ApprenticeNecromancer());
        necromancer.tap();
        Card creature = new PlatedSpider();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 0, null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Apprentice Necromancer");
        harness.assertInGraveyard(player1, "Plated Spider");
    }

    @Test
    @DisplayName("Fizzles if the targeted creature leaves the graveyard before resolution")
    void fizzlesIfTargetLeavesGraveyard() {
        addCreatureReady(player1, new ApprenticeNecromancer());
        Card creature = new PlatedSpider();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, creature.getId(), Zone.GRAVEYARD);
        gd.playerGraveyards.get(player1.getId()).remove(creature);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Plated Spider");
        harness.assertNotOnBattlefield(player1, "Apprentice Necromancer");
        harness.assertInGraveyard(player1, "Apprentice Necromancer");
        assertThat(gd.stack).isEmpty();
    }
}
