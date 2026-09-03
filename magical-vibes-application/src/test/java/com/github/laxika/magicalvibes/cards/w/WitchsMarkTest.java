package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WitchsMark.class, Forest.class, GrizzlyBears.class})
class WitchsMarkTest extends BaseCardTest {

    @Test
    void discardingDrawsTwoAndCreatesWickedRole() {
        Card discarded = new GrizzlyBears();
        Card firstDraw = new Forest();
        Card secondDraw = new Forest();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        setDeck(firstDraw, secondDraw);
        harness.setHand(player1, new ArrayList<>(List.of(new WitchsMark(), discarded)));
        addMana();

        harness.castSorcery(player1, 0, List.of(target.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        Permanent role = findPermanent(player1, "Wicked");
        assertThat(role.getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    void decliningDiscardStillCreatesWickedRole() {
        Card kept = new GrizzlyBears();
        Card topCard = new Forest();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        setDeck(topCard);
        harness.setHand(player1, new ArrayList<>(List.of(new WitchsMark(), kept)));
        addMana();

        harness.castSorcery(player1, 0, List.of(target.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(findPermanents(player1, "Wicked")).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    void creatureTargetCanBeOmitted() {
        Card kept = new GrizzlyBears();
        harness.setHand(player1, new ArrayList<>(List.of(new WitchsMark(), kept)));
        addMana();

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(findPermanents(player1, "Wicked")).isEmpty();
    }

    @Test
    void cannotTargetAnOpponentCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WitchsMark()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void setDeck(Card... cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(cards));
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
