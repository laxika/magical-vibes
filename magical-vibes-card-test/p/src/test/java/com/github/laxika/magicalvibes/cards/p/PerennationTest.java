package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.k.KrotiqNestguard;
import com.github.laxika.magicalvibes.cards.k.KnockoutManeuver;
import com.github.laxika.magicalvibes.cards.w.WingspanStride;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({Perennation.class, KrotiqNestguard.class, KnockoutManeuver.class, Island.class, WingspanStride.class})
class PerennationTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target permanent card with hexproof and indestructible counters")
    void returnsPermanentWithAbilityCounters() {
        Card creature = new KrotiqNestguard();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new Perennation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent returned = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Krotiq Nestguard"));
        harness.assertNotInGraveyard(player1, "Krotiq Nestguard");
        assertThat(returned.getCounterCount(CounterType.HEXPROOF)).isEqualTo(1);
        assertThat(returned.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, returned, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, returned, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a nonpermanent card")
    void cannotTargetNonpermanentCard() {
        Card nonpermanent = new KnockoutManeuver();
        harness.setGraveyard(player1, List.of(nonpermanent));
        harness.setHand(player1, List.of(new Perennation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, nonpermanent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnsLandWithBothCounters() {
        Card land = new Island();
        harness.setGraveyard(player1, List.of(land));
        preparePerennation();

        harness.castSorcery(player1, 0, land.getId());
        harness.passBothPriorities();

        Permanent returned = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Island"));
        assertThat(returned.getCounterCount(CounterType.HEXPROOF)).isEqualTo(1);
        assertThat(returned.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Island");
    }

    @Test
    void cannotTargetOpponentsGraveyard() {
        Card creature = new KrotiqNestguard();
        harness.setGraveyard(player2, List.of(creature));
        preparePerennation();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotReturnTargetThatLeftGraveyard() {
        Card creature = new KrotiqNestguard();
        harness.setGraveyard(player1, List.of(creature));
        preparePerennation();
        harness.castSorcery(player1, 0, creature.getId());

        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(creature));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Krotiq Nestguard");
    }

    @Test
    void returnedAuraChoosesAttachmentAndReceivesCounters() {
        Card aura = new WingspanStride();
        Permanent host = harness.addToBattlefieldAndReturn(player2, new KrotiqNestguard());
        harness.setGraveyard(player1, List.of(aura));
        preparePerennation();

        harness.castSorcery(player1, 0, aura.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, host.getId());

        Permanent returned = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Wingspan Stride"));
        assertThat(returned.getAttachedTo()).isEqualTo(host.getId());
        assertThat(returned.getCounterCount(CounterType.HEXPROOF)).isEqualTo(1);
        assertThat(returned.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Wingspan Stride");
    }

    @Test
    void auraWithoutLegalAttachmentStaysInGraveyard() {
        Card aura = new WingspanStride();
        harness.setGraveyard(player1, List.of(aura));
        preparePerennation();

        harness.castSorcery(player1, 0, aura.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Wingspan Stride");
        harness.assertNotOnBattlefield(player1, "Wingspan Stride");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void preparePerennation() {
        harness.setHand(player1, List.of(new Perennation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
