package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TasteOfDeath.class, GrizzlyBears.class})
class TasteOfDeathTest extends BaseCardTest {

    @Test
    @DisplayName("Each player sacrifices three creatures and the caster creates three Foods")
    void eachPlayerSacrificesThreeCreaturesAndCreatesFood() {
        addCreatures(player1, 3);
        addCreatures(player2, 3);

        castTasteOfDeath();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(countPermanents(player1, "Food")).isEqualTo(3);
    }

    @Test
    @DisplayName("Players choose which three creatures to sacrifice when they have more")
    void playersChooseCreaturesToSacrifice() {
        addCreatures(player1, 4);
        addCreatures(player2, 4);

        castTasteOfDeath();

        PendingInteraction.MultiPermanentChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(firstChoice).isNotNull();
        assertThat(firstChoice.playerId()).isEqualTo(player1.getId());
        assertThat(firstChoice.maxCount()).isEqualTo(3);
        assertThat(firstChoice.context()).isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);

        List<Permanent> player1Creatures = findPermanents(player1, "Grizzly Bears");
        harness.handleMultiplePermanentsChosen(player1,
                player1Creatures.subList(0, 3).stream().map(Permanent::getId).toList());

        PendingInteraction.MultiPermanentChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(secondChoice).isNotNull();
        assertThat(secondChoice.playerId()).isEqualTo(player2.getId());
        assertThat(secondChoice.maxCount()).isEqualTo(3);

        List<Permanent> player2Creatures = findPermanents(player2, "Grizzly Bears");
        harness.handleMultiplePermanentsChosen(player2,
                player2Creatures.subList(0, 3).stream().map(Permanent::getId).toList());

        assertThat(countPermanents(player1, "Grizzly Bears")).isOne();
        assertThat(countPermanents(player2, "Grizzly Bears")).isOne();
        assertThat(countPermanents(player1, "Food")).isEqualTo(3);
    }

    @Test
    @DisplayName("A Food created by Taste of Death can be sacrificed for three life")
    void createdFoodCanBeSacrificedForLife() {
        addCreatures(player1, 3);
        addCreatures(player2, 3);

        castTasteOfDeath();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(countPermanents(player1, "Food")).isEqualTo(2);
    }

    private void castTasteOfDeath() {
        harness.setHand(player1, List.of(new TasteOfDeath()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private void addCreatures(com.github.laxika.magicalvibes.model.Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new GrizzlyBears());
        }
    }
}
