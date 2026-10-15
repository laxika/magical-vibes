package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlawlessForgery.class, CounselOfTheSoratami.class, AirElemental.class, Forest.class,
        GrizzlyBears.class, Shock.class})
class FlawlessForgeryTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles an opponent's instant or sorcery and casts its copy for free")
    void exilesAndCastsOpponentGraveyardCopy() {
        CounselOfTheSoratami target = new CounselOfTheSoratami();
        harness.setGraveyard(player2, List.of(target));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new FlawlessForgery()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, target.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Casualty 3 copies the spell and permits a new graveyard target")
    void casualtyCopiesAndRetargets() {
        CounselOfTheSoratami first = new CounselOfTheSoratami();
        CounselOfTheSoratami second = new CounselOfTheSoratami();
        harness.setGraveyard(player2, List.of(first, second));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new FlawlessForgery()));
        Permanent casualtyCreature = addCreatureReady(player1, new AirElemental());
        addMana();

        harness.castSorceryWithSacrifice(player1, 0, first.getId(), casualtyCreature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(casualtyCreature.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactlyInAnyOrder(first, second);
    }

    @Test
    void onlyTargetsInstantOrSorceryCardsInAnOpponentsGraveyard() {
        CounselOfTheSoratami ownTarget = new CounselOfTheSoratami();
        harness.setGraveyard(player1, List.of(ownTarget));
        harness.setHand(player1, List.of(new FlawlessForgery()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, ownTarget.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsCreatureCardInOpponentsGraveyard() {
        GrizzlyBears target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new FlawlessForgery()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void decliningCopyStillExilesOriginalCard() {
        CounselOfTheSoratami target = new CounselOfTheSoratami();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new FlawlessForgery()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, target.getId());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(target);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void castsTargetedInstantCopyForFree() {
        Shock target = new Shock();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new FlawlessForgery()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, target.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(target);
    }

    @Test
    void casualtyCopyKeepingOriginalTargetMakesOriginalSpellFizzle() {
        CounselOfTheSoratami target = new CounselOfTheSoratami();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new FlawlessForgery()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        Permanent sacrifice = addCreatureReady(player1, new AirElemental());
        addMana();

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(target);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void casualtyRejectsCreatureWithPowerBelowThree() {
        CounselOfTheSoratami target = new CounselOfTheSoratami();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new FlawlessForgery()));
        Permanent sacrifice = addCreatureReady(player1, new GrizzlyBears());
        addMana();

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(
                player1, 0, target.getId(), sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sacrifice);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(target);
        assertThat(gd.stack).isEmpty();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
