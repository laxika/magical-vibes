package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NarsetEnlightenedMasterTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking exiles the top four cards and grants free permission only to noncreature spells")
    void attackingExilesTopFourAndGrantsFreePermissionToNoncreatureSpells() {
        Card spell = new Divination();
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        Card otherSpell = new Shock();
        harness.setLibrary(player1, List.of(spell, creature, land, otherSpell));
        addCreatureReady(player1, new NarsetEnlightenedMaster());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(spell.getId(), creature.getId(), land.getId(), otherSpell.getId());
        assertThat(gd.exilePlayPermissions)
                .containsEntry(spell.getId(), player1.getId())
                .containsEntry(otherSpell.getId(), player1.getId())
                .doesNotContainKeys(creature.getId(), land.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost)
                .contains(spell.getId(), otherSpell.getId())
                .doesNotContain(creature.getId(), land.getId());
    }

    @Test
    @DisplayName("A permitted spell can be cast from exile without mana")
    void permittedSpellCanBeCastFromExileWithoutMana() {
        Card spell = new Divination();
        harness.setLibrary(player1, List.of(
                spell, new GrizzlyBears(), new Forest(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears()));
        addCreatureReady(player1, new NarsetEnlightenedMaster());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player1, spell.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(spell.getId());
    }
}
