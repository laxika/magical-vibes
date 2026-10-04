package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HuntedBonebrute.class, Shock.class})
class HuntedBonebruteTest extends BaseCardTest {

    @Test
    @DisplayName("Hunted Bonebrute's ETB only offers opponents as targets")
    void etbTargetsOpponent() {
        castBonebrute();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactly(player2.getId());
    }

    @Test
    @DisplayName("Hunted Bonebrute's ETB gives the targeted opponent two Dogs")
    void etbCreatesDogsForTargetedOpponent() {
        castBonebrute();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        List<Permanent> dogs = findPermanents(player2, "Dog");
        assertThat(dogs).hasSize(2);
        assertThat(dogs).allSatisfy(dog -> {
            assertThat(dog.getCard().isToken()).isTrue();
            assertThat(dog.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(dog.getCard().getSubtypes()).contains(CardSubtype.DOG);
            assertThat(dog.getEffectivePower()).isEqualTo(1);
            assertThat(dog.getEffectiveToughness()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("When Hunted Bonebrute dies, each opponent loses 3 life")
    void deathTriggerMakesEachOpponentLoseLife() {
        castBonebrute();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        UUID bonebruteId = harness.getPermanentId(player1, "Hunted Bonebrute");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, bonebruteId);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Disguise does not create Dogs, and dying face down does not cause life loss")
    void faceDownEntryAndDeathHaveNoPrintedTriggers() {
        Permanent bonebrute = castDisguisedBonebrute();
        assertThat(bonebrute.isFaceDown()).isTrue();
        assertThat(findPermanents(player2, "Dog")).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, bonebrute.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bonebrute);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof HuntedBonebrute);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Turning face up does not create Dogs, but restores the death trigger")
    void turningFaceUpRestoresDeathTriggerWithoutEtb() {
        Permanent bonebrute = castDisguisedBonebrute();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(bonebrute));
        resolveAllTriggers();

        assertThat(bonebrute.isFaceDown()).isFalse();
        assertThat(findPermanents(player2, "Dog")).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, bonebrute.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bonebrute);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Menace rejects one Dog blocker and allows two")
    void menaceRequiresTwoDogBlockers() {
        castBonebrute();
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();
        Permanent bonebrute = gd.playerBattlefields.get(player1.getId()).getFirst();
        bonebrute.setSummoningSick(false);
        List<Permanent> dogs = findPermanents(player2, "Dog");
        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        assertThat(dogs).allSatisfy(dog -> assertThat(dog.isBlocking()).isTrue());
    }

    private Permanent castDisguisedBonebrute() {
        harness.setHand(player1, List.of(new HuntedBonebrute()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }

    private void castBonebrute() {
        harness.setHand(player1, List.of(new HuntedBonebrute()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
