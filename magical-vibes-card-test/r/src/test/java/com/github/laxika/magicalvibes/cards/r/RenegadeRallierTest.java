package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.Conviction;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.s.StoneGolem;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RenegadeRallier.class, GrizzlyBears.class, Forest.class, HolyDay.class,
        StoneGolem.class, Conviction.class})
class RenegadeRallierTest extends BaseCardTest {

    @Test
    @DisplayName("Revolt returns a target permanent with mana value 2 or less")
    void revoltReturnsTargetPermanent() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, permanent));
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));

        castRallier();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Revolt does not trigger when no permanent left the battlefield")
    void noRevoltWithoutPermanentLeaving() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));

        castRallier();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Revolt only targets permanents with mana value 2 or less")
    void filtersGraveyardTargets() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, permanent));
        Card valid = new GrizzlyBears();
        Card land = new Forest();
        Card nonPermanent = new HolyDay();
        Card tooExpensive = new StoneGolem();
        harness.setGraveyard(player1, List.of(valid, land, nonPermanent, tooExpensive));

        castRallier();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(valid.getId(), land.getId());
    }

    @Test
    @DisplayName("An opponent's permanent leaving the battlefield does not satisfy revolt")
    void opponentPermanentLeavingDoesNotSatisfyRevolt() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, permanent));
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        castRallier();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    @Test
    @DisplayName("Revolt returns a land untapped")
    void returnsLand() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, permanent));
        Card target = new Forest();
        harness.setGraveyard(player1, List.of(target));

        castRallier();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getId().equals(target.getId()))
                .findFirst().orElseThrow();
        assertThat(returned.isTapped()).isFalse();
        harness.assertNotInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Revolt cannot return a target that left the graveyard before resolution")
    void targetMustRemainInGraveyard() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, permanent));
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));

        castRallier();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Returning an Aura lets its controller choose what it enchants")
    void returnsAuraAttachedToChosenCreature() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, permanent));
        Card aura = new Conviction();
        harness.setGraveyard(player1, List.of(aura));

        castRallier();
        UUID rallierId = harness.getPermanentId(player1, "Renegade Rallier");
        harness.handleMultipleCardsChosen(player1, List.of(aura.getId()));
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(rallierId);
        harness.handlePermanentChosen(player1, rallierId);

        Permanent returnedAura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getId().equals(aura.getId()))
                .findFirst().orElseThrow();
        assertThat(returnedAura.getAttachedTo()).isEqualTo(rallierId);
        harness.assertNotInGraveyard(player1, "Conviction");
    }

    private void castRallier() {
        harness.castFromHand(player1, new RenegadeRallier(), "{1}{G}{W}");
        harness.passBothPriorities();
    }
}
