package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GoblinBirdGrabber;
import com.github.laxika.magicalvibes.cards.i.Infuriate;
import com.github.laxika.magicalvibes.cards.l.LavakinBrawler;
import com.github.laxika.magicalvibes.cards.m.MuYanlingSkyDancer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChandrasEmbercat.class, LavakinBrawler.class, ChandraNovicePyromancer.class,
        MuYanlingSkyDancer.class, GoblinBirdGrabber.class, CloudkinSeer.class, Infuriate.class})
class ChandrasEmbercatTest extends BaseCardTest {

    @Test
    @DisplayName("Mana ability produces mana that casts an Elemental spell")
    void manaCastsElementalSpell() {
        addCreatureReady(player1, new ChandrasEmbercat());
        harness.setHand(player1, List.of(new LavakinBrawler()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Lavakin Brawler");
    }

    @Test
    @DisplayName("Mana ability produces mana that casts a Chandra planeswalker spell")
    void manaCastsChandraPlaneswalkerSpell() {
        addCreatureReady(player1, new ChandrasEmbercat());
        harness.setHand(player1, List.of(new ChandraNovicePyromancer()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.castPlaneswalker(player1, 0);

        assertThat(harness.getGameData().stack).hasSize(1);
    }

    @Test
    @DisplayName("Mana ability cannot pay for a non-Elemental or non-Chandra spell")
    void manaCannotCastUnlistedSpell() {
        addCreatureReady(player1, new ChandrasEmbercat());
        harness.setHand(player1, List.of(new MuYanlingSkyDancer()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.castPlaneswalker(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void manaPaysGenericCostOfNonredElemental() {
        addCreatureReady(player1, new ChandrasEmbercat());
        harness.setHand(player1, List.of(new CloudkinSeer()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.assertNotInHand(player1, "Cloudkin Seer");
    }

    @Test
    void manaCannotCastNonElementalCreature() {
        addCreatureReady(player1, new ChandrasEmbercat());
        harness.setHand(player1, List.of(new GoblinBirdGrabber()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Goblin Bird-Grabber");
    }

    @Test
    void manaCannotCastInstant() {
        var embercat = addCreatureReady(player1, new ChandrasEmbercat());
        harness.setHand(player1, List.of(new Infuriate()));
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, embercat.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Infuriate");
    }

    @Test
    void manaCannotPayActivatedAbilityCost() {
        addCreatureReady(player1, new ChandrasEmbercat());
        harness.addToBattlefield(player1, new GoblinBirdGrabber());
        harness.addToBattlefield(player1, new CloudkinSeer());
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 1, 0, null, null);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void manaAbilityResolvesImmediatelyAndRequiresUntappedSource() {
        var embercat = addCreatureReady(player1, new ChandrasEmbercat());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(embercat.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void summoningSickEmbercatCannotActivate() {
        harness.addToBattlefield(player1, new ChandrasEmbercat());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
