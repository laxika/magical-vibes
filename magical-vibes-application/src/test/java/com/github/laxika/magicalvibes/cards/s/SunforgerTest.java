package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.h.HealingSalve;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Sunforger.class, GrizzlyBears.class, HealingSalve.class})
class SunforgerTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +4/+0")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1);
        Permanent sunforger = addSunforgerReady(player1);
        sunforger.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equipped creature unattaches Sunforger and casts a matching instant for free")
    void unattachesAndCastsMatchingInstantForFree() {
        Permanent creature = addCreatureReady(player1);
        Permanent sunforger = addSunforgerReady(player1);
        sunforger.setAttachedTo(creature.getId());
        harness.setLibrary(player1, List.of(new HealingSalve(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(sunforger.getAttachedTo()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);

        harness.handleCardChosen(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "Target player gains 3 life");
        harness.handlePermanentChosen(player1, player1.getId());

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Healing Salve")
                && entry.getEntryType() == StackEntryType.INSTANT_SPELL
                && entry.getControllerId().equals(player1.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName).contains("Healing Salve");
    }

    @Test
    @DisplayName("Nonmatching cards are not offered by the activated ability")
    void doesNotOfferNonmatchingCards() {
        Permanent creature = addCreatureReady(player1);
        Permanent sunforger = addSunforgerReady(player1);
        sunforger.setAttachedTo(creature.getId());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName).containsExactly("Grizzly Bears");
    }

    private Permanent addCreatureReady(Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        creature.setSummoningSick(false);
        return creature;
    }

    private Permanent addSunforgerReady(Player player) {
        Permanent sunforger = harness.addToBattlefieldAndReturn(player, new Sunforger());
        sunforger.setSummoningSick(false);
        return sunforger;
    }
}
