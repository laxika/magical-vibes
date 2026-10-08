package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GildedLotus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WKabiShieldOfTheNation.class, GildedLotus.class, GrizzlyBears.class, SolRing.class})
class WKabiShieldOfTheNationTest extends BaseCardTest {

    @Test
    void commanderAttackWithLargeArtifactCreatesRhino() {
        addCommander();
        harness.addToBattlefield(player1, new GildedLotus());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        List<Permanent> rhinos = findPermanents(player1, "Rhino");
        assertThat(rhinos).hasSize(1);
        Permanent rhino = rhinos.getFirst();
        assertThat(rhino.getCard().getPower()).isEqualTo(4);
        assertThat(rhino.getCard().getToughness()).isEqualTo(4);
        assertThat(rhino.getCard().getSubtypes()).containsExactly(CardSubtype.RHINO);
        assertThat(rhino.getCard().getKeywords()).contains(Keyword.TRAMPLE);
        assertThat(rhino.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(rhino.isAttacking()).isFalse();
    }

    @Test
    void doesNotCreateRhinoWithoutLargeArtifact() {
        addCommander();

        declareAttackers(List.of(0));

        assertThat(findPermanents(player1, "Rhino")).isEmpty();
    }

    @Test
    void doesNotTriggerForNonCommanderAttacker() {
        harness.addToBattlefield(player1, new WKabiShieldOfTheNation());
        harness.addToBattlefield(player1, new GildedLotus());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(2));

        assertThat(findPermanents(player1, "Rhino")).isEmpty();
    }

    private Permanent addCommander() {
        WKabiShieldOfTheNation card = new WKabiShieldOfTheNation();
        gd.makeCommander(player1.getId(), card);
        return addCreatureReady(player1, card);
    }

    @Test
    void losingTheQualifyingArtifactBeforeResolutionPreventsRhino() {
        addCommander();
        Permanent lotus = harness.addToBattlefieldAndReturn(player1, new GildedLotus());

        declareAttackers(List.of(0));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(lotus);
        gd.playerGraveyards.get(player1.getId()).add(lotus.getCard());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Rhino")).isEmpty();
    }

    @Test
    void attackingWithAnOpponentsCommanderDoesNotTrigger() {
        WKabiShieldOfTheNation card = new WKabiShieldOfTheNation();
        gd.makeCommander(player2.getId(), card);
        addCreatureReady(player1, card);
        harness.addToBattlefield(player1, new GildedLotus());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Rhino")).isEmpty();
    }

    @Test
    void lowManaValueArtifactDoesNotQualify() {
        addCommander();
        harness.addToBattlefield(player1, new SolRing());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Rhino")).isEmpty();
    }

    @Test
    void opponentsLargeArtifactDoesNotQualify() {
        addCommander();
        harness.addToBattlefield(player2, new GildedLotus());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Rhino")).isEmpty();
    }
}
