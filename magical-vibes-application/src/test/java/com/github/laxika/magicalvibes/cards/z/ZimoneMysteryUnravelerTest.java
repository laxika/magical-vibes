package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.c.Cultivate;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SongOfTheDryads;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZimoneMysteryUnraveler.class, Forest.class, GrizzlyBears.class,
        Cultivate.class, SongOfTheDryads.class})
class ZimoneMysteryUnravelerTest extends BaseCardTest {

    @Test
    void firstLandfallManifestsDread() {
        Card manifestedCard = new GrizzlyBears();
        Card graveyardCard = new Forest();
        harness.addToBattlefield(player1, new ZimoneMysteryUnraveler());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(manifestedCard, graveyardCard));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class))
                .isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isManifested()
                        && permanent.getCard().getId().equals(manifestedCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCard);
    }

    @Test
    void laterLandfallMayTurnAControlledPermanentFaceUp() {
        Card manifestedCard = new GrizzlyBears();
        Card graveyardCard = new Forest();
        harness.addToBattlefield(player1, new ZimoneMysteryUnraveler());
        harness.setLibrary(player1, List.of(manifestedCard, graveyardCard));
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        Permanent manifestedPermanent = gqs.findPermanentById(gd,
                harness.getPermanentId(player1, manifestedCard.getName()));
        gd.landsPlayedThisTurn.put(player1.getId(), 0);
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(manifestedPermanent.isFaceDown()).isFalse();
    }

    @Test
    void queuedLandfallTriggersUseResolutionOrder() {
        Card manifestedCard = new GrizzlyBears();
        harness.addToBattlefield(player1, new ZimoneMysteryUnraveler());
        harness.setLibrary(player1, List.of(manifestedCard, new Forest()));

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.enterBattlefieldAndReturn(player1, new Forest());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        Permanent manifestedPermanent = gqs.findPermanentById(gd,
                harness.getPermanentId(player1, manifestedCard.getName()));
        assertThat(manifestedPermanent.isFaceDown()).isTrue();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(manifestedPermanent.isFaceDown()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void laterLandfallMayBeDeclined() {
        Card manifestedCard = new GrizzlyBears();
        harness.addToBattlefield(player1, new ZimoneMysteryUnraveler());
        harness.setLibrary(player1, List.of(manifestedCard, new Forest()));
        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));
        Permanent manifestedPermanent = gqs.findPermanentById(gd,
                harness.getPermanentId(player1, manifestedCard.getName()));

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(manifestedPermanent.isFaceDown()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void manifestedLandCanBeTurnedFaceUpWithoutTriggeringLandfall() {
        Card manifestedCard = new Forest();
        harness.addToBattlefield(player1, new ZimoneMysteryUnraveler());
        harness.setLibrary(player1, List.of(manifestedCard, new GrizzlyBears()));
        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));
        Permanent manifestedPermanent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(manifestedCard.getId()))
                .findFirst().orElseThrow();

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(manifestedPermanent.isFaceDown()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void manifestedSorceryRemainsFaceDownWhenChosen() {
        Card manifestedCard = new Cultivate();
        harness.addToBattlefield(player1, new ZimoneMysteryUnraveler());
        harness.setLibrary(player1, List.of(manifestedCard, new Forest()));
        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));
        Permanent manifestedPermanent = gqs.findPermanentById(gd,
                harness.getPermanentId(player1, manifestedCard.getName()));

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(manifestedPermanent.isFaceDown()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(manifestedPermanent);
        assertThat(gd.permanentsTurnedFaceUpThisTurn).doesNotContain(manifestedPermanent.getId());
    }

    @Test
    void faceDownPermanentThatIsCurrentlyOnlyALandCanBeTurnedFaceUp() {
        Card manifestedCard = new GrizzlyBears();
        harness.addToBattlefield(player1, new ZimoneMysteryUnraveler());
        harness.setLibrary(player1, List.of(manifestedCard, new Forest()));
        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));
        Permanent manifestedPermanent = gqs.findPermanentById(gd,
                harness.getPermanentId(player1, manifestedCard.getName()));

        harness.setHand(player1, List.of(new SongOfTheDryads()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, manifestedPermanent.getId());
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, manifestedPermanent)).isFalse();

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(manifestedPermanent.isFaceDown()).isFalse();
        assertThat(gqs.isLand(gd, manifestedPermanent)).isTrue();
    }

    @Test
    void opponentsLandDoesNotTriggerTheAbility() {
        harness.addToBattlefield(player1, new ZimoneMysteryUnraveler());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest()));

        harness.enterBattlefieldAndReturn(player2, new Forest());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    void manifestDreadWorksWithOneCardInLibrary() {
        Card manifestedCard = new GrizzlyBears();
        harness.addToBattlefield(player1, new ZimoneMysteryUnraveler());
        harness.setLibrary(player1, List.of(manifestedCard));

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        Permanent manifestedPermanent = gqs.findPermanentById(gd,
                harness.getPermanentId(player1, manifestedCard.getName()));
        assertThat(manifestedPermanent.isFaceDown()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

}
