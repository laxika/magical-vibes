package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApprenticeNecromancerTest extends BaseCardTest {

    @Test
    @DisplayName("Reanimates a creature with haste and sacrifices it at the next end step")
    void reanimatesWithHasteAndSacrificesAtNextEndStep() {
        Permanent necromancer = harness.addToBattlefieldAndReturn(player1, new ApprenticeNecromancer());
        necromancer.setSummoningSick(false);
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        Permanent reanimated = findPermanent(player1, "Grizzly Bears");
        assertThat(reanimated.getGrantedKeywords()).contains(Keyword.HASTE);
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Apprentice Necromancer");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Requires a creature card as the graveyard target")
    void rejectsNonCreatureGraveyardTarget() {
        Permanent necromancer = harness.addToBattlefieldAndReturn(player1, new ApprenticeNecromancer());
        necromancer.setSummoningSick(false);
        Card land = new Mountain();
        harness.setGraveyard(player1, List.of(land));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, land.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Apprentice Necromancer");
        harness.assertInGraveyard(player1, "Mountain");
    }
}
