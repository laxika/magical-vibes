package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BesottedKnight;
import com.github.laxika.magicalvibes.cards.s.ScarecrowGuide;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AgathasChampion.class, ScarecrowGuide.class, BesottedKnight.class})
class AgathasChampionTest extends BaseCardTest {

    @Test
    void withoutBargainDoesNotFight() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BesottedKnight());
        harness.setHand(player1, List.of(new AgathasChampion()));
        addMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    void withBargainFightsTargetCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new ScarecrowGuide());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BesottedKnight());
        harness.setHand(player1, List.of(new AgathasChampion()));
        addMana();

        harness.castKickedCreatureWithPermanent(player1, 0, sacrifice.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        harness.assertOnBattlefield(player1, "Agatha's Champion");
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getId().equals(harness.getPermanentId(player1, "Agatha's Champion")))
                .findFirst().orElseThrow().getMarkedDamage()).isEqualTo(3);
        harness.assertInGraveyard(player1, "Scarecrow Guide");
    }

    @Test
    void bargainCanDeclineFightEvenWhenAnOpponentHasACreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new ScarecrowGuide());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BesottedKnight());
        harness.setHand(player1, List.of(new AgathasChampion()));
        addMana();

        harness.castKickedCreatureWithPermanent(player1, 0, sacrifice.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Agatha's Champion");
        harness.assertInGraveyard(player1, "Scarecrow Guide");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    void bargainCanFightNoCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new ScarecrowGuide());
        harness.setHand(player1, List.of(new AgathasChampion()));
        addMana();

        harness.castKickedCreatureWithPermanent(player1, 0, sacrifice.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Agatha's Champion");
        harness.assertInGraveyard(player1, "Scarecrow Guide");
    }

    @Test
    void bargainCannotTargetOwnCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new ScarecrowGuide());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BesottedKnight());
        harness.setHand(player1, List.of(new AgathasChampion()));
        addMana();

        harness.addToBattlefield(player2, new BesottedKnight());
        harness.castKickedCreatureWithPermanent(player1, 0, sacrifice.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bargainCannotSacrificeCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new BesottedKnight());
        harness.setHand(player1, List.of(new AgathasChampion()));
        addMana();

        assertThatThrownBy(() -> harness.castKickedCreatureWithPermanent(player1, 0, sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
