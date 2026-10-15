package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.w.WildGooseChase;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AlbiorixGooseTyrant.class, WildGooseChase.class, Forest.class})
class AlbiorixGooseTyrantTest extends BaseCardTest {

    @Test
    void adventureDrawsTwoDiscardsTwoAndCreatesFood() {
        AlbiorixGooseTyrant card = new AlbiorixGooseTyrant();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Food");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void sacrificingATokenBoostsAlbiorixOnTheBattlefield() {
        AlbiorixGooseTyrant card = castAdventureAndCreatureFace();
        Permanent albiorix = findPermanent(player1, "Albiorix, Goose Tyrant");
        assertThat(gqs.getEffectivePower(gd, albiorix)).isEqualTo(3);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, albiorix)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, albiorix)).isEqualTo(4);
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    @Test
    void sacrificingATokenWhileAlbiorixIsExiledBoostsItsCreatureFace() {
        AlbiorixGooseTyrant card = new AlbiorixGooseTyrant();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAdventure(player1, 0, List.of());
        resolveAdventureDiscard();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        var albiorix = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Albiorix, Goose Tyrant"))
                .findFirst()
                .orElseThrow();
        assertThat(gqs.getEffectivePower(gd, albiorix)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, albiorix)).isEqualTo(4);
    }

    private AlbiorixGooseTyrant castAdventureAndCreatureFace() {
        AlbiorixGooseTyrant card = new AlbiorixGooseTyrant();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAdventure(player1, 0, List.of());
        resolveAdventureDiscard();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        return card;
    }

    private void resolveAdventureDiscard() {
        resolveAllTriggers();
        while (gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class) != null) {
            harness.handleCardChosen(player1, 0);
        }
        resolveAllTriggers();
    }
}
