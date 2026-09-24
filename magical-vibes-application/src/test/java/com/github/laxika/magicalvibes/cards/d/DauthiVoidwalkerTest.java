package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DauthiVoidwalker.class, Shock.class, GrizzlyBears.class})
class DauthiVoidwalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles an opponent's card from anywhere with a void counter")
    void replacesOpponentGraveyardEntryWithVoidCounter() {
        addReadyDauthi();
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(shock.getId()));
        assertThat(gd.findExiledCard(shock.getId())).isNotNull();
        assertThat(gd.findExiledCard(shock.getId()).ownerId()).isEqualTo(player2.getId());
        assertThat(gd.exiledCardsWithVoidCounters).contains(shock.getId());
    }

    @Test
    @DisplayName("Permits its controller to cast an opponent-owned void-counter card")
    void permitsCastingOpponentCardFromExile() {
        addReadyDauthi();
        GrizzlyBears bears = new GrizzlyBears();
        gd.addToExileWithVoidCounter(player2.getId(), bears, null);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFromExile(player1, bears.getId());

        assertThat(gd.stack).singleElement().satisfies(entry -> {
            assertThat(entry.getCard().getId()).isEqualTo(bears.getId());
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
            assertThat(entry.getControllerId()).isEqualTo(player1.getId());
        });
        assertThat(gd.findExiledCard(bears.getId())).isNull();
    }

    @Test
    @DisplayName("Sacrifice ability offers one opponent-owned void-counter card for free")
    void sacrificesAndFreeCastsVoidCounterCard() {
        Permanent dauthi = addReadyDauthi();
        GrizzlyBears bears = new GrizzlyBears();
        gd.addToExileWithVoidCounter(player2.getId(), bears, null);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).singleElement().satisfies(entry -> {
            assertThat(entry.getCard().getId()).isEqualTo(bears.getId());
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
            assertThat(entry.getControllerId()).isEqualTo(player1.getId());
        });
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(dauthi);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(bears.getId()));
    }

    private Permanent addReadyDauthi() {
        return addCreatureReady(player1, new DauthiVoidwalker());
    }
}
