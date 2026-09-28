package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheLostAndTheDamned.class, Forest.class, GrizzlyBears.class})
class TheLostAndTheDamnedTest extends BaseCardTest {

    @Test
    void landEnteringFromGraveyardCreatesSpawn() {
        harness.addToBattlefield(player1, new TheLostAndTheDamned());
        Forest land = new Forest();
        harness.setGraveyard(player1, List.of(land));
        gd.graveyardPlayPermissions.put(land.getId(), player1.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.playGraveyardLand(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spawn")).hasSize(1);
    }

    @Test
    void spellCastFromExileCreatesSpawn() {
        harness.addToBattlefield(player1, new TheLostAndTheDamned());
        GrizzlyBears spell = new GrizzlyBears();
        gd.addToExile(player1.getId(), spell);
        gd.exilePlayPermissions.put(spell.getId(), player1.getId());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player1, spell.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spawn")).hasSize(1);
    }

    @Test
    void landEnteringFromHandDoesNotCreateSpawn() {
        harness.addToBattlefield(player1, new TheLostAndTheDamned());
        harness.setHand(player1, List.of(new Forest()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.playLand(player1, 0);

        assertThat(findPermanents(player1, "Spawn")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
