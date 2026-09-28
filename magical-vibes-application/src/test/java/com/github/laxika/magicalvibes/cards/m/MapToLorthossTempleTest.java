package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.l.LorthosTheTidemaker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MapToLorthossTemple.class, DarksteelCitadel.class, CoralMerfolk.class,
        DarkRitual.class, LorthosTheTidemaker.class})
class MapToLorthossTempleTest extends BaseCardTest {

    @Test
    void checksArtifactMerfolkAndInstantOrSorceryObjectivesThenCreatesLorthos() {
        Permanent map = harness.addToBattlefieldAndReturn(player1, new MapToLorthossTemple());

        harness.enterBattlefieldAndReturn(player1, new DarksteelCitadel());
        harness.passBothPriorities();
        assertThat(map.getChosenModeLabels()).contains("Diving Gear");

        harness.enterBattlefieldAndReturn(player1, new CoralMerfolk());
        harness.passBothPriorities();
        assertThat(map.getChosenModeLabels()).contains("Merfolk");

        harness.setHand(player1, List.of(new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(map);
        assertThat(findPermanents(player1, "Lorthos, the Tidemaker")).singleElement()
                .satisfies(token -> assertThat(token.getCard().isToken()).isTrue());
    }
}
