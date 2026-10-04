package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.a.AxebaneGuardian;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.c.CrumblingSanctuary;
import com.github.laxika.magicalvibes.cards.g.GoreHouseChainwalker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.p.Progenitus;
import com.github.laxika.magicalvibes.cards.s.SpellbreakerBehemoth;
import com.github.laxika.magicalvibes.cards.w.WayfaringTemple;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EssenceBacklash.class, GrizzlyBears.class, Millstone.class, SpellbreakerBehemoth.class,
        AvatarOfMight.class, AxebaneGuardian.class, Cancel.class, CrumblingSanctuary.class,
        Progenitus.class, WayfaringTemple.class, GoreHouseChainwalker.class})
class EssenceBacklashTest extends BaseCardTest {

    @Test
    @DisplayName("Counters the creature spell and deals damage equal to its power to its controller")
    void countersAndDamagesByPower() {
        GrizzlyBears bears = new GrizzlyBears(); // 2/2
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new EssenceBacklash()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Cannot target a non-creature spell")
    void cannotTargetNonCreatureSpell() {
        Millstone millstone = new Millstone();
        harness.setHand(player1, List.of(millstone));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setHand(player2, List.of(new EssenceBacklash()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, millstone.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Still deals damage if the creature spell can't be countered")
    void damagesEvenIfUncounterable() {
        harness.addToBattlefield(player1, new SpellbreakerBehemoth());

        AvatarOfMight avatar = new AvatarOfMight(); // 8/8, protected by Behemoth
        harness.setHand(player1, List.of(avatar));
        harness.addMana(player1, ManaColor.GREEN, 8);

        harness.setHand(player2, List.of(new EssenceBacklash()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, avatar.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Avatar of Might");
        harness.assertLife(player1, 12);
    }

    @Test
    @DisplayName("Uses characteristic-defining power on the stack")
    void damagesByCharacteristicDefiningPower() {
        harness.addToBattlefield(player1, new AxebaneGuardian());
        harness.addToBattlefield(player1, new AxebaneGuardian());
        WayfaringTemple temple = new WayfaringTemple();
        harness.setHand(player1, List.of(temple));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        prepareBacklash();

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, temple.getId());

        harness.assertInGraveyard(player1, "Wayfaring Temple");
        harness.assertNotOnBattlefield(player1, "Wayfaring Temple");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Counters a zero-power creature without dealing damage")
    void countersZeroPowerCreature() {
        AxebaneGuardian guardian = new AxebaneGuardian();
        harness.setHand(player1, List.of(guardian));
        harness.addMana(player1, ManaColor.GREEN, 3);
        prepareBacklash();

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, guardian.getId());

        harness.assertInGraveyard(player1, "Axebane Guardian");
        harness.assertNotOnBattlefield(player1, "Axebane Guardian");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Deals no damage when its target has already been countered")
    void noDamageWhenTargetLeavesStack() {
        GoreHouseChainwalker creature = new GoreHouseChainwalker();
        harness.setHand(player1, List.of(creature, new Cancel()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLUE, 3);
        prepareBacklash();

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, creature.getId());
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Gore-House Chainwalker");
        harness.assertInGraveyard(player2, "Essence Backlash");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Counters before damage replaces exiling cards from the library")
    void counterReplacementHappensBeforeDamageReplacement() {
        harness.addToBattlefield(player2, new CrumblingSanctuary());
        harness.setLibrary(player1, List.of());
        Progenitus progenitus = new Progenitus();
        harness.setHand(player1, List.of(progenitus));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        prepareBacklash();

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, progenitus.getId());

        harness.assertNotOnBattlefield(player1, "Progenitus");
        harness.assertNotInGraveyard(player1, "Progenitus");
        harness.assertLife(player1, 20);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(progenitus);
    }

    @Test
    @DisplayName("Can counter its controller's own creature spell and damages that controller")
    void damagesOwnCreatureSpellController() {
        GoreHouseChainwalker creature = new GoreHouseChainwalker();
        harness.setHand(player1, List.of(creature, new EssenceBacklash()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertInGraveyard(player1, "Gore-House Chainwalker");
        harness.assertNotOnBattlefield(player1, "Gore-House Chainwalker");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    private void prepareBacklash() {
        harness.setHand(player2, List.of(new EssenceBacklash()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
    }
}
