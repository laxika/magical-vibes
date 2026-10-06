package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.e.ElfhameDruid;
import com.github.laxika.magicalvibes.cards.g.Gingerbrute;
import com.github.laxika.magicalvibes.cards.h.HopelessNightmare;
import com.github.laxika.magicalvibes.cards.u.UnassumingSage;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RealmScorcherHellkite.class, Gingerbrute.class, HopelessNightmare.class,
        UnassumingSage.class, ElfhameDruid.class})
class RealmScorcherHellkiteTest extends BaseCardTest {

    @Test
    void withoutBargainDoesNotAddMana() {
        harness.castFromHand(player1, new RealmScorcherHellkite(), "{4}{R}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void bargainAddsFourManaInAnyCombinationOfColors() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new Gingerbrute());
        harness.setHand(player1, List.of(new RealmScorcherHellkite()));
        addHellkiteMana();

        harness.castKickedCreatureWithPermanent(player1, 0, sacrifice.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, ManaColor.RED.name());
        harness.handleListChoice(player1, ManaColor.BLUE.name());
        harness.handleListChoice(player1, ManaColor.GREEN.name());
        harness.handleListChoice(player1, ManaColor.WHITE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Gingerbrute");
    }

    @Test
    void bargainCanSacrificeAnEnchantmentAndAddFourManaOfOneColor() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new HopelessNightmare());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new RealmScorcherHellkite()));
        addHellkiteMana();

        harness.castKickedCreatureWithPermanent(player1, 0, sacrifice.getId());
        harness.assertInGraveyard(player1, "Hopeless Nightmare");
        while (!gd.stack.isEmpty() && !gd.interaction.isAwaitingInput()) {
            harness.passBothPriorities();
        }

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        for (int i = 0; i < 4; i++) {
            harness.handleListChoice(player1, ManaColor.BLACK.name());
        }

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(4);
    }

    @Test
    void bargainCannotSacrificeANontokenNonartifactCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new UnassumingSage());
        harness.setHand(player1, List.of(new RealmScorcherHellkite()));
        addHellkiteMana();

        assertThatThrownBy(() -> harness.castKickedCreatureWithPermanent(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Unassuming Sage");
    }

    @Test
    @CardUsed({RealmScorcherHellkite.class, Gingerbrute.class, ElfhameDruid.class})
    void bargainDoesNotAllowManaRestrictedToKickedSpells() {
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new ElfhameDruid());
        druid.setSummoningSick(false);
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new Gingerbrute());
        harness.setHand(player1, List.of(new RealmScorcherHellkite()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, null, null);

        assertThatThrownBy(() -> harness.castKickedCreatureWithPermanent(player1, 0, sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Realm-Scorcher Hellkite");
        harness.assertOnBattlefield(player1, "Gingerbrute");
    }

    @Test
    void activatedAbilityDealsOneDamageToTargetPlayer() {
        harness.addToBattlefield(player1, new RealmScorcherHellkite());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    void activatedAbilityCanKillACreature() {
        harness.addToBattlefield(player1, new RealmScorcherHellkite());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Gingerbrute());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Gingerbrute");
        harness.assertNotOnBattlefield(player2, "Gingerbrute");
    }

    @Test
    void activatedAbilityCanBeUsedRepeatedlyWhileTapped() {
        Permanent hellkite = harness.addToBattlefieldAndReturn(player1, new RealmScorcherHellkite());
        hellkite.tap();
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    private void addHellkiteMana() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
