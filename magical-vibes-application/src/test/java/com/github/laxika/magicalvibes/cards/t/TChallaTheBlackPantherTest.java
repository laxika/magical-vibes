package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DarksteelAxe;
import com.github.laxika.magicalvibes.cards.d.DarksteelReactor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TChallaTheBlackPanther.class, DarksteelAxe.class, DarksteelReactor.class, GrizzlyBears.class})
class TChallaTheBlackPantherTest extends BaseCardTest {

    @Test
    void entersWithATappedIndestructibleVibraniumToken() {
        castTChalla();

        Permanent vibranium = findPermanent(player1, "Vibranium");
        assertThat(vibranium.isTapped()).isTrue();
        assertThat(vibranium.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(vibranium.getCard().hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void vibraniumAddsRestrictedColorlessMana() {
        castTChalla();
        Permanent vibranium = findPermanent(player1, "Vibranium");
        vibranium.untap();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(vibranium), 0, null, null);

        assertThat(vibranium.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getPowerstoneOnlyColorless()).isEqualTo(1);
    }

    @Test
    void attacksToCreateAnotherTappedVibraniumToken() {
        Permanent tChalla = harness.addToBattlefieldAndReturn(player1, new TChallaTheBlackPanther());
        tChalla.setSummoningSick(false);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Vibranium")).hasSize(1);
        assertThat(findPermanent(player1, "Vibranium").isTapped()).isTrue();
    }

    @Test
    void putsTwoCountersOnItWhenControllerCastsArtifactWithManaValueFourOrGreater() {
        Permanent tChalla = harness.addToBattlefieldAndReturn(player1, new TChallaTheBlackPanther());
        harness.setHand(player1, List.of(new DarksteelReactor()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(tChalla.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void doesNotPutCountersOnItForSmallerOrNonartifactSpells() {
        Permanent tChalla = harness.addToBattlefieldAndReturn(player1, new TChallaTheBlackPanther());

        harness.setHand(player1, List.of(new DarksteelAxe()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(tChalla.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castTChalla() {
        harness.setHand(player1, List.of(new TChallaTheBlackPanther()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
