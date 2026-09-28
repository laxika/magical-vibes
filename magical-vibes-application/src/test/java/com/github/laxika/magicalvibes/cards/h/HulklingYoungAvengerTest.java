package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HulklingYoungAvenger.class, GrizzlyBears.class, Divination.class})
class HulklingYoungAvengerTest extends BaseCardTest {

    @Test
    @DisplayName("A noncreature spell copies another creature with Hulkling's exceptions")
    void copiesAnotherCreatureWithExceptions() {
        Permanent hulkling = harness.addToBattlefieldAndReturn(player1, new HulklingYoungAvenger());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castDivination();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(bears.getId()).doesNotContain(hulkling.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(hulkling.getCard().getName()).isEqualTo("Hulkling, Young Avenger");
        assertThat(gqs.getEffectivePower(gd, hulkling)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, hulkling)).isEqualTo(4);
        assertThat(gqs.hasEffectiveSubtype(gd, hulkling, CardSubtype.BEAR)).isTrue();
        assertThat(gqs.hasKeyword(gd, hulkling, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Hulkling retains its copy trigger while copied")
    void retainsCopyTriggerWhileCopied() {
        Permanent hulkling = harness.addToBattlefieldAndReturn(player1, new HulklingYoungAvenger());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castDivination();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        castDivination();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(hulkling.getCard().getName()).isEqualTo("Hulkling, Young Avenger");
    }

    @Test
    @DisplayName("Creature spells do not trigger Hulkling")
    void creatureSpellsDoNotTrigger() {
        harness.addToBattlefield(player1, new HulklingYoungAvenger());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    private void castDivination() {
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0);
    }
}
