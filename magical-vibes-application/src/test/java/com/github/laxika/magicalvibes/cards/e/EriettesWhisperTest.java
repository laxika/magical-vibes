package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EriettesWhisper.class, GrizzlyBears.class})
class EriettesWhisperTest extends BaseCardTest {

    @Test
    void opponentDiscardsTwoAndWickedRoleBoostsTargetCreature() {
        Card firstCard = new GrizzlyBears();
        Card secondCard = new GrizzlyBears();
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player2, new ArrayList<>(List.of(firstCard, secondCard)));
        harness.setHand(player1, List.of(new EriettesWhisper()));
        addMana();

        harness.castSorcery(player1, 0, List.of(player2.getId(), target.getId()));
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        Permanent role = findPermanent(player1, "Wicked");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(firstCard, secondCard);
        assertThat(role.getCard().getSubtypes()).contains(CardSubtype.ROLE);
        assertThat(role.getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isTrue();
    }

    @Test
    void creatureTargetCanBeOmitted() {
        Card firstCard = new GrizzlyBears();
        Card secondCard = new GrizzlyBears();
        harness.setHand(player2, new ArrayList<>(List.of(firstCard, secondCard)));
        harness.setHand(player1, List.of(new EriettesWhisper()));
        addMana();

        harness.castSorcery(player1, 0, List.of(player2.getId()));
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(findPermanents(player1, "Wicked")).isEmpty();
    }

    @Test
    void replacingWickedRoleTriggersItsLifeLoss() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player2, new ArrayList<>(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears())));
        harness.setHand(player1, List.of(new EriettesWhisper(), new EriettesWhisper()));
        addMana(2);

        castAndResolve(target);
        assertThat(findPermanents(player1, "Wicked")).hasSize(1);
        int lifeBeforeReplacement = gd.playerLifeTotals.get(player2.getId());

        castAndResolve(target);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Wicked")).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBeforeReplacement - 1);
    }

    @Test
    void onlyOpponentsCanBeTargetedForDiscard() {
        harness.setHand(player1, List.of(new EriettesWhisper()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    private void castAndResolve(Permanent target) {
        harness.castSorcery(player1, 0, List.of(player2.getId(), target.getId()));
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
    }

    private void addMana() {
        addMana(1);
    }

    private void addMana(int casts) {
        harness.addMana(player1, ManaColor.BLACK, casts);
        harness.addMana(player1, ManaColor.COLORLESS, 3 * casts);
    }
}
