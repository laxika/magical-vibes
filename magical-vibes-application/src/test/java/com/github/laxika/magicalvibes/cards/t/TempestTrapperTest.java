package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TempestTrapper.class, LightningBolt.class})
class TempestTrapperTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability adds two mana in separately chosen colors for instants and sorceries")
    void tapAbilityAddsRestrictedManaInAnyColorCombination() {
        addCreatureReady(player1, new TempestTrapper());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).getInstantSorceryOnlyColored(ManaColor.BLUE))
                .isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getInstantSorceryOnlyColored(ManaColor.RED))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("The third spell exiles a shuffled library card with free-play permission")
    void thirdSpellExilesLibraryCardForFree() {
        addCreatureReady(player1, new TempestTrapper());
        Card exiledCard = new LightningBolt();
        harness.setLibrary(player1, List.of(exiledCard));
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(exiledCard);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(exiledCard);
        assertThat(gd.exilePlayPermissions).containsEntry(exiledCard.getId(), player1.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).contains(exiledCard.getId());
    }

    @Test
    @DisplayName("Free-play permission lets the third-spell card be cast without mana")
    void thirdSpellCardCanBeCastWithoutMana() {
        addCreatureReady(player1, new TempestTrapper());
        Card exiledCard = new LightningBolt();
        harness.setLibrary(player1, List.of(exiledCard));
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        gd.playerManaPools.get(player1.getId()).clear();
        harness.castFromExile(player1, exiledCard.getId(), player2.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(exiledCard);
    }
}
