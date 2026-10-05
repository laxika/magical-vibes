package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CurryFavor;
import com.github.laxika.magicalvibes.cards.m.SmittenSwordmaster;
import com.github.laxika.magicalvibes.cards.g.Gingerbrute;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SmittenSwordmaster.class, CurryFavor.class, Gingerbrute.class})
class SmittenSwordmasterTest extends BaseCardTest {

    @Test
    void adventureGainsAndLosesLifeForEachKnightControlled() {
        harness.addToBattlefield(player1, new SmittenSwordmaster());
        harness.addToBattlefield(player1, new SmittenSwordmaster());
        SmittenSwordmaster card = new SmittenSwordmaster();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void adventureCountsOnlyKnightsControlledByCaster() {
        harness.addToBattlefield(player1, new SmittenSwordmaster());
        harness.addToBattlefield(player2, new SmittenSwordmaster());
        harness.setHand(player1, List.of(new SmittenSwordmaster()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    void adventureWithNoKnightsDoesNotChangeLifeAndStillExilesCard() {
        harness.addToBattlefield(player1, new Gingerbrute());
        SmittenSwordmaster card = new SmittenSwordmaster();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void adventureCountsKnightsAtResolution() {
        harness.addToBattlefield(player1, new SmittenSwordmaster());
        harness.addToBattlefield(player1, new Gingerbrute());
        harness.setHand(player1, List.of(new SmittenSwordmaster()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.addToBattlefield(player1, new SmittenSwordmaster());
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    void creatureFaceCanBeCastFromExileAfterAdventure() {
        SmittenSwordmaster card = new SmittenSwordmaster();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Smitten Swordmaster");
        assertThat(gd.findExiledCard(card.getId())).isNull();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void castingCreatureDirectlyDoesNotResolveAdventureEffects() {
        harness.addToBattlefield(player1, new SmittenSwordmaster());

        harness.castFromHand(player1, new SmittenSwordmaster(), "{1}{B}");
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Smitten Swordmaster")).isEqualTo(2);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void lifelinkGainsLifeWhenSwordmasterDealsCombatDamageAndDies() {
        Permanent attacker = addCreatureReady(player1, new SmittenSwordmaster());
        Permanent blocker = addCreatureReady(player2, new SmittenSwordmaster());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 22);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        harness.assertInGraveyard(player1, "Smitten Swordmaster");
        harness.assertInGraveyard(player2, "Smitten Swordmaster");
    }

    @Test
    void sorceryAdventureCannotBeCastDuringOpponentsTurn() {
        SmittenSwordmaster card = new SmittenSwordmaster();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
