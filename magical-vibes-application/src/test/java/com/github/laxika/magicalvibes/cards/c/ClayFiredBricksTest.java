package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ClayFiredBricks.class, CosmiumKiln.class, DarksteelRelic.class, LlanowarElves.class, Plains.class})
class ClayFiredBricksTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by finding a basic Plains and gaining two life")
    void entersWithPlainsAndLife() {
        harness.setHand(player1, List.of(new ClayFiredBricks()));
        harness.setLibrary(player1, List.of(new Plains(), new LlanowarElves()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).contains("Plains");
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Craft exiles another artifact and returns transformed with two Gnomes")
    void craftsFromBattlefieldArtifact() {
        Permanent bricks = harness.addToBattlefieldAndReturn(player1, new ClayFiredBricks());
        Permanent relic = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        addCraftMana();

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bricks, relic);
        assertThat(gd.findExiledCard(relic.getCard().getId())).isNotNull();

        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent kiln = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof CosmiumKiln)
                .findFirst().orElseThrow();
        assertThat(kiln.isTransformed()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(
                permanent -> permanent.getCard().getName().equals("Gnome")).hasSize(2);
        List<Permanent> gnomes = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Gnome"))
                .toList();
        assertThat(gnomes).allMatch(gnome -> gqs.getEffectivePower(gd, gnome) == 2);
    }

    @Test
    @DisplayName("Craft can exile an artifact card from the graveyard")
    void craftsFromGraveyardArtifact() {
        harness.addToBattlefieldAndReturn(player1, new ClayFiredBricks());
        DarksteelRelic relic = new DarksteelRelic();
        harness.setGraveyard(player1, List.of(relic));
        addCraftMana();

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.findExiledCard(relic.getId())).isNotNull();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(
                permanent -> permanent.getCard() instanceof CosmiumKiln);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(
                permanent -> permanent.getCard() instanceof ClayFiredBricks);
    }

    @Test
    @DisplayName("Craft prompts when more than one material is available")
    void choosesCraftMaterial() {
        harness.addToBattlefieldAndReturn(player1, new ClayFiredBricks());
        harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        DarksteelRelic graveyardRelic = new DarksteelRelic();
        harness.setGraveyard(player1, List.of(graveyardRelic));
        addCraftMana();

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.CraftMaterialChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(graveyardRelic.getId()));
        assertThat(gd.findExiledCard(graveyardRelic.getId())).isNotNull();
    }

    private void addCraftMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.WHITE, 2);
    }
}
