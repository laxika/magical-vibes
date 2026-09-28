package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DisorientingChoice.class, Forest.class, GloriousAnthem.class, Plains.class, SolRing.class})
class DisorientingChoiceTest extends BaseCardTest {

    @Test
    @DisplayName("The opponent may keep a chosen permanent, then the caster searches for a tapped land")
    void keepsPermanentAndSearchesForSurvivors() {
        Permanent anthem = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        harness.setLibrary(player1, List.of(new Forest()));

        cast(List.of(anthem.getId()));
        harness.handleListChoice(player2, "Keep it");

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(anthem);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        Permanent forest = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof Forest)
                .findFirst()
                .orElseThrow();
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The opponent may exile a chosen permanent, preventing the follow-up search when none survive")
    void exilesPermanentAndSkipsSearchWhenNoneSurvive() {
        Permanent ring = harness.addToBattlefieldAndReturn(player2, new SolRing());
        harness.setLibrary(player1, List.of(new Forest()));

        cast(List.of(ring.getId()));
        harness.handleListChoice(player2, "Exile it");

        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(permanent -> permanent.getId().equals(ring.getId()));
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Only opponent-controlled artifacts and enchantments can be chosen")
    void rejectsInvalidTargets() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());
        prepareCast();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(List<UUID> targetIds) {
        prepareCast();
        harness.castSorcery(player1, 0, targetIds);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new DisorientingChoice()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
