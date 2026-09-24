package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.cards.m.MonkeyCage;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RishadanPawnshop.class, FreshVolunteers.class, MonkeyCage.class})
class RishadanPawnshopTest extends BaseCardTest {

    @Test
    @DisplayName("Shuffles a nontoken permanent you control into its owner's library")
    void shufflesControlledNontokenPermanentIntoOwnersLibrary() {
        Permanent pawnshop = harness.addToBattlefieldAndReturn(player1, new RishadanPawnshop());
        Permanent volunteers = harness.addToBattlefieldAndReturn(player1, new FreshVolunteers());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, volunteers.getId());
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        harness.assertNotOnBattlefield(player1, "Fresh Volunteers");
        harness.assertNotInGraveyard(player1, "Fresh Volunteers");
        assertThat(gameData.playerDecks.get(player1.getId()))
                .hasSize(deckSizeBefore + 1)
                .anyMatch(card -> card.getName().equals("Fresh Volunteers"));
        assertThat(pawnshop.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Shuffles a controlled permanent into its owner's library")
    void shufflesControlledPermanentIntoItsOwnersLibrary() {
        harness.addToBattlefield(player1, new RishadanPawnshop());
        Permanent stolenVolunteers = harness.addToBattlefieldAndReturn(player1, new FreshVolunteers());
        gd.stolenCreatures.put(stolenVolunteers.getId(), player2.getId());
        gd.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(),
                "Test control effect",
                null,
                player1.getId(),
                new GainControlOfTargetEffect(ControlDuration.PERMANENT),
                stolenVolunteers.getId(),
                null,
                null,
                EffectDuration.PERMANENT,
                0));
        int player1DeckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        int player2DeckSizeBefore = gd.playerDecks.get(player2.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, stolenVolunteers.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(player1DeckSizeBefore);
        assertThat(gd.playerDecks.get(player2.getId()))
                .hasSize(player2DeckSizeBefore + 1)
                .anyMatch(card -> card.getName().equals("Fresh Volunteers"));
    }

    @Test
    @DisplayName("Cannot target a token or a permanent controlled by an opponent")
    void rejectsTokenAndOpponentPermanent() {
        harness.addToBattlefield(player1, new RishadanPawnshop());
        harness.addToBattlefield(player1, new MonkeyCage());
        harness.castFromHand(player1, new FreshVolunteers(), "{1}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent monkey = findPermanent(player1, "Monkey");

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, monkey.getId()))
                .isInstanceOf(IllegalStateException.class);

        Permanent opponentPermanent = harness.addToBattlefieldAndReturn(player2, new FreshVolunteers());
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentPermanent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
