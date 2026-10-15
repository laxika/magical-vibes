package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.n.NyxbornRollicker;
import com.github.laxika.magicalvibes.cards.s.SatyrWayfinder;
import com.github.laxika.magicalvibes.cards.s.SpringleafDrum;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GlimpseTheSunGod.class, NyxbornRollicker.class, SatyrWayfinder.class, SpringleafDrum.class})
class GlimpseTheSunGodTest extends BaseCardTest {

    @Test
    @DisplayName("Taps X target creatures and then scries 1")
    void tapsTargetCreaturesAndScries() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new NyxbornRollicker());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new SatyrWayfinder());
        Card topCard = new SpringleafDrum();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new GlimpseTheSunGod()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstantForX(player1, 0, 2, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(topCard);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Scry 1 can put the top card on the bottom of the library")
    void scriesToBottom() {
        Card topCard = new SpringleafDrum();
        Card nextCard = new SpringleafDrum();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SatyrWayfinder());
        harness.setHand(player1, List.of(new GlimpseTheSunGod()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstantForX(player1, 0, 1, List.of(target.getId()));
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard, topCard);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new SpringleafDrum());
        harness.setHand(player1, List.of(new GlimpseTheSunGod()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        UUID artifactId = harness.getPermanentId(player2, "Springleaf Drum");

        assertThatThrownBy(() -> harness.castInstantForX(player1, 0, 1, List.of(artifactId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    void zeroXScriesWithoutTargets() {
        Card topCard = new SpringleafDrum();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new GlimpseTheSunGod()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstantForX(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(topCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.assertInGraveyard(player1, "Glimpse the Sun God");
    }

    @Test
    void cannotChooseFewerTargetsThanX() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SatyrWayfinder());
        harness.addToBattlefield(player2, new NyxbornRollicker());
        harness.setHand(player1, List.of(new GlimpseTheSunGod()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castInstantForX(player1, 0, 2, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotChooseMoreTargetsThanX() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new SatyrWayfinder());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new NyxbornRollicker());
        harness.setHand(player1, List.of(new GlimpseTheSunGod()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstantForX(player1, 0, 1, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void remainingLegalTargetIsTappedAndScryStillHappens() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new SatyrWayfinder());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new NyxbornRollicker());
        Card topCard = new SpringleafDrum();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new GlimpseTheSunGod()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstantForX(player1, 0, 2, List.of(first.getId(), second.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(first);
        harness.passBothPriorities();

        assertThat(second.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(topCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
    }

    @Test
    void allTargetsGonePreventsScry() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SatyrWayfinder());
        Card topCard = new SpringleafDrum();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new GlimpseTheSunGod()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstantForX(player1, 0, 1, List.of(target.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        harness.assertInGraveyard(player1, "Glimpse the Sun God");
    }

    @Test
    void canTargetOwnAlreadyTappedCreatureWithEmptyLibrary() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SatyrWayfinder());
        target.tap();
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new GlimpseTheSunGod()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstantForX(player1, 0, 1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        harness.assertInGraveyard(player1, "Glimpse the Sun God");
    }
}
