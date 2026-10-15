package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.c.CandyTrail;
import com.github.laxika.magicalvibes.cards.e.ElfhameDruid;
import com.github.laxika.magicalvibes.cards.s.SweettoothWitch;
import com.github.laxika.magicalvibes.cards.u.UpTheBeanstalk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HighFaeNegotiator.class, DarksteelRelic.class, CandyTrail.class,
        UpTheBeanstalk.class, ElfhameDruid.class, SweettoothWitch.class})
class HighFaeNegotiatorTest extends BaseCardTest {

    @Test
    void doesNotDrainWithoutBargain() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.castFromHand(player1, new HighFaeNegotiator(), "{3}{B}{B}");
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void bargainDrainsOpponentAndGainsThreeLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        harness.setHand(player1, List.of(new HighFaeNegotiator()));
        addManaForHighFaeNegotiator();

        harness.castKickedCreatureWithPermanent(player1, 0, sacrifice.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        harness.assertInGraveyard(player1, "Darksteel Relic");
    }

    @Test
    @CardUsed({HighFaeNegotiator.class, CandyTrail.class})
    void artifactIsSacrificedAsCastingCostBeforeLifeDrain() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new CandyTrail());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new HighFaeNegotiator()));
        addManaForHighFaeNegotiator();

        harness.castKickedCreatureWithPermanent(player1, 0, sacrifice.getId());

        harness.assertInGraveyard(player1, "Candy Trail");
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);

        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @CardUsed({HighFaeNegotiator.class, UpTheBeanstalk.class})
    void canBargainWithAnEnchantment() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new UpTheBeanstalk());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new HighFaeNegotiator()));
        addManaForHighFaeNegotiator();

        harness.castKickedCreatureWithPermanent(player1, 0, sacrifice.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Up the Beanstalk");
        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @CardUsed({HighFaeNegotiator.class})
    void cannotBargainWithANontokenNonartifactNonenchantmentCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HighFaeNegotiator());
        harness.setHand(player1, List.of(new HighFaeNegotiator()));
        addManaForHighFaeNegotiator();

        assertThatThrownBy(() -> harness.castKickedCreatureWithPermanent(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @CardUsed({HighFaeNegotiator.class, CandyTrail.class})
    void cannotBargainWithOpponentsArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new CandyTrail());
        harness.setHand(player1, List.of(new HighFaeNegotiator()));
        addManaForHighFaeNegotiator();

        assertThatThrownBy(() -> harness.castKickedCreatureWithPermanent(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(artifact);
    }

    @Test
    @CardUsed({HighFaeNegotiator.class, CandyTrail.class, ElfhameDruid.class})
    void bargainCannotUseManaRestrictedToKickedSpells() {
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new ElfhameDruid());
        druid.setSummoningSick(false);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new CandyTrail());
        harness.activateAbility(player1, 0, 1, null, null);
        harness.setHand(player1, List.of(new HighFaeNegotiator()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castKickedCreatureWithPermanent(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
    }

    private void addManaForHighFaeNegotiator() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    @Test
    @CardUsed({HighFaeNegotiator.class, SweettoothWitch.class})
    void canBargainWithAFoodToken() {
        harness.castFromHand(player1, new SweettoothWitch(), "{2}{B}");
        resolveAllTriggers();
        Permanent food = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new HighFaeNegotiator()));
        addManaForHighFaeNegotiator();

        harness.castKickedCreatureWithPermanent(player1, 0, food.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(food);
        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }
}
