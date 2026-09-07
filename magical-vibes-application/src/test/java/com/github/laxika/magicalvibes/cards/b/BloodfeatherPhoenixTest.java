package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InvasionOfInnistrad;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodfeatherPhoenix.class, Shock.class, GrizzlyBears.class, InvasionOfInnistrad.class})
class BloodfeatherPhoenixTest extends BaseCardTest {

    @Test
    @DisplayName("Returns from the graveyard when a spell damages an opponent and gains haste")
    void returnsWhenSpellDamagesOpponent() {
        harness.setGraveyard(player1, List.of(new BloodfeatherPhoenix()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent phoenix = findPermanent(player1, "Bloodfeather Phoenix");
        assertThat(phoenix.getGrantedKeywords()).contains(Keyword.HASTE);
        harness.assertNotInGraveyard(player1, "Bloodfeather Phoenix");
    }

    @Test
    @DisplayName("Returns from the graveyard when a spell damages a battle")
    void returnsWhenSpellDamagesBattle() {
        harness.setGraveyard(player1, List.of(new BloodfeatherPhoenix()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfInnistrad());
        battle.setCounterCount(CounterType.DEFENSE, 5);

        harness.castInstant(player1, 0, battle.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Bloodfeather Phoenix");
    }

    @Test
    @DisplayName("Does not return when the spell damages a creature")
    void doesNotReturnWhenSpellDamagesCreature() {
        harness.setGraveyard(player1, List.of(new BloodfeatherPhoenix()));
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Bloodfeather Phoenix");
    }
}
