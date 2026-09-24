package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.u.UrzasSylex;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TemplarKnight.class, UrzasSylex.class, GrizzlyBears.class, Spellbook.class})
class TemplarKnightTest extends BaseCardTest {

    @Test
    @DisplayName("Taps five attacking Templar Knights to put a legendary artifact from the library onto the battlefield")
    void searchesForLegendaryArtifact() {
        Permanent source = addTemplar(false);
        List<Permanent> attackingTemplars = addTemplars(5, true);
        UrzasSylex sylex = new UrzasSylex();
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Spellbook(), sylex));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null);

        assertThat(source.isTapped()).isFalse();
        assertThat(attackingTemplars).allMatch(Permanent::isTapped);

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(sylex);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == sylex);
    }

    @Test
    @DisplayName("Requires five untapped attacking creatures named Templar Knight")
    void requiresFiveMatchingAttackers() {
        addTemplar(false);
        addTemplars(4, true);
        Permanent attackingBears = addCreatureReady(player1, new GrizzlyBears());
        attackingBears.setAttacking(true);
        Permanent nonattackingTemplar = addTemplar(false);
        nonattackingTemplar.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough untapped permanents to tap");
    }

    private Permanent addTemplar(boolean attacking) {
        Permanent permanent = addCreatureReady(player1, new TemplarKnight());
        permanent.setAttacking(attacking);
        return permanent;
    }

    private List<Permanent> addTemplars(int count, boolean attacking) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(ignored -> addTemplar(attacking))
                .toList();
    }
}
