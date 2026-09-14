package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.ArmoredGriffin;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LastStand.class, ArmoredGriffin.class, Forest.class, Island.class, Mountain.class,
        Plains.class, Swamp.class})
class LastStandTest extends BaseCardTest {

    @Test
    @DisplayName("Uses controlled basic land counts for each effect")
    void resolvesAllLandCountedEffects() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Plains());
        harness.addToBattlefield(player2, new Island());
        Permanent griffin = harness.addToBattlefieldAndReturn(player2, new ArmoredGriffin());
        harness.setHand(player1, List.of(new LastStand()));
        harness.setLibrary(player1, List.of(new Swamp(), new Mountain(), new Forest()));
        addLastStandMana();

        harness.castSorcery(player1, 0, List.of(player2.getId(), griffin.getId()));
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        assertThat(findPermanents(player1, "Saproling")).hasSize(2);
        harness.assertLife(player1, 22);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(2);

        harness.handleCardChosen(player1, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(1);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        harness.assertNotOnBattlefield(player2, "Armored Griffin");
    }

    @Test
    @DisplayName("Counts zero controlled land types without prompting for discard")
    void resolvesWithNoControlledBasicLands() {
        Permanent griffin = harness.addToBattlefieldAndReturn(player2, new ArmoredGriffin());
        harness.setHand(player1, List.of(new LastStand()));
        addLastStandMana();

        harness.castSorcery(player1, 0, List.of(player2.getId(), griffin.getId()));
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertLife(player1, 20);
        assertThat(findPermanents(player1, "Saproling")).isEmpty();
        assertThat(griffin.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Armored Griffin");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Requires the first target to be an opponent")
    void rejectsControllerAsOpponentTarget() {
        Permanent griffin = harness.addToBattlefieldAndReturn(player2, new ArmoredGriffin());
        harness.setHand(player1, List.of(new LastStand()));
        addLastStandMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(player1.getId(), griffin.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    @DisplayName("Requires the second target to be a creature")
    void rejectsNonCreatureTarget() {
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new LastStand()));
        addLastStandMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(player2.getId(), plains.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    private void addLastStandMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
