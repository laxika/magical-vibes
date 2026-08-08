package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MassOfGhouls;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

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

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Grizzly Bears")).isNotNull();
    }

    @Test
    @DisplayName("An opponent's Swamp does not satisfy the condition")
    void opponentSwampDoesNotCount() {
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HorobisWhisper()));
        giveCastingMana();

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Grizzly Bears")).isNotNull();
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
    @DisplayName("Splices onto an Arcane spell by exiling four graveyard cards, staying in hand")
    void splicesOntoArcaneSpell() {
        Card arcaneShock = new Shock().createRuntimeCopy();
        arcaneShock.setSubtypes(List.of(CardSubtype.ARCANE));
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(arcaneShock, new HorobisWhisper()));
        harness.setGraveyard(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castWithSplice(player1, 0, harness.getPermanentId(player2, "Hill Giant"), List.of(1));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Shock");
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(4);
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Horobi's Whisper");
        // The 3/3 survives Shock's 2 damage, so its death is the spliced destroy.
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot splice with fewer than four cards in the graveyard")
    void cannotSpliceWithoutEnoughGraveyardCards() {
        Card arcaneShock = new Shock().createRuntimeCopy();
        arcaneShock.setSubtypes(List.of(CardSubtype.ARCANE));
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(arcaneShock, new HorobisWhisper()));
        harness.setGraveyard(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castWithSplice(player1, 0, bearId, List.of(1)))
                .isInstanceOf(IllegalStateException.class);
    }
}
