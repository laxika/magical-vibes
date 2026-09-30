package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScalesoulGnome.class, Forest.class, GrizzlyBears.class})
class ScalesoulGnomeTest extends BaseCardTest {

    @Test
    void discoversUsingCombatDamage() {
        Forest land = new Forest();
        GrizzlyBears discovered = new GrizzlyBears();
        harness.setLibrary(player1, List.of(land, discovered));
        harness.setHand(player1, List.of());
        addReadyGnome(player1);

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(discovered);

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).contains(discovered);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
    }

    @Test
    void conjuresDuplicateWhenSpellIsCastFromExile() {
        harness.setHand(player1, List.of());
        addReadyGnome(player1);
        GrizzlyBears spell = new GrizzlyBears();
        gd.addToExile(player1.getId(), spell);
        gd.exilePlayPermissions.put(spell.getId(), player1.getId());
        prepareMainPhase(player1);

        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 2);
        harness.castFromExile(player1, spell.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        Card duplicate = gd.playerHands.get(player1.getId()).getFirst();
        assertThat(duplicate.getId()).isNotEqualTo(spell.getId());
        assertThat(duplicate.isTokenCard()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == spell);
    }

    @Test
    void conjuresDuplicateWhenLandIsPlayedFromExile() {
        harness.setHand(player1, List.of());
        addReadyGnome(player1);
        Forest land = new Forest();
        gd.addToExile(player1.getId(), land);
        gd.exilePlayPermissions.put(land.getId(), player1.getId());
        prepareMainPhase(player1);

        harness.castFromExile(player1, land.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        Card duplicate = gd.playerHands.get(player1.getId()).getFirst();
        assertThat(duplicate.getId()).isNotEqualTo(land.getId());
        assertThat(duplicate.isTokenCard()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == land);
    }

    private Permanent addReadyGnome(Player player) {
        Permanent gnome = harness.addToBattlefieldAndReturn(player, new ScalesoulGnome());
        gnome.setSummoningSick(false);
        return gnome;
    }

    private void prepareMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
