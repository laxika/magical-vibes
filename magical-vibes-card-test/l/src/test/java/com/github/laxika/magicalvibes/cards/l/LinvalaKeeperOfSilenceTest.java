package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DawnglareInvoker;
import com.github.laxika.magicalvibes.cards.d.DreamstoneHedron;
import com.github.laxika.magicalvibes.cards.o.OvergrownBattlement;
import com.github.laxika.magicalvibes.cards.o.Ovinize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LinvalaKeeperOfSilence.class, DawnglareInvoker.class,
        OvergrownBattlement.class, DreamstoneHedron.class, Ovinize.class})
class LinvalaKeeperOfSilenceTest extends BaseCardTest {

    @Test
    void blocksOpponentsCreatureAbilities() {
        harness.addToBattlefield(player1, new LinvalaKeeperOfSilence());
        harness.addToBattlefield(player2, new DawnglareInvoker());
        harness.addMana(player2, ManaColor.COLORLESS, 8);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated")
                .hasMessageContaining("Linvala, Keeper of Silence");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(8);
    }

    @Test
    void blocksOpponentsCreatureManaAbilities() {
        harness.addToBattlefield(player1, new LinvalaKeeperOfSilence());
        Permanent creature = addCreatureReady(player2, new OvergrownBattlement());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated")
                .hasMessageContaining("Linvala, Keeper of Silence");
        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void allowsControllersCreatureAbilities() {
        harness.addToBattlefield(player1, new LinvalaKeeperOfSilence());
        harness.addToBattlefield(player1, new DawnglareInvoker());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OvergrownBattlement());
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void allowsOpponentsNoncreatureAbilities() {
        harness.addToBattlefield(player1, new LinvalaKeeperOfSilence());
        harness.addToBattlefield(player2, new DreamstoneHedron());
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.setLibrary(player2, List.of(new OvergrownBattlement(),
                new OvergrownBattlement(), new OvergrownBattlement()));
        int initialHandSize = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player2, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(initialHandSize + 3);
        harness.assertInGraveyard(player2, "Dreamstone Hedron");
    }

    @Test
    void allowsControllersCreatureManaAbilities() {
        harness.addToBattlefield(player1, new LinvalaKeeperOfSilence());
        Permanent creature = addCreatureReady(player1, new OvergrownBattlement());

        harness.activateAbility(player1, 1, null, null);

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void allowsOpponentsArtifactManaAbilities() {
        harness.addToBattlefield(player1, new LinvalaKeeperOfSilence());
        harness.addToBattlefield(player2, new DreamstoneHedron());

        harness.activateAbility(player2, 0, null, null);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }

    @Test
    void doesNotCounterAnAbilityAlreadyOnTheStack() {
        harness.addToBattlefield(player2, new DawnglareInvoker());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new OvergrownBattlement());
        harness.addMana(player2, ManaColor.COLORLESS, 8);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.addToBattlefield(player1, new LinvalaKeeperOfSilence());

        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void allowsOpponentsCreatureAbilitiesAfterLinvalaLosesAbilities() {
        Permanent linvala = harness.addToBattlefieldAndReturn(player1, new LinvalaKeeperOfSilence());
        harness.addToBattlefield(player2, new DawnglareInvoker());
        harness.setHand(player1, List.of(new Ovinize()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, linvala.getId());
        harness.addMana(player2, ManaColor.COLORLESS, 8);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(linvala.isTapped()).isTrue();
    }

    @Test
    void allowsOpponentsCreatureManaAbilitiesAfterLinvalaLosesAbilities() {
        Permanent linvala = harness.addToBattlefieldAndReturn(player1, new LinvalaKeeperOfSilence());
        addCreatureReady(player2, new OvergrownBattlement());
        harness.setHand(player1, List.of(new Ovinize()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, linvala.getId());

        harness.activateAbility(player2, 0, null, null);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }
}
