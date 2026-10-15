package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ConduitPylons.class})
class ConduitPylonsTest extends BaseCardTest {

    @Test
    void entersAndSurveilsOne() {
        Card topCard = new ConduitPylons();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new ConduitPylons()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void tapsForColorlessMana() {
        Permanent pylons = addReadyPylons();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(pylons.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void paysOneAndTapsForAnyColorMana() {
        Permanent pylons = addReadyPylons();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(pylons.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void cannotPayAnyColorAbilityWithoutMana() {
        addReadyPylons();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canKeepSurveilledCardOnTop() {
        Card topCard = new ConduitPylons();
        Card nextCard = new ConduitPylons();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.setHand(player1, List.of(new ConduitPylons()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, nextCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void surveilsEmptyLibraryWithoutChoice() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new ConduitPylons()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canTapForManaBeforeEntryTriggerResolves() {
        Card topCard = new ConduitPylons();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new ConduitPylons()));

        harness.playLand(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
    }

    private Permanent addReadyPylons() {
        Permanent pylons = harness.addToBattlefieldAndReturn(player1, new ConduitPylons());
        pylons.setSummoningSick(false);
        return pylons;
    }
}
