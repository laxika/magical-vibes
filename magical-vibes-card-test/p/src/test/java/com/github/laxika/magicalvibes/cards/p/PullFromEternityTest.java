package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BenalishCavalry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PullFromEternity.class, BenalishCavalry.class})
class PullFromEternityTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a face-up exiled card into its owner's graveyard")
    void putsFaceUpExiledCardIntoOwnersGraveyard() {
        BenalishCavalry exiledCard = new BenalishCavalry();
        harness.setExile(player2, List.of(exiledCard));
        harness.setHand(player1, List.of(new PullFromEternity()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, exiledCard.getId());

        assertThat(gd.findExiledCard(exiledCard.getId())).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(exiledCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(exiledCard);
    }

    @Test
    @DisplayName("Can move its controller's exiled noncreature card to the graveyard")
    void movesOwnExiledNoncreatureCardToGraveyard() {
        PullFromEternity exiledCard = new PullFromEternity();
        PullFromEternity spell = new PullFromEternity();
        harness.setExile(player1, List.of(exiledCard));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, exiledCard.getId());

        assertThat(gd.findExiledCard(exiledCard.getId())).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(exiledCard, spell);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(exiledCard);
    }

    @Test
    @DisplayName("Only the first resolving copy moves a shared exile target")
    void respondingCopyMakesOriginalTargetIllegal() {
        BenalishCavalry exiledCard = new BenalishCavalry();
        PullFromEternity original = new PullFromEternity();
        PullFromEternity response = new PullFromEternity();
        harness.setExile(player2, List.of(exiledCard));
        harness.setHand(player1, List.of(original, response));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, exiledCard.getId());
        harness.castInstant(player1, 0, exiledCard.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(exiledCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(response).doesNotContain(original);

        harness.passBothPriorities();

        assertThat(gd.findExiledCard(exiledCard.getId())).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(exiledCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(original, response);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a face-down exiled card")
    void cannotTargetFaceDownExiledCard() {
        BenalishCavalry exiledCard = new BenalishCavalry();
        gd.addToExile(player1.getId(), exiledCard, null, true);
        harness.setHand(player1, List.of(new PullFromEternity()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, exiledCard.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not found in exile");
    }

    @Test
    @DisplayName("Fizzles if the target leaves exile before resolution")
    void fizzlesIfTargetLeavesExileBeforeResolution() {
        BenalishCavalry exiledCard = new BenalishCavalry();
        harness.setExile(player2, List.of(exiledCard));
        harness.setHand(player1, List.of(new PullFromEternity()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, exiledCard.getId());
        gd.removeFromExile(exiledCard.getId());
        gd.addCardToHand(player2.getId(), exiledCard);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).contains(exiledCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(exiledCard);
    }
}
