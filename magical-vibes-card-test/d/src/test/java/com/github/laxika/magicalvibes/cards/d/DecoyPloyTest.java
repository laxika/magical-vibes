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

    @Test
    @DisplayName("The Hero mode alone leaves Villains in the graveyard")
    void returnsOnlyHeroCardToHand() {
        Card villain = new CrossbonesMaliciousMercenary();
        Card hero = new HeroInTraining();
        harness.setGraveyard(player1, List.of(villain, hero));
        harness.setHand(player1, List.of(new DecoyPloy()));
        addMana();

        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{1}, List.of(hero.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Hero in Training");
        harness.assertNotInGraveyard(player1, "Hero in Training");
        harness.assertInGraveyard(player1, "Crossbones, Malicious Mercenary");
        harness.assertNotInHand(player1, "Crossbones, Malicious Mercenary");
    }

    @Test
    @DisplayName("The Villain mode cannot target an opponent's graveyard")
    void rejectsOpponentVillain() {
        Card villain = new CrossbonesMaliciousMercenary();
        harness.setGraveyard(player2, List.of(villain));
        harness.setHand(player1, List.of(new DecoyPloy()));
        addMana();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{0}, List.of(villain.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The Hero mode cannot target an opponent's graveyard")
    void rejectsOpponentHero() {
        Card hero = new HeroInTraining();
        harness.setGraveyard(player2, List.of(hero));
        harness.setHand(player1, List.of(new DecoyPloy()));
        addMana();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{1}, List.of(hero.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The remaining legal target is returned when the Villain target leaves the graveyard")
    void returnsHeroWhenVillainTargetLeavesGraveyard() {
        Card villain = new CrossbonesMaliciousMercenary();
        Card hero = new HeroInTraining();
        harness.setGraveyard(player1, List.of(villain, hero));
        harness.setHand(player1, List.of(new DecoyPloy()));
        addMana();

        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0, 1},
                List.of(villain.getId(), hero.getId()));
        harness.setGraveyard(player1, List.of(hero));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Hero in Training");
        harness.assertNotInGraveyard(player1, "Hero in Training");
        harness.assertNotInHand(player1, "Crossbones, Malicious Mercenary");
    }
    @Test
    @DisplayName("The remaining legal target is returned when the Hero target leaves the graveyard")
    void returnsVillainWhenHeroTargetLeavesGraveyard() {
        Card villain = new CrossbonesMaliciousMercenary();
        Card hero = new HeroInTraining();
        harness.setGraveyard(player1, List.of(villain, hero));
        harness.setHand(player1, List.of(new DecoyPloy()));
        addMana();

        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0, 1},
                List.of(villain.getId(), hero.getId()));
        harness.setGraveyard(player1, List.of(villain));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Crossbones, Malicious Mercenary");
        harness.assertNotInGraveyard(player1, "Crossbones, Malicious Mercenary");
        harness.assertNotInHand(player1, "Hero in Training");
    }

    @Test
    @DisplayName("The Hero mode cannot target a Villain without the Hero subtype")
    void rejectsVillainForHeroMode() {
        Card villain = new CrossbonesMaliciousMercenary();
        harness.setGraveyard(player1, List.of(villain));
        harness.setHand(player1, List.of(new DecoyPloy()));
        addMana();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{1}, List.of(villain.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
