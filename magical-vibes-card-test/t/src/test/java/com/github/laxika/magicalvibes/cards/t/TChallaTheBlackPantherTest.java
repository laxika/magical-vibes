package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DarksteelAxe;
import com.github.laxika.magicalvibes.cards.d.DarksteelReactor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OverwhelmingStampede;
import com.github.laxika.magicalvibes.cards.s.StonecoilSerpent;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TChallaTheBlackPanther.class, DarksteelAxe.class, DarksteelReactor.class, GrizzlyBears.class,
        OverwhelmingStampede.class, StonecoilSerpent.class})
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
        addCreatureReady(player1, new TChallaTheBlackPanther());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Vibranium")).hasSize(1);
        assertThat(findPermanent(player1, "Vibranium").isTapped()).isTrue();
    }

    @Test
    void putsTwoCountersOnItWhenControllerCastsArtifactWithManaValueFourOrGreater() {
        Permanent tChalla = harness.addToBattlefieldAndReturn(player1, new TChallaTheBlackPanther());
        harness.castFromHand(player1, new DarksteelReactor(), "{4}");
        resolveAllTriggers();

        assertThat(tChalla.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void doesNotPutCountersOnItForSmallerOrNonartifactSpells() {
        Permanent tChalla = harness.addToBattlefieldAndReturn(player1, new TChallaTheBlackPanther());

        harness.castFromHand(player1, new DarksteelAxe(), "{1}");
        harness.passBothPriorities();

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(tChalla.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotTriggerForHighManaValueNonartifactSpell() {
        Permanent tChalla = harness.addToBattlefieldAndReturn(player1, new TChallaTheBlackPanther());

        harness.castFromHand(player1, new OverwhelmingStampede(), "{3}{G}{G}");
        resolveAllTriggers();

        assertThat(tChalla.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotTriggerForOpponentsArtifactSpell() {
        Permanent tChalla = harness.addToBattlefieldAndReturn(player1, new TChallaTheBlackPanther());
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new DarksteelReactor(), "{4}");
        resolveAllTriggers();

        assertThat(tChalla.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void enteringArtifactWithoutCastingDoesNotTrigger() {
        Permanent tChalla = harness.addToBattlefieldAndReturn(player1, new TChallaTheBlackPanther());

        harness.enterBattlefieldAndReturn(player1, new DarksteelReactor());
        resolveAllTriggers();

        assertThat(tChalla.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void artifactCastTriggerResolvesBeforeTheArtifactSpell() {
        Permanent tChalla = harness.addToBattlefieldAndReturn(player1, new TChallaTheBlackPanther());
        harness.castFromHand(player1, new DarksteelReactor(), "{4}");

        assertThat(tChalla.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        assertThat(tChalla.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertNotOnBattlefield(player1, "Darksteel Reactor");
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Darksteel Reactor");
    }

    @Test
    void xCountsTowardArtifactSpellsManaValueAtFour() {
        Permanent tChalla = harness.addToBattlefieldAndReturn(player1, new TChallaTheBlackPanther());
        harness.setHand(player1, List.of(new StonecoilSerpent()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0, 4);
        resolveAllTriggers();

        assertThat(tChalla.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void xBelowFourDoesNotTrigger() {
        Permanent tChalla = harness.addToBattlefieldAndReturn(player1, new TChallaTheBlackPanther());
        harness.setHand(player1, List.of(new StonecoilSerpent()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0, 3);
        resolveAllTriggers();

        assertThat(tChalla.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void vibraniumManaCanPayForArtifactSpell() {
        castTChalla();
        Permanent vibranium = findPermanent(player1, "Vibranium");
        vibranium.untap();
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(vibranium), 0, null, null);
        harness.setHand(player1, List.of(new DarksteelAxe()));

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Darksteel Axe");
        assertThat(gd.playerManaPools.get(player1.getId()).getPowerstoneOnlyColorless()).isZero();
    }

    @Test
    void vibraniumManaCannotPayGenericCostOfNonartifactSpell() {
        castTChalla();
        Permanent vibranium = findPermanent(player1, "Vibranium");
        vibranium.untap();
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(vibranium), 0, null, null);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).getPowerstoneOnlyColorless()).isEqualTo(1);
    }

    @Test
    void eachQualifyingArtifactCastAddsTwoMoreCounters() {
        Permanent tChalla = harness.addToBattlefieldAndReturn(player1, new TChallaTheBlackPanther());

        harness.castFromHand(player1, new DarksteelReactor(), "{4}");
        resolveAllTriggers();
        harness.castFromHand(player1, new DarksteelReactor(), "{4}");
        resolveAllTriggers();

        assertThat(tChalla.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void attackAddsVibraniumToTheTokenAlreadyCreatedOnEntry() {
        castTChalla();
        Permanent tChalla = findPermanent(player1, "T'Challa, the Black Panther");
        tChalla.setSummoningSick(false);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(tChalla)));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Vibranium")).hasSize(2)
                .allMatch(Permanent::isTapped);
    }

    private void castTChalla() {
        harness.castFromHand(player1, new TChallaTheBlackPanther(), "{1}{G}{W}");
        resolveAllTriggers();
    }
}
