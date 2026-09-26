package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MassOfGhouls;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.v.VitalSurge;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({
        HorobisWhisper.class,
        Forest.class,
        GrizzlyBears.class,
        HillGiant.class,
        MassOfGhouls.class,
        Shock.class,
        Swamp.class,
        VitalSurge.class
})
class HorobisWhisperTest extends BaseCardTest {

    private void giveCastingMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Destroys the targeted nonblack creature while you control a Swamp")
    void destroysWithSwamp() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HorobisWhisper()));
        giveCastingMana();

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("The target survives when you control no Swamp")
    void doesNothingWithoutSwamp() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HorobisWhisper()));
        giveCastingMana();

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        assertThat(findPermanent(player2, "Grizzly Bears")).isNotNull();
    }

    @Test
    @DisplayName("An opponent's Swamp does not satisfy the condition")
    void opponentSwampDoesNotCount() {
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HorobisWhisper()));
        giveCastingMana();

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        assertThat(findPermanent(player2, "Grizzly Bears")).isNotNull();
    }

    @Test
    @DisplayName("Checks Swamp control when the spell resolves")
    void checksSwampAtResolution() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HorobisWhisper()));
        giveCastingMana();

        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearId);
        harness.addToBattlefield(player1, new Swamp());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new MassOfGhouls());
        harness.setHand(player1, List.of(new HorobisWhisper()));
        giveCastingMana();

        UUID ghoulsId = harness.getPermanentId(player2, "Mass of Ghouls");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, ghoulsId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new HorobisWhisper()));
        giveCastingMana();

        UUID forestId = harness.getPermanentId(player2, "Forest");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, forestId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Splices onto an Arcane spell by exiling four graveyard cards, staying in hand")
    void splicesOntoArcaneSpell() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new VitalSurge(), new HorobisWhisper()));
        harness.setGraveyard(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithSplice(player1, 0, harness.getPermanentId(player2, "Hill Giant"), List.of(1));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Vital Surge");
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(4);
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Horobi's Whisper");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot splice onto a non-Arcane spell")
    void cannotSpliceOntoNonArcaneSpell() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock(), new HorobisWhisper()));
        harness.setGraveyard(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castWithSplice(player1, 0, bearId, List.of(1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot splice with fewer than four cards in the graveyard")
    void cannotSpliceWithoutEnoughGraveyardCards() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new VitalSurge(), new HorobisWhisper()));
        harness.setGraveyard(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castWithSplice(player1, 0, bearId, List.of(1)))
                .isInstanceOf(IllegalStateException.class);
    }
}
