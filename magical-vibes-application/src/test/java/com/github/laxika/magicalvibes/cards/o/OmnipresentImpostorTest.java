package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SakuraTribeElder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OmnipresentImpostor.class, SakuraTribeElder.class, Forest.class})
class OmnipresentImpostorTest extends BaseCardTest {

    @Test
    void mayBeChosenInPlaceOfARestrictedBasicLandSearch() {
        harness.addToBattlefieldAndReturn(player1, new SakuraTribeElder());
        Card impostor = new OmnipresentImpostor();
        harness.setLibrary(player1, List.of(impostor, new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).contains(impostor);

        harness.handleCardChosen(player1, search.params().cards().indexOf(impostor));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(impostor.getId()) && permanent.isTapped());
    }
}
