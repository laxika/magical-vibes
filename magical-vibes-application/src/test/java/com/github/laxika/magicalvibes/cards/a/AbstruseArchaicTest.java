package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.IchorWellspring;
import com.github.laxika.magicalvibes.cards.t.TrialOfZeal;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AbstruseArchaic.class, Forest.class, IchorWellspring.class, TrialOfZeal.class})
class AbstruseArchaicTest extends BaseCardTest {

    @Test
    @DisplayName("Copies a triggered ability from a colorless source")
    void copiesColorlessTriggeredAbility() {
        addReady(new AbstruseArchaic());
        harness.setHand(player1, List.of(new IchorWellspring()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(new Forest(), new Forest()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        UUID triggerId = gd.stack.stream()
                .filter(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .findFirst()
                .orElseThrow()
                .getCard()
                .getId();
        int handBeforeResolution = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, triggerId);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBeforeResolution + 2);
    }

    @Test
    @DisplayName("Cannot target an ability from a colored source")
    void cannotTargetColoredSourceAbility() {
        addReady(new AbstruseArchaic());
        harness.setHand(player1, List.of(new TrialOfZeal()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();
        UUID triggerId = gd.stack.stream()
                .filter(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .findFirst()
                .orElseThrow()
                .getCard()
                .getId();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, triggerId))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReady(com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
