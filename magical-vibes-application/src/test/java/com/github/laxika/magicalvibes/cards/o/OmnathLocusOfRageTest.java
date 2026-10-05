package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OmnathLocusOfRage.class, AirElemental.class, Forest.class, GrizzlyBears.class})
class OmnathLocusOfRageTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall creates a 5/5 red and green Elemental token")
    void landfallCreatesElementalToken() {
        harness.addToBattlefield(player1, new OmnathLocusOfRage());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        Permanent elemental = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Elemental"))
                .findFirst()
                .orElseThrow();
        assertThat(elemental.getEffectivePower()).isEqualTo(5);
        assertThat(elemental.getEffectiveToughness()).isEqualTo(5);
        assertThat(elemental.getCard().getColors()).containsExactlyInAnyOrder(CardColor.RED, CardColor.GREEN);
        assertThat(elemental.getCard().getSubtypes()).contains(CardSubtype.ELEMENTAL);
    }

    @Test
    @DisplayName("Another Elemental dying lets Omnath deal 3 damage to any target")
    void anotherElementalDeathDealsDamage() {
        harness.addToBattlefield(player1, new OmnathLocusOfRage());
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        elemental.setMarkedDamage(4);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("A non-Elemental creature dying does not trigger Omnath")
    void nonElementalDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new OmnathLocusOfRage());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setMarkedDamage(2);
        harness.runStateBasedActions();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Omnath dying triggers its own damage ability")
    void ownDeathDealsDamage() {
        Permanent omnath = harness.addToBattlefieldAndReturn(player1, new OmnathLocusOfRage());
        omnath.setMarkedDamage(5);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("An opposing land entering does not trigger landfall")
    void opposingLandDoesNotTrigger() {
        harness.addToBattlefield(player1, new OmnathLocusOfRage());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Forest()));

        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An opposing Elemental dying does not trigger Omnath")
    void opposingElementalDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new OmnathLocusOfRage());
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        elemental.setMarkedDamage(4);

        harness.runStateBasedActions();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An Elemental token dying can deal damage to a creature")
    void tokenDeathCanTargetCreature() {
        Permanent omnath = harness.addToBattlefieldAndReturn(player1, new OmnathLocusOfRage());
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();
        token.setMarkedDamage(5);

        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, omnath.getId());
        harness.passBothPriorities();

        assertThat(omnath.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Omnath and its token dying together produce one trigger for each")
    void simultaneousDeathsEachDealDamage() {
        Permanent omnath = harness.addToBattlefieldAndReturn(player1, new OmnathLocusOfRage());
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();
        omnath.setMarkedDamage(5);
        token.setMarkedDamage(5);

        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 14);
        harness.assertNotOnBattlefield(player1, "Omnath, Locus of Rage");
        assertThat(gd.stack).isEmpty();
    }
}
