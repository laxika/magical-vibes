package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ItemShopkeep.class, GrizzlyBears.class, LeoninScimitar.class})
class ItemShopkeepTest extends BaseCardTest {

    @Test
    @DisplayName("Whenever you attack, it targets an attacking equipped creature")
    void targetsAttackingEquippedCreature() {
        addCreatureReady(player1, new ItemShopkeep());
        Permanent equippedAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent unequippedAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        equipment.setAttachedTo(equippedAttacker.getId());

        declareAttackers(player1, List.of(1, 2));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds())
                .containsExactly(equippedAttacker.getId())
                .doesNotContain(unequippedAttacker.getId());

        harness.handlePermanentChosen(player1, equippedAttacker.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, equippedAttacker, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Menace wears off at end of turn")
    void menaceWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new ItemShopkeep());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        equipment.setAttachedTo(attacker.getId());

        declareAttackers(player1, List.of(1));
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.MENACE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Item Shopkeep can target itself when attacking and equipped")
    void canTargetItself() {
        Permanent shopkeep = addCreatureReady(player1, new ItemShopkeep());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        equipment.setAttachedTo(shopkeep.getId());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, shopkeep.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, shopkeep, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("A creature that loses its Equipment before resolution is an illegal target")
    void losingEquipmentBeforeResolutionInvalidatesTarget() {
        addCreatureReady(player1, new ItemShopkeep());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        equipment.setAttachedTo(attacker.getId());

        declareAttackers(List.of(1));
        harness.handlePermanentChosen(player1, attacker.getId());
        equipment.setAttachedTo(null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Losing Equipment after resolution does not remove granted menace")
    void losingEquipmentAfterResolutionKeepsMenace() {
        addCreatureReady(player1, new ItemShopkeep());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        equipment.setAttachedTo(attacker.getId());

        declareAttackers(List.of(1));
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();
        equipment.setAttachedTo(null);

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.MENACE)).isTrue();
    }
}
