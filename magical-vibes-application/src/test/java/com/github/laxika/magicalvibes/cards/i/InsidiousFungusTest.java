package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GildedLotus;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InsidiousFungus.class, GildedLotus.class, GloriousAnthem.class, Forest.class})
class InsidiousFungusTest extends BaseCardTest {

    @Test
    void destroysTargetArtifactAndSacrificesItself() {
        addFungus();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new GildedLotus());
        activate(0, artifact.getId());

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Insidious Fungus");
        harness.assertInGraveyard(player2, "Gilded Lotus");
    }

    @Test
    void destroysTargetEnchantmentAndSacrificesItself() {
        addFungus();
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        activate(1, enchantment.getId());

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Insidious Fungus");
        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    void drawModeDrawsThenPutsAChosenLandOntoTheBattlefieldTapped() {
        addFungus();
        Card landInHand = new Forest();
        Card drawnCard = new Forest();
        harness.setHand(player1, List.of(landInHand));
        harness.setLibrary(player1, List.of(drawnCard));

        activate(2, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == landInHand && permanent.isTapped());
        harness.assertInGraveyard(player1, "Insidious Fungus");
    }

    @Test
    void drawModeCanDeclineTheLandPut() {
        addFungus();
        Card landInHand = new Forest();
        Card drawnCard = new Forest();
        harness.setHand(player1, List.of(landInHand));
        harness.setLibrary(player1, List.of(drawnCard));

        activate(2, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(landInHand, drawnCard);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == landInHand);
    }

    @Test
    void artifactModeRejectsAnEnchantmentTarget() {
        addFungus();
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addFungus() {
        harness.addToBattlefield(player1, new InsidiousFungus());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void activate(int mode, java.util.UUID targetId) {
        harness.activateAbility(player1, 0, mode, targetId);
    }
}
