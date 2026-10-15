package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DefiantFalcon;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.r.RamosianCommander;
import com.github.laxika.magicalvibes.cards.s.SaltfieldRecluse;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Blightspeaker.class, DefiantFalcon.class, GrizzlyBears.class, HolyDay.class,
        RamosianCommander.class, SaltfieldRecluse.class})
class BlightspeakerTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability makes the targeted player lose 1 life")
    void targetPlayerLosesLife() {
        Permanent blightspeaker = addReadyBlightspeaker();
        int lifeBefore = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
        assertThat(blightspeaker.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The first ability may target its controller")
    void targetControllerLosesLife() {
        Permanent blightspeaker = addReadyBlightspeaker();
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(blightspeaker.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The second ability offers only Rebel permanents with mana value 3 or less")
    void searchesForEligibleRebelPermanent() {
        addReadyBlightspeaker();
        harness.setLibrary(player1, List.of(
                new DefiantFalcon(),
                new RamosianCommander(),
                new GrizzlyBears(),
                new HolyDay()));

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactly("Defiant Falcon");

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Defiant Falcon");
    }

    @Test
    @DisplayName("The second ability does nothing when no eligible Rebel is in the library")
    void noEligibleRebelFound() {
        addReadyBlightspeaker();
        harness.setLibrary(player1, List.of(new RamosianCommander(), new GrizzlyBears(), new HolyDay()));

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotOnBattlefield(player1, "Ramosian Commander");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A Rebel with mana value exactly three enters untapped without being cast")
    void findsRebelAtManaValueLimit() {
        Permanent blightspeaker = addReadyBlightspeaker();
        Card rebel = new SaltfieldRecluse();
        harness.setLibrary(player1, List.of(rebel));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(blightspeaker.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent found = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(rebel.getId()))
                .findFirst().orElseThrow();
        assertThat(found.isTapped()).isFalse();
        assertThat(found.isSummoningSick()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertNotOnBattlefield(player2, "Saltfield Recluse");
    }

    @Test
    @DisplayName("The controller may fail to find even when an eligible Rebel exists")
    void mayFailToFindEligibleRebel() {
        addReadyBlightspeaker();
        Card rebel = new Blightspeaker();
        harness.setLibrary(player1, List.of(rebel));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(rebel);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Searching requires four mana")
    void cannotSearchWithOnlyThreeMana() {
        addReadyBlightspeaker();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Both tap abilities are unavailable while summoning sick")
    void summoningSicknessPreventsBothAbilities() {
        harness.addToBattlefield(player1, new Blightspeaker());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Both tap abilities are unavailable while already tapped")
    void tappedBlightspeakerCannotActivateEitherAbility() {
        addReadyBlightspeaker().tap();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyBlightspeaker() {
        return addCreatureReady(player1, new Blightspeaker());
    }
}
