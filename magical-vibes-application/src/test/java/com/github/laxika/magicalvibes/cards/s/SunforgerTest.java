package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.cards.c.Char;
import com.github.laxika.magicalvibes.cards.c.Convolute;
import com.github.laxika.magicalvibes.cards.d.DevouringLight;
import com.github.laxika.magicalvibes.cards.l.LightningHelix;
import com.github.laxika.magicalvibes.cards.r.RainOfEmbers;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Sunforger.class, BorosRecruit.class, LightningHelix.class, Char.class,
        RainOfEmbers.class, Convolute.class, DevouringLight.class})
class SunforgerTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +4/+0")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new BorosRecruit());
        Permanent sunforger = addSunforgerReady(player1);
        sunforger.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Unattached Sunforger does not boost creatures")
    void unattachedSunforgerDoesNotBoostCreatures() {
        Permanent creature = addCreatureReady(player1, new BorosRecruit());
        addSunforgerReady(player1);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Equip {3} attaches Sunforger to a creature you control")
    void equipAbilityAttachesSunforger() {
        Permanent sunforger = addSunforgerReady(player1);
        Permanent creature = addCreatureReady(player1, new BorosRecruit());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(sunforger.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Sunforger grants its search ability only to the equipped creature")
    void searchAbilityRequiresAnEquippedCreature() {
        addCreatureReady(player1, new BorosRecruit());
        addSunforgerReady(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Equipped creature unattaches Sunforger and casts a red-or-white instant for free")
    void unattachesAndCastsMatchingInstantForFree() {
        Permanent creature = addCreatureReady(player1, new BorosRecruit());
        Permanent sunforger = addSunforgerReady(player1);
        sunforger.setAttachedTo(creature.getId());
        harness.setLibrary(player1, List.of(new LightningHelix(), new RainOfEmbers()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(sunforger.getAttachedTo()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);

        harness.handleCardChosen(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Lightning Helix")
                && entry.getEntryType() == StackEntryType.INSTANT_SPELL
                && entry.getControllerId().equals(player1.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player1, "Lightning Helix");
    }

    @Test
    @DisplayName("Sunforger can cast a red instant from the library")
    void castsRedInstantFromLibrary() {
        Permanent creature = addCreatureReady(player1, new BorosRecruit());
        Permanent sunforger = addSunforgerReady(player1);
        sunforger.setAttachedTo(creature.getId());
        harness.setLibrary(player1, List.of(new Char()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 16);
        harness.assertInGraveyard(player1, "Char");
    }

    @Test
    @DisplayName("A non-instant red card is not offered by the activated ability")
    void doesNotOfferNonInstantCards() {
        Permanent creature = addCreatureReady(player1, new BorosRecruit());
        Permanent sunforger = addSunforgerReady(player1);
        sunforger.setAttachedTo(creature.getId());
        harness.setLibrary(player1, List.of(new RainOfEmbers()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName).containsExactly("Rain of Embers");
    }

    @Test
    @DisplayName("A blue instant is not offered by the activated ability")
    void doesNotOfferInstantOfWrongColor() {
        Permanent creature = addCreatureReady(player1, new BorosRecruit());
        Permanent sunforger = addSunforgerReady(player1);
        sunforger.setAttachedTo(creature.getId());
        harness.setLibrary(player1, List.of(new Convolute()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName).containsExactly("Convolute");
    }

    @Test
    @DisplayName("A matching instant with no legal targets stays in the library")
    void leavesMatchingInstantWithoutLegalTargetsInLibrary() {
        Permanent creature = addCreatureReady(player1, new BorosRecruit());
        Permanent sunforger = addSunforgerReady(player1);
        sunforger.setAttachedTo(creature.getId());
        harness.setLibrary(player1, List.of(new DevouringLight()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName).containsExactly("Devouring Light");
    }

    private Permanent addSunforgerReady(Player player) {
        Permanent sunforger = harness.addToBattlefieldAndReturn(player, new Sunforger());
        sunforger.setSummoningSick(false);
        return sunforger;
    }
}
