package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KlothysGodOfDestiny.class, Forest.class, GrizzlyBears.class, LlanowarElves.class, RagingGoblin.class})
class KlothysGodOfDestinyTest extends BaseCardTest {

    @Test
    @DisplayName("Klothys is not a creature below seven combined red and green devotion")
    void isNotCreatureBelowDevotionThreshold() {
        Permanent klothys = addKlothys();

        assertThat(gqs.isCreature(gd, klothys)).isFalse();
        assertThat(gqs.isEnchantment(gd, klothys)).isTrue();
    }

    @Test
    @DisplayName("Klothys becomes a creature at seven combined red and green devotion")
    void becomesCreatureAtDevotionThreshold() {
        Permanent klothys = addKlothys();
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new RagingGoblin());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new RagingGoblin());
        harness.addToBattlefield(player1, new LlanowarElves());

        assertThat(gqs.isCreature(gd, klothys)).isTrue();
    }

    @Test
    @DisplayName("A land exiled in the first main phase adds a chosen red or green mana")
    void exiledLandAddsChosenMana() {
        addKlothys();
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(land));

        advanceToPrecombatMain(player1);
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(land.getId());

        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));
        harness.passBothPriorities();
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(land);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("A nonland card exiled in the first main phase gains life and damages each opponent")
    void exiledNonlandGainsLifeAndDamagesOpponents() {
        addKlothys();
        Card nonland = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(nonland));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToPrecombatMain(player1);
        harness.handleMultipleCardsChosen(player1, List.of(nonland.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(nonland);
        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Klothys does not trigger when no graveyard card can be targeted")
    void doesNotTriggerWithoutGraveyardCards() {
        addKlothys();

        advanceToPrecombatMain(player1);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addKlothys() {
        return harness.addToBattlefieldAndReturn(player1, new KlothysGodOfDestiny());
    }

    private void advanceToPrecombatMain(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
