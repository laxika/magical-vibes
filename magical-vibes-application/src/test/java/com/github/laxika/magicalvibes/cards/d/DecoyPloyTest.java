package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CrossbonesMaliciousMercenary;
import com.github.laxika.magicalvibes.cards.h.HeroInTraining;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DecoyPloy.class, CrossbonesMaliciousMercenary.class, HeroInTraining.class})
class DecoyPloyTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target Villain card from the graveyard to hand")
    void returnsVillainCardToHand() {
        Card villain = new CrossbonesMaliciousMercenary();
        Card hero = new HeroInTraining();
        harness.setGraveyard(player1, List.of(villain, hero));
        harness.setHand(player1, List.of(new DecoyPloy()));
        addMana();

        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0}, List.of(villain.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Crossbones, Malicious Mercenary");
        harness.assertNotInGraveyard(player1, "Crossbones, Malicious Mercenary");
        harness.assertInGraveyard(player1, "Hero in Training");
    }

    @Test
    @DisplayName("Choosing both modes returns a Villain and a Hero card")
    void returnsBothCardTypesToHand() {
        Card villain = new CrossbonesMaliciousMercenary();
        Card hero = new HeroInTraining();
        harness.setGraveyard(player1, List.of(villain, hero));
        harness.setHand(player1, List.of(new DecoyPloy()));
        addMana();

        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0, 1},
                List.of(villain.getId(), hero.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Crossbones, Malicious Mercenary");
        harness.assertInHand(player1, "Hero in Training");
        harness.assertNotInGraveyard(player1, "Crossbones, Malicious Mercenary");
        harness.assertNotInGraveyard(player1, "Hero in Training");
    }

    @Test
    @DisplayName("A mode cannot target the wrong card subtype")
    void rejectsWrongCardSubtype() {
        Card hero = new HeroInTraining();
        harness.setGraveyard(player1, List.of(hero));
        harness.setHand(player1, List.of(new DecoyPloy()));
        addMana();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{0}, List.of(hero.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
