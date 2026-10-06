package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SenateGuildmage.class, Forest.class, GrizzlyBears.class})
class SenateGuildmageTest extends BaseCardTest {

    @Test
    @DisplayName("The white ability gains 2 life")
    void whiteAbilityGainsLife() {
        Permanent guildmage = addCreatureReady(player1, new SenateGuildmage());
        harness.setLife(player1, 17);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(guildmage.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The blue ability draws a card, then discards a card")
    void blueAbilityDrawsThenDiscards() {
        addCreatureReady(player1, new SenateGuildmage());
        Card discarded = new Forest();
        Card drawn = new GrizzlyBears();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
    }

    @Test
    @DisplayName("The newly drawn card can be chosen for discard")
    void canDiscardNewlyDrawnCard() {
        addCreatureReady(player1, new SenateGuildmage());
        Card kept = new SenateGuildmage();
        Card drawn = new SenateGuildmage();
        harness.setHand(player1, List.of(kept));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept, drawn);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("With an empty hand, the blue ability still draws and discards")
    void blueAbilityWithEmptyHand() {
        Permanent guildmage = addCreatureReady(player1, new SenateGuildmage());
        Card drawn = new SenateGuildmage();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(guildmage.isTapped()).isTrue();
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.DiscardChoice) {
            harness.handleCardChosen(player1, 0);
        }

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Both abilities require the correct colored mana")
    void abilitiesRequireCorrectMana(int abilityIndex) {
        Permanent guildmage = addCreatureReady(player1, new SenateGuildmage());
        harness.addMana(player1, abilityIndex == 0 ? ManaColor.BLUE : ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(guildmage.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Neither ability can be activated while tapped")
    void abilitiesRequireUntappedSource(int abilityIndex) {
        Permanent guildmage = addCreatureReady(player1, new SenateGuildmage());
        guildmage.tap();
        harness.addMana(player1, abilityIndex == 0 ? ManaColor.WHITE : ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Summoning sickness prevents both tap abilities")
    void summoningSicknessPreventsAbilities(int abilityIndex) {
        Permanent guildmage = addCreatureReady(player1, new SenateGuildmage());
        guildmage.setSummoningSick(true);
        harness.addMana(player1, abilityIndex == 0 ? ManaColor.WHITE : ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(guildmage.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
