package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CateranPersuader.class, CateranBrute.class, CateranSummons.class, CacklingWitch.class})
class CateranPersuaderTest extends BaseCardTest {

    @Test
    void doesNotFindMercenaryPermanentWithManaValueAboveOne() {
        Permanent persuader = addCreatureReady(player1, new CateranPersuader());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.setLibrary(player1, List.of(new CateranBrute(), new CateranSummons(), new CacklingWitch()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNull();
        assertThat(persuader.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Cateran Brute");
        harness.assertNotOnBattlefield(player1, "Cateran Summons");
        harness.assertNotOnBattlefield(player1, "Cackling Witch");
    }

    @Test
    void cannotActivateWithoutGenericMana() {
        Permanent persuader = addCreatureReady(player1, new CateranPersuader());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(persuader.isTapped()).isFalse();
    }
}
