package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VerdantDread.class, Forest.class, GrizzlyBears.class})
class VerdantDreadTest extends BaseCardTest {

    @Test
    void enteringManifestsDread() {
        Card manifestedCard = new GrizzlyBears();
        Card graveyardCard = new Forest();
        harness.setLibrary(player1, List.of(manifestedCard, graveyardCard));
        harness.setHand(player1, List.of(new VerdantDread()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isManifested()
                        && permanent.getCard().getId().equals(manifestedCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCard);
    }

    @Test
    void anotherVerdantDreadAlsoTriggersManifestDread() {
        harness.addToBattlefieldAndReturn(player1, new VerdantDread());
        Card firstManifested = new GrizzlyBears();
        Card secondManifested = new GrizzlyBears();
        Card firstGraveyard = new Forest();
        Card secondGraveyard = new Forest();
        harness.setLibrary(player1, List.of(firstManifested, firstGraveyard, secondManifested, secondGraveyard));
        harness.setHand(player1, List.of(new VerdantDread()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(firstManifested.getId()));
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(secondManifested.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(Permanent::isManifested)
                .hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(firstGraveyard, secondGraveyard);
    }

    @Test
    void activationConjuresAnotherVerdantDreadOntoTheBattlefield() {
        harness.addToBattlefieldAndReturn(player1, new VerdantDread());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof VerdantDread)
                .hasSize(2);
    }
}
