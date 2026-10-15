package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.l.LorthosTheTidemaker;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.p.Ponder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MapToLorthossTemple.class, DarksteelCitadel.class, CoralMerfolk.class,
        DarkRitual.class, LorthosTheTidemaker.class, Naturalize.class, Ponder.class})
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

        harness.castFromHand(player1, new DarkRitual(), "{B}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(map);
        assertThat(findPermanents(player1, "Lorthos, the Tidemaker")).singleElement()
                .satisfies(token -> assertThat(token.getCard().isToken()).isTrue());
    }

    @Test
    void repeatedArtifactsDoNotReplaceTheOtherObjectives() {
        Permanent map = harness.addToBattlefieldAndReturn(player1, new MapToLorthossTemple());

        for (int i = 0; i < 3; i++) {
            harness.enterBattlefieldAndReturn(player1, new DarksteelCitadel());
            resolveAllTriggers();
        }

        assertThat(map.getChosenModeLabels()).containsExactly("Diving Gear");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(map);
        assertThat(findPermanents(player1, "Lorthos, the Tidemaker")).isEmpty();
    }

    @Test
    void opponentsActionsDoNotCompleteObjectives() {
        Permanent map = harness.addToBattlefieldAndReturn(player1, new MapToLorthossTemple());

        harness.enterBattlefieldAndReturn(player2, new DarksteelCitadel());
        resolveAllTriggers();
        harness.enterBattlefieldAndReturn(player2, new CoralMerfolk());
        resolveAllTriggers();
        harness.castFromHand(player2, new DarkRitual(), "{B}");
        resolveAllTriggers();

        assertThat(map.getChosenModeLabels()).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(map);
        assertThat(findPermanents(player1, "Lorthos, the Tidemaker")).isEmpty();
    }

    @Test
    void sorceryCompletesRitualBeforeTheSpellResolves() {
        Permanent map = harness.addToBattlefieldAndReturn(player1, new MapToLorthossTemple());
        harness.enterBattlefieldAndReturn(player1, new DarksteelCitadel());
        resolveAllTriggers();
        harness.enterBattlefieldAndReturn(player1, new CoralMerfolk());
        resolveAllTriggers();

        Ponder ponder = new Ponder();
        harness.castFromHand(player1, ponder, "{U}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(map);
        assertThat(findPermanents(player1, "Lorthos, the Tidemaker")).hasSize(1);
        assertThat(gd.stack).anySatisfy(entry -> assertThat(entry.getCard()).isSameAs(ponder));
    }

    @Test
    void noTokenIsCreatedWhenMapLeavesBeforeTheFinalObjectiveResolves() {
        Permanent map = harness.addToBattlefieldAndReturn(player1, new MapToLorthossTemple());
        harness.enterBattlefieldAndReturn(player1, new DarksteelCitadel());
        resolveAllTriggers();
        harness.enterBattlefieldAndReturn(player1, new CoralMerfolk());
        resolveAllTriggers();
        harness.castFromHand(player1, new DarkRitual(), "{B}");

        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, map.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(map);
        assertThat(findPermanents(player1, "Lorthos, the Tidemaker")).isEmpty();
    }

    @Test
    void newControllerCanCompleteMapAfterPreviousControllerCannotSacrificeIt() {
        Permanent map = harness.addToBattlefieldAndReturn(player1, new MapToLorthossTemple());
        harness.enterBattlefieldAndReturn(player1, new DarksteelCitadel());
        resolveAllTriggers();
        harness.enterBattlefieldAndReturn(player1, new CoralMerfolk());
        resolveAllTriggers();
        harness.castFromHand(player1, new DarkRitual(), "{B}");

        gd.playerBattlefields.get(player1.getId()).remove(map);
        gd.playerBattlefields.get(player2.getId()).add(map);
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(map);
        assertThat(findPermanents(player1, "Lorthos, the Tidemaker")).isEmpty();

        harness.castFromHand(player2, new DarkRitual(), "{B}");
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(map);
        assertThat(findPermanents(player2, "Lorthos, the Tidemaker")).hasSize(1);
    }
}
