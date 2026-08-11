package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.service.spell.SpellCastingService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DreamHarvestTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles each opponent's cards until total mana value five")
    void exilesUntilTotalManaValueFive() {
        Forest firstLand = new Forest();
        GrizzlyBears firstSpell = new GrizzlyBears();
        Forest secondLand = new Forest();
        GrizzlyBears secondSpell = new GrizzlyBears();
        GrizzlyBears thirdSpell = new GrizzlyBears();
        harness.setLibrary(player2,
                new ArrayList<>(List.of(firstLand, firstSpell, secondLand, secondSpell, thirdSpell)));

        harness.setHand(player1, List.of(new DreamHarvest()));
        harness.addMana(player1, ManaColor.BLUE, 7);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(firstLand.getId(), firstSpell.getId(), secondLand.getId(),
                        secondSpell.getId(), thirdSpell.getId());
        assertThat(gd.exilePlayPermissions)
                .containsEntry(firstSpell.getId(), player1.getId())
                .containsEntry(secondSpell.getId(), player1.getId())
                .containsEntry(thirdSpell.getId(), player1.getId())
                .doesNotContainKey(firstLand.getId())
                .doesNotContainKey(secondLand.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost)
                .contains(firstSpell.getId(), secondSpell.getId(), thirdSpell.getId())
                .doesNotContain(firstLand.getId(), secondLand.getId());
    }

    @Test
    @DisplayName("An exiled spell can be cast without mana until end of turn")
    void castsExiledSpellWithoutMana() {
        GrizzlyBears firstSpell = new GrizzlyBears();
        GrizzlyBears secondSpell = new GrizzlyBears();
        GrizzlyBears thirdSpell = new GrizzlyBears();
        harness.setLibrary(player2, new ArrayList<>(List.of(firstSpell, secondSpell, thirdSpell)));

        harness.setHand(player1, List.of(new DreamHarvest()));
        harness.addMana(player1, ManaColor.BLUE, 7);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        gd.playerManaPools.get(player1.getId()).clear();

        SpellCastingService spellCastingService =
                GameTestEngineContext.get().getBean(SpellCastingService.class);
        harness.inMutationScope(() -> spellCastingService.playCardFromExile(
                gd, player1, firstSpell.getId(), 0, null));

        assertThat(gd.stack)
                .anyMatch(entry -> entry.getCard().getId().equals(firstSpell.getId())
                        && entry.getEntryType() == StackEntryType.CREATURE_SPELL);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(firstSpell.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).doesNotContain(firstSpell.getId());
    }
}
