package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PersistentPetitionersTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability mills one card and taps Persistent Petitioners")
    void firstAbilityMillsOneCard() {
        Permanent petitioners = addReadyPetitioners();
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(topCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(petitioners.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Tapping four Advisors mills twelve cards")
    void fourAdvisorsMillTwelveCards() {
        Permanent petitioners = harness.addToBattlefieldAndReturn(player1, new PersistentPetitioners());
        List<Permanent> advisors = new ArrayList<>(List.of(petitioners));
        for (int i = 0; i < 3; i++) {
            advisors.add(harness.addToBattlefieldAndReturn(player1, new PersistentPetitioners()));
        }

        List<Card> library = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            library.add(new GrizzlyBears());
        }
        harness.setLibrary(player2, library);
        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(petitioners);

        harness.activateAbility(player1, sourceIndex, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(12);
        assertThat(advisors).allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("The twelve-card ability cannot be activated without four Advisors")
    void cannotActivateWithoutFourAdvisors() {
        harness.addToBattlefield(player1, new PersistentPetitioners());
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new GrizzlyBears());
        }

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyPetitioners() {
        Permanent petitioners = harness.addToBattlefieldAndReturn(player1, new PersistentPetitioners());
        petitioners.setSummoningSick(false);
        return petitioners;
    }
}
