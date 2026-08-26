package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.d.DelugeOfTheDead;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InvasionOfInnistrad;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TheCore;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MatzalantliTheGreatDoor.class, TheCore.class, DarksteelRelic.class, DelugeOfTheDead.class,
        Forest.class, GloriousAnthem.class, GrizzlyBears.class, InvasionOfInnistrad.class,
        JaceBeleren.class, Shock.class})
class MatzalantliTheGreatDoorTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card and then prompts for a discard")
    void drawsThenDiscards() {
        Permanent door = addReadyDoor();
        harness.setHand(player1, List.of(new Forest()));
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new Forest());
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(door.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("Transforms when the graveyard has four distinct permanent types")
    void transformsAtFourPermanentTypes() {
        Permanent door = addReadyDoor();
        harness.setGraveyard(player1, List.of(
                new DarksteelRelic(), new GrizzlyBears(), new GloriousAnthem(),
                new Forest(), new InvasionOfInnistrad(), new JaceBeleren()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(door.isTransformed()).isTrue();
        assertThat(door.isTapped()).isTrue();
        assertThat(door.getCard()).isInstanceOf(TheCore.class);
    }

    @Test
    @DisplayName("Does not activate with only three permanent types plus instants")
    void doesNotActivateBelowFourPermanentTypes() {
        Permanent door = addReadyDoor();
        harness.setGraveyard(player1, List.of(
                new DarksteelRelic(), new GrizzlyBears(), new GloriousAnthem(), new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("four or more permanent types");

        assertThat(door.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);
    }

    @Test
    @DisplayName("The Core adds mana equal to the number of permanent cards in the graveyard")
    void coreAddsManaForPermanentCards() {
        Permanent core = addTransformedDoor();
        harness.setGraveyard(player1, List.of(
                new DarksteelRelic(), new GrizzlyBears(), new GloriousAnthem(), new Forest(), new Shock()));

        harness.activateAbility(player1, battlefieldIndex(core), 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(4);
    }

    private Permanent addReadyDoor() {
        Permanent door = new Permanent(new MatzalantliTheGreatDoor());
        door.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(door);
        return door;
    }

    private Permanent addTransformedDoor() {
        MatzalantliTheGreatDoor card = new MatzalantliTheGreatDoor();
        Permanent door = new Permanent(card);
        door.setSummoningSick(false);
        door.setCard(card.getBackFaceCard());
        door.setTransformed(true);
        gd.playerBattlefields.get(player1.getId()).add(door);
        return door;
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
