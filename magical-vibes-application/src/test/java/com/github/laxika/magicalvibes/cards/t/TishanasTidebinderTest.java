package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FumeSpitter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MerfolkTrickster;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TishanasTidebinder.class, FumeSpitter.class, GrizzlyBears.class,
        MerfolkTrickster.class, Shock.class})
class TishanasTidebinderTest extends BaseCardTest {

    @Test
    @DisplayName("Counters an activated ability and removes all abilities from its creature source")
    void countersActivatedAbilityAndRemovesSourceAbilities() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);
        FumeSpitter fumeSpitter = new FumeSpitter();
        harness.addToBattlefield(player2, fumeSpitter);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null,
                harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player2);

        TishanasTidebinder tidebinder = new TishanasTidebinder();
        harness.setHand(player1, List.of(tidebinder));
        addTidebinderMana();
        harness.castCreature(player1, 0, 0, fumeSpitter.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bearsPermanent = findPermanent(player1, "Grizzly Bears");
        assertThat(bearsPermanent.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE))
                .isZero();
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null,
                harness.getPermanentId(player1, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class);

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Tishana's Tidebinder"));
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null,
                harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();
        assertThat(bearsPermanent.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Counters a triggered ability and removes the triggered source's abilities")
    void countersTriggeredAbilityAndRemovesSourceAbilities() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player2, bears);
        MerfolkTrickster trickster = new MerfolkTrickster();
        harness.setHand(player2, List.of(trickster));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castCreature(player2, 0, 0,
                harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new TishanasTidebinder()));
        addTidebinderMana();
        harness.castCreature(player1, 0, 0, trickster.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd,
                findPermanent(player2, "Merfolk Trickster"), Keyword.FLASH)).isFalse();
    }

    @Test
    @DisplayName("Can resolve with no target")
    void canResolveWithNoTarget() {
        harness.setHand(player1, List.of(new TishanasTidebinder()));
        addTidebinderMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    private void addTidebinderMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
