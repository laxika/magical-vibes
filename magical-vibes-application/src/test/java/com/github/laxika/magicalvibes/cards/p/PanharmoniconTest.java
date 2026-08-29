package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AuthorityOfTheConsuls;
import com.github.laxika.magicalvibes.cards.e.ElvishVisionary;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.i.IchorWellspring;
import com.github.laxika.magicalvibes.cards.t.TatyovaBenthicDruid;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PanharmoniconTest extends BaseCardTest {

    @Test
    @DisplayName("Panharmonicon doubles a creature's enter-the-battlefield ability")
    void doublesCreatureEnterAbility() {
        harness.addToBattlefield(player1, new Panharmonicon());
        harness.setHand(player1, List.of(new ElvishVisionary()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    @DisplayName("Panharmonicon doubles an artifact's enter-the-battlefield ability")
    void doublesArtifactEnterAbility() {
        harness.addToBattlefield(player1, new Panharmonicon());
        harness.setHand(player1, List.of(new IchorWellspring()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    @DisplayName("Panharmonicon doubles a controlled trigger caused by an opponent's creature entering")
    void doublesTriggerFromOpponentCreatureEntering() {
        harness.addToBattlefield(player1, new Panharmonicon());
        harness.addToBattlefield(player1, new AuthorityOfTheConsuls());
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new FugitiveWizard()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    @DisplayName("Panharmonicon does not double a trigger caused by a land entering")
    void doesNotDoubleLandfallTrigger() {
        harness.addToBattlefield(player1, new Panharmonicon());
        harness.addToBattlefield(player1, new TatyovaBenthicDruid());
        harness.setHand(player1, List.of(new Forest()));
        harness.forceActivePlayer(player1);
        harness.clearPriorityPassed();
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }
}
