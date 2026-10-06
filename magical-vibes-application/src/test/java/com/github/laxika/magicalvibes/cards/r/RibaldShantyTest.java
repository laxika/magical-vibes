package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RibaldShanty.class, HillGiant.class, Plains.class, ChandraNalaar.class})
class RibaldShantyTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to its starting intensity")
    void dealsDamageEqualToStartingIntensity() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        RibaldShanty shanty = new RibaldShanty();
        harness.setHand(player1, List.of(shanty));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.getCardIntensity(shanty.getId())).isEqualTo(3);
    }

    @Test
    @DisplayName("Intensifies all owned Chorus cards")
    void intensifiesOwnedChorusCards() {
        RibaldShanty shanty = new RibaldShanty();
        RibaldShanty otherShanty = new RibaldShanty();
        RibaldShanty opponentShanty = new RibaldShanty();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(shanty, otherShanty));
        harness.setLibrary(player2, List.of(opponentShanty));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.getCardIntensity(shanty.getId())).isEqualTo(3);
        assertThat(gd.getCardIntensity(otherShanty.getId())).isEqualTo(3);
        assertThat(gd.getCardIntensity(opponentShanty.getId())).isZero();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void rejectsNonCreatureTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new RibaldShanty()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A previously intensified uncast copy deals three damage")
    void previouslyIntensifiedCopyDealsThreeDamage() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        RibaldShanty first = new RibaldShanty();
        RibaldShanty second = new RibaldShanty();
        harness.setHand(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, firstTarget.getId());
        harness.castAndResolveInstant(player1, 0, secondTarget.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(secondTarget);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(secondTarget.getCard());
        assertThat(gd.getCardIntensity(first.getId())).isEqualTo(4);
        assertThat(gd.getCardIntensity(second.getId())).isEqualTo(4);
    }

    @Test
    @DisplayName("Intensifies owned Chorus cards in library, graveyard and exile")
    void intensifiesCardsAcrossZones() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        RibaldShanty libraryCard = new RibaldShanty();
        RibaldShanty graveyardCard = new RibaldShanty();
        RibaldShanty exiledCard = new RibaldShanty();
        RibaldShanty opponentCard = new RibaldShanty();
        Plains nonChorus = new Plains();
        harness.setHand(player1, List.of(new RibaldShanty(), nonChorus));
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setExile(player1, List.of(exiledCard));
        harness.setExile(player2, List.of(opponentCard));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.getCardIntensity(libraryCard.getId())).isEqualTo(3);
        assertThat(gd.getCardIntensity(graveyardCard.getId())).isEqualTo(3);
        assertThat(gd.getCardIntensity(exiledCard.getId())).isEqualTo(3);
        assertThat(gd.getCardIntensity(opponentCard.getId())).isZero();
        assertThat(gd.getCardIntensity(nonChorus.getId())).isZero();
    }

    @Test
    @DisplayName("An owned Chorus spell on the stack intensifies before its damage resolves")
    void intensifiesSpellOnStack() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new RibaldShanty(), new RibaldShanty()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, firstTarget.getId());
        harness.castAndResolveInstant(player1, 0, secondTarget.getId());
        resolveAllTriggers();

        assertThat(secondTarget.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(firstTarget);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(firstTarget.getCard());
    }

    @Test
    @DisplayName("Can damage a planeswalker")
    void damagesPlaneswalker() {
        Permanent target = harness.enterBattlefieldAndReturn(player2, new ChandraNalaar());
        harness.setHand(player1, List.of(new RibaldShanty()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("Can target a creature controlled by its caster")
    void canTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new RibaldShanty()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a player")
    void rejectsPlayerTarget() {
        harness.setHand(player1, List.of(new RibaldShanty()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not intensify Chorus cards when its only target leaves the battlefield")
    void illegalTargetPreventsIntensification() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        RibaldShanty shanty = new RibaldShanty();
        RibaldShanty otherShanty = new RibaldShanty();
        harness.setHand(player1, List.of(shanty, otherShanty));
        harness.addMana(player1, ManaColor.RED, 1);

        int intensityBefore = gd.getCardIntensity(otherShanty);
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(gd.getCardIntensity(otherShanty)).isEqualTo(intensityBefore);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shanty);
    }
}
