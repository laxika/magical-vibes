package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.e.EmrakulTheAeonsTorn;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LayBare.class, GlorySeeker.class, EmrakulTheAeonsTorn.class})
class LayBareTest extends BaseCardTest {

    @Test
    @DisplayName("Counters the spell and looks at its controller's hand")
    void countersSpellAndLooksAtControllerHand() {
        GlorySeeker seeker = new GlorySeeker();
        harness.setHand(player1, List.of(seeker, new GlorySeeker()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.setHand(player2, List.of(new LayBare()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, seeker.getId());

        harness.assertInGraveyard(player1, "Glory Seeker");
        harness.assertInGraveyard(player2, "Lay Bare");
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Glory Seeker"));
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("looks at") && log.contains("hand"));
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        GlorySeeker seeker = new GlorySeeker();
        harness.addToBattlefield(player1, seeker);

        harness.setHand(player2, List.of(new LayBare()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() ->
                harness.castInstant(player2, 0, harness.getPermanentId(player1, "Glory Seeker")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Only the caster sees the spell controller's remaining hand")
    void handInformationIsPrivate() {
        GlorySeeker spell = new GlorySeeker();
        GlorySeeker remaining = new GlorySeeker();
        harness.setHand(player1, List.of(spell, remaining));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setHand(player2, List.of(new LayBare()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, spell.getId());

        assertThat(harness.getConn2().getMessagesContaining("REVEAL_HAND"))
                .anyMatch(message -> message.contains(remaining.getId().toString()))
                .noneMatch(message -> message.contains(spell.getId().toString()));
        assertThat(harness.getConn1().getMessagesContaining("REVEAL_HAND")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(remaining);
        harness.assertInGraveyard(player1, "Glory Seeker");
    }

    @Test
    @DisplayName("Still counters a spell whose controller has an empty hand")
    void emptyHandDoesNotPreventCountering() {
        GlorySeeker spell = new GlorySeeker();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setHand(player2, List.of(new LayBare()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, spell.getId());

        harness.assertInGraveyard(player1, "Glory Seeker");
        harness.assertInGraveyard(player2, "Lay Bare");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("looks at") && log.contains("empty"));
    }

    @Test
    @DisplayName("Looks at the hand even when the targeted spell cannot be countered")
    void uncounterableSpellStillRevealsHand() {
        EmrakulTheAeonsTorn spell = new EmrakulTheAeonsTorn();
        GlorySeeker remaining = new GlorySeeker();
        harness.setHand(player1, List.of(spell, remaining));
        harness.addMana(player1, ManaColor.COLORLESS, 15);
        harness.setHand(player2, List.of(new LayBare()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, spell.getId());

        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == spell);
        harness.assertNotInGraveyard(player1, "Emrakul, the Aeons Torn");
        harness.assertInGraveyard(player2, "Lay Bare");
        assertThat(harness.getConn2().getMessagesContaining("REVEAL_HAND"))
                .anyMatch(message -> message.contains(remaining.getId().toString()));
        assertThat(harness.getConn1().getMessagesContaining("REVEAL_HAND")).isEmpty();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Emrakul, the Aeons Torn");
    }

    @Test
    @DisplayName("Does not look at a hand when the targeted spell has left the stack")
    void missingTargetPreventsHandLook() {
        GlorySeeker spell = new GlorySeeker();
        harness.setHand(player1, List.of(spell, new LayBare(), new GlorySeeker()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.setHand(player2, List.of(new LayBare()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, spell.getId());
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, spell.getId());
        harness.clearMessages();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Glory Seeker");
        harness.assertInGraveyard(player2, "Lay Bare");
        assertThat(harness.getConn2().getMessagesContaining("REVEAL_HAND")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
