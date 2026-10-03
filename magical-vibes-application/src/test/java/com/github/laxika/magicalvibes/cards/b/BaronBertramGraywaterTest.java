package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PristineTalisman;
import com.github.laxika.magicalvibes.cards.p.Panharmonicon;
import com.github.laxika.magicalvibes.cards.q.QueensCommission;
import com.github.laxika.magicalvibes.cards.t.TreasureDredger;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BaronBertramGraywater.class, QueensCommission.class, GrizzlyBears.class,
        PristineTalisman.class, TreasureDredger.class, Panharmonicon.class})
class BaronBertramGraywaterTest extends BaseCardTest {

    @Test
    void createsOneLifelinkVampireRogueWhenTokensEnter() {
        addCreatureReady(player1, new BaronBertramGraywater());
        harness.setHand(player1, List.of(new QueensCommission()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Vampire Rogue");
        assertThat(token.hasKeyword(Keyword.LIFELINK)).isTrue();
        assertThat(countPermanents(player1, "Vampire Rogue")).isEqualTo(1);
    }

    @Test
    void tokenTriggerFiresOnlyOnceEachTurn() {
        addCreatureReady(player1, new BaronBertramGraywater());
        harness.setHand(player1, List.of(new QueensCommission(), new QueensCommission()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, 0);
        resolveAllTriggers();
        harness.castSorcery(player1, 0, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Vampire Rogue")).isEqualTo(1);
    }

    @Test
    void sacrificesAnotherCreatureAndDrawsACard() {
        addCreatureReady(player1, new BaronBertramGraywater());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void sacrificesAnArtifactAndDrawsACard() {
        addCreatureReady(player1, new BaronBertramGraywater());
        harness.addToBattlefield(player1, new PristineTalisman());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        harness.assertInGraveyard(player1, "Pristine Talisman");
    }

    @Test
    void noncreatureTokenAlsoCreatesAVampireRogue() {
        addCreatureReady(player1, new BaronBertramGraywater());
        addCreatureReady(player1, new TreasureDredger());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
        assertThat(countPermanents(player1, "Vampire Rogue")).isEqualTo(1);
    }

    @Test
    void opposingTokensDoNotTriggerOrConsumeTheTurnLimit() {
        addCreatureReady(player1, new BaronBertramGraywater());
        addCreatureReady(player1, new TreasureDredger());
        addCreatureReady(player2, new TreasureDredger());
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 0, null, null);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Vampire Rogue")).isZero();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Vampire Rogue")).isEqualTo(1);
    }

    @Test
    void triggerBecomesAvailableAgainDuringOpponentsTurn() {
        addCreatureReady(player1, new BaronBertramGraywater());
        addCreatureReady(player1, new TreasureDredger());
        addCreatureReady(player1, new TreasureDredger());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Vampire Rogue")).isEqualTo(1);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 2, null, null);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Vampire Rogue")).isEqualTo(2);
    }

    @Test
    void cannotSacrificeBaronToItsOwnAbility() {
        addCreatureReady(player1, new BaronBertramGraywater());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Baron Bertram Graywater");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotSacrificeOpponentsCreature() {
        addCreatureReady(player1, new BaronBertramGraywater());
        addCreatureReady(player2, new BaronBertramGraywater());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Baron Bertram Graywater");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void sacrificeIsPaidBeforeTheDrawResolves() {
        addCreatureReady(player1, new BaronBertramGraywater());
        addCreatureReady(player1, new TreasureDredger());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new BaronBertramGraywater()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Treasure Dredger");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        resolveAllTriggers();
        harness.assertInHand(player1, "Baron Bertram Graywater");
    }

    @Test
    void panharmoniconCannotBypassTheOncePerTurnRestriction() {
        addCreatureReady(player1, new BaronBertramGraywater());
        addCreatureReady(player1, new TreasureDredger());
        harness.addToBattlefield(player1, new Panharmonicon());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
        assertThat(countPermanents(player1, "Vampire Rogue")).isEqualTo(1);
    }
}
