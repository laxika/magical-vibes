package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TyvarKell;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KayaTheInexorableTest extends BaseCardTest {

    @Test
    @DisplayName("+1 returns a ghostform creature to its owner's hand when it dies and creates a Spirit")
    void plusOneReturnsCreatureFromGraveyard() {
        Permanent kaya = addReadyKaya(4);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, battlefieldIndex(kaya), 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.GHOSTFORM)).isEqualTo(1);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castInstant(player2, 0, creature.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Grizzly Bears");
        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(spirit.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(spirit.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);
        assertThat(spirit.getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("+1 returns a ghostform creature to its owner's hand when it is exiled and creates a Spirit")
    void plusOneReturnsCreatureFromExile() {
        Permanent kaya = addReadyKaya(4);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, battlefieldIndex(kaya), 0, null, creature.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, battlefieldIndex(kaya), 1, null, creature.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
    }

    @Test
    @DisplayName("-7 emblem offers a legendary spell from hand and casts it without paying its mana cost")
    void ultimateCastsLegendarySpellFromHand() {
        Permanent kaya = addReadyKaya(7);
        harness.activateAbility(player1, battlefieldIndex(kaya), 2, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new TyvarKell()));
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Tyvar Kell");
    }

    private Permanent addReadyKaya(int loyalty) {
        Permanent kaya = new Permanent(new KayaTheInexorable());
        kaya.setCounterCount(CounterType.LOYALTY, loyalty);
        kaya.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(kaya);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return kaya;
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
