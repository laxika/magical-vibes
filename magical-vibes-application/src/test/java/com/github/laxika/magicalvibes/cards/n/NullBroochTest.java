package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.s.Skyshaper;
import com.github.laxika.magicalvibes.cards.w.WhiptongueFrog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NullBrooch.class, Skyshaper.class, WhiptongueFrog.class})
class NullBroochTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a noncreature spell and discards its controller's hand")
    void countersNoncreatureSpellAndDiscardsHand() {
        Permanent brooch = harness.addToBattlefieldAndReturn(player1, new NullBrooch());
        Skyshaper skyshaper = new Skyshaper();
        WhiptongueFrog discardedFrog = new WhiptongueFrog();
        WhiptongueFrog otherDiscardedFrog = new WhiptongueFrog();
        harness.setHand(player1, List.of(discardedFrog, otherDiscardedFrog));
        harness.setHand(player2, List.of(skyshaper));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player2);
        harness.castArtifact(player2, 0);
        harness.passPriority(player2);
        harness.activateAbility(player1, 0, null, skyshaper.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(card -> card.getId())
                .containsExactly(skyshaper.getId());
        assertThat(brooch.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getId())
                .containsExactlyInAnyOrder(discardedFrog.getId(), otherDiscardedFrog.getId());
    }

    @Test
    @DisplayName("Cannot target a creature spell")
    void cannotTargetCreatureSpell() {
        Permanent brooch = harness.addToBattlefieldAndReturn(player1, new NullBrooch());
        WhiptongueFrog frog = new WhiptongueFrog();
        harness.setHand(player2, List.of(frog));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, frog.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(brooch.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target an activated ability")
    void cannotTargetActivatedAbility() {
        Permanent brooch = harness.addToBattlefieldAndReturn(player1, new NullBrooch());
        Skyshaper skyshaper = new Skyshaper();
        harness.addToBattlefield(player2, skyshaper);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, null);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, skyshaper.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(brooch.isTapped()).isFalse();
    }
}
