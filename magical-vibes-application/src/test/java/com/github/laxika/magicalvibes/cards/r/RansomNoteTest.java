package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RansomNote.class, Forest.class, GrizzlyBears.class})
class RansomNoteTest extends BaseCardTest {

    @Test
    void entersAndSurveilsOne() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        harness.enterBattlefieldAndReturn(player1, new RansomNote());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    void cloakModeCloaksTheTopCardAndSacrificesRansomNote() {
        Card topCard = new GrizzlyBears();
        harness.addToBattlefield(player1, new RansomNote());
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null);
        harness.passBothPriorities();

        Permanent cloaked = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == topCard)
                .findFirst()
                .orElseThrow();
        assertThat(cloaked.isFaceDown()).isTrue();
        assertThat(cloaked.isCloaked()).isTrue();
        harness.assertInGraveyard(player1, "Ransom Note");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void goadModeGoadsTargetCreatureAndSacrificesRansomNote() {
        harness.addToBattlefield(player1, new RansomNote());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.isGoaded(gd, target)).isTrue();
        harness.assertInGraveyard(player1, "Ransom Note");
    }

    @Test
    void drawModeDrawsACardAndSacrificesRansomNote() {
        Card drawnCard = new GrizzlyBears();
        harness.addToBattlefield(player1, new RansomNote());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
        harness.assertInGraveyard(player1, "Ransom Note");
    }

    @Test
    void goadModeRejectsANonCreatureTarget() {
        harness.addToBattlefield(player1, new RansomNote());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
