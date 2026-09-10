package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TabletOfEpityr.class, MindStone.class, Naturalize.class})
class TabletOfEpityrTest extends BaseCardTest {

    @Test
    @DisplayName("When your artifact is put into a graveyard, paying {1} gains 1 life")
    void ownArtifactTriggerPaysAndGainsLife() {
        harness.addToBattlefield(player1, new TabletOfEpityr());
        harness.addToBattlefield(player1, new MindStone());
        harness.setLife(player1, 20);

        destroyArtifact(player1, 3);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 21);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Declining the payment does not gain life")
    void decliningPaymentDoesNotGainLife() {
        harness.addToBattlefield(player1, new TabletOfEpityr());
        harness.addToBattlefield(player1, new MindStone());
        harness.setLife(player1, 20);

        destroyArtifact(player1, 2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 20);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);
    }

    @Test
    @DisplayName("An artifact controlled by an opponent does not trigger Tablet of Epityr")
    void opponentArtifactDoesNotTrigger() {
        harness.addToBattlefield(player1, new TabletOfEpityr());
        harness.addToBattlefield(player2, new MindStone());
        harness.addMana(player1, ManaColor.GREEN, 2);

        destroyArtifact(player2, 2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void destroyArtifact(com.github.laxika.magicalvibes.model.Player artifactController, int mana) {
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, mana);
        harness.castInstant(player1, 0, harness.getPermanentId(artifactController, "Mind Stone"));
    }
}
