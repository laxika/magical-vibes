package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AlphaKavu;
import com.github.laxika.magicalvibes.cards.a.ArcticMerfolk;
import com.github.laxika.magicalvibes.cards.d.DaringLeap;
import com.github.laxika.magicalvibes.cards.f.ForsakenCity;
import com.github.laxika.magicalvibes.cards.i.Implode;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Confound.class, ArcticMerfolk.class, DaringLeap.class, Implode.class,
        ForsakenCity.class, AlphaKavu.class})
class ConfoundTest extends BaseCardTest {

    @Test
    void canTargetASpellThatTargetsACreature() {
        ArcticMerfolk merfolk = new ArcticMerfolk();
        harness.addToBattlefield(player1, merfolk);

        DaringLeap daringLeap = new DaringLeap();
        harness.setHand(player1, List.of(daringLeap));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Arctic Merfolk"));
        harness.passPriority(player1);

        Confound confound = new Confound();
        harness.setHand(player2, List.of(confound));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, daringLeap.getId());

        assertThat(harness.getGameData().stack).hasSize(2);
        assertThat(harness.getGameData().stack.getLast().getTargetId()).isEqualTo(daringLeap.getId());
    }

    @Test
    void cannotTargetACreatureSpell() {
        ArcticMerfolk merfolk = new ArcticMerfolk();
        harness.setHand(player1, List.of(merfolk));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new Confound()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, merfolk.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetASpellThatTargetsALand() {
        ForsakenCity city = new ForsakenCity();
        harness.addToBattlefield(player1, city);

        Implode implode = new Implode();
        harness.setHand(player1, List.of(implode));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castSorcery(player1, 0, harness.getPermanentId(player1, "Forsaken City"));
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new Confound()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, implode.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetAnAbilityThatTargetsACreature() {
        Permanent alphaKavu = addCreatureReady(player1, new AlphaKavu());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.activateAbility(player1, 0, null, alphaKavu.getId());
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new Confound()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        var abilityId = harness.getGameData().stack.getLast().getCard().getId();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, abilityId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void countersTargetSpellAndDrawsACard() {
        ArcticMerfolk merfolk = new ArcticMerfolk();
        harness.addToBattlefield(player1, merfolk);

        DaringLeap daringLeap = new DaringLeap();
        harness.setHand(player1, List.of(daringLeap));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Arctic Merfolk"));
        harness.passPriority(player1);

        Confound confound = new Confound();
        harness.setHand(player2, List.of(confound));
        harness.setLibrary(player2, List.of(new ArcticMerfolk()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        int handSizeBeforeCasting = gd.playerHands.get(player2.getId()).size();
        harness.castInstant(player2, 0, daringLeap.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Daring Leap");
        harness.assertNotOnBattlefield(player1, "Daring Leap");
        harness.assertInGraveyard(player2, "Confound");
        assertThat(gd.playerHands.get(player2.getId()).size()).isEqualTo(handSizeBeforeCasting);
        harness.assertInHand(player2, "Arctic Merfolk");
    }
}
