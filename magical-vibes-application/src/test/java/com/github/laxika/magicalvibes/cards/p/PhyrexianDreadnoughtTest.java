package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.e.Enfeeblement;
import com.github.laxika.magicalvibes.cards.j.JungleWurm;
import com.github.laxika.magicalvibes.cards.m.MorticianBeetle;
import com.github.laxika.magicalvibes.cards.r.RayOfCommand;
import com.github.laxika.magicalvibes.cards.t.TeekasDragon;
import com.github.laxika.magicalvibes.cards.v.ViashinoWarrior;
import com.github.laxika.magicalvibes.cards.w.WallOfRoots;
import com.github.laxika.magicalvibes.cards.z.ZhalfirinKnight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Enfeeblement.class, PhyrexianDreadnought.class, JungleWurm.class,
        RayOfCommand.class, TeekasDragon.class, ViashinoWarrior.class,
        WallOfRoots.class, ZhalfirinKnight.class})
class PhyrexianDreadnoughtTest extends BaseCardTest {

    private void castDreadnought() {
        harness.castFromHand(player1, new PhyrexianDreadnought(), "{1}");
        harness.passBothPriorities(); // resolve creature spell → ETB on stack
        harness.passBothPriorities(); // resolve ETB
    }

    @Test
    @DisplayName("Auto-sacrifices when the controller's other creatures cannot reach total power 12")
    void autoSacrificesWithoutEnoughPower() {
        harness.addToBattlefield(player1, new TeekasDragon());
        harness.addToBattlefield(player1, new JungleWurm());
        castDreadnought();

        // 5 + 5 = 10 power available, so there is nothing to choose.
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Phyrexian Dreadnought");
        harness.assertOnBattlefield(player1, "Teeka's Dragon");
        harness.assertOnBattlefield(player1, "Jungle Wurm");
    }

    @Test
    @DisplayName("Sacrificing creatures with total power 12 keeps Phyrexian Dreadnought")
    void sacrificingEnoughPowerKeepsDreadnought() {
        harness.addToBattlefield(player1, new TeekasDragon());
        harness.addToBattlefield(player1, new JungleWurm());
        harness.addToBattlefield(player1, new ZhalfirinKnight());
        harness.addToBattlefield(player1, new ZhalfirinKnight());
        castDreadnought();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(
                harness.getPermanentId(player1, "Teeka's Dragon"),
                harness.getPermanentId(player1, "Jungle Wurm"),
                harness.getPermanentId(player1, "Zhalfirin Knight")));

        harness.assertOnBattlefield(player1, "Phyrexian Dreadnought");
        harness.assertInGraveyard(player1, "Teeka's Dragon");
        harness.assertInGraveyard(player1, "Jungle Wurm");
        // The knight was not chosen, so it survives.
        assertThat(countPermanents(player1, "Zhalfirin Knight")).isEqualTo(1);
    }

    @Test
    @DisplayName("Choosing no creatures sacrifices Phyrexian Dreadnought and nothing else")
    void decliningSacrificesDreadnought() {
        harness.addToBattlefield(player1, new TeekasDragon());
        harness.addToBattlefield(player1, new TeekasDragon());
        harness.addToBattlefield(player1, new ZhalfirinKnight());
        castDreadnought();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of());

        harness.assertInGraveyard(player1, "Phyrexian Dreadnought");
        assertThat(countPermanents(player1, "Teeka's Dragon")).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Zhalfirin Knight");
    }

    @Test
    @DisplayName("A selection below total power 12 is rejected and the prompt stands")
    void rejectsSelectionBelowThreshold() {
        harness.addToBattlefield(player1, new TeekasDragon());
        harness.addToBattlefield(player1, new TeekasDragon());
        harness.addToBattlefield(player1, new ZhalfirinKnight());
        castDreadnought();

        Permanent dragon = findPermanents(player1, "Teeka's Dragon").getFirst();
        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of(dragon.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.assertOnBattlefield(player1, "Phyrexian Dreadnought");
        assertThat(countPermanents(player1, "Teeka's Dragon")).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Zhalfirin Knight");
    }

    @Test
    @DisplayName("A selection above total power 12 is accepted")
    void acceptsSelectionAboveThreshold() {
        harness.addToBattlefield(player1, new TeekasDragon());
        harness.addToBattlefield(player1, new JungleWurm());
        harness.addToBattlefield(player1, new ViashinoWarrior());
        castDreadnought();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(
                harness.getPermanentId(player1, "Teeka's Dragon"),
                harness.getPermanentId(player1, "Jungle Wurm"),
                harness.getPermanentId(player1, "Viashino Warrior")));

        harness.assertOnBattlefield(player1, "Phyrexian Dreadnought");
        harness.assertInGraveyard(player1, "Teeka's Dragon");
        harness.assertInGraveyard(player1, "Jungle Wurm");
        harness.assertInGraveyard(player1, "Viashino Warrior");
    }

    @Test
    @DisplayName("Negative power counts against the required total")
    void negativePowerCountsAgainstTotal() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new WallOfRoots());
        harness.addToBattlefield(player1, new TeekasDragon());
        harness.addToBattlefield(player1, new JungleWurm());
        harness.addToBattlefield(player1, new ZhalfirinKnight());

        harness.setHand(player1, List.of(new Enfeeblement()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castEnchantment(player1, 0, wall.getId());
        harness.passBothPriorities();

        castDreadnought();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Phyrexian Dreadnought");
        harness.assertOnBattlefield(player1, "Wall of Roots");
    }

    @Test
    @DisplayName("Negative power reduces the selected total")
    void negativePowerReducesSelectedTotal() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new WallOfRoots());
        harness.addToBattlefield(player1, new TeekasDragon());
        harness.addToBattlefield(player1, new JungleWurm());
        harness.addToBattlefield(player1, new ZhalfirinKnight());
        harness.addToBattlefield(player1, new ViashinoWarrior());

        harness.setHand(player1, List.of(new Enfeeblement()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castEnchantment(player1, 0, wall.getId());
        harness.passBothPriorities();

        castDreadnought();

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of(
                wall.getId(),
                harness.getPermanentId(player1, "Teeka's Dragon"),
                harness.getPermanentId(player1, "Jungle Wurm"),
                harness.getPermanentId(player1, "Zhalfirin Knight"))))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.assertOnBattlefield(player1, "Phyrexian Dreadnought");
        harness.assertOnBattlefield(player1, "Wall of Roots");
    }

    @Test
    @DisplayName("A Dreadnought that changes controller before its trigger resolves is not sacrificed")
    void doesNotSacrificeDreadnoughtAfterControlChanges() {
        harness.castFromHand(player1, new PhyrexianDreadnought(), "{1}");
        harness.passBothPriorities();

        Permanent dreadnought = findPermanent(player1, "Phyrexian Dreadnought");
        harness.setHand(player2, List.of(new RayOfCommand()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, dreadnought.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(dreadnought.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(dreadnought.getId()));
        harness.assertNotOnBattlefield(player1, "Phyrexian Dreadnought");
        harness.assertNotInGraveyard(player1, "Phyrexian Dreadnought");
    }

    @CardUsed({MorticianBeetle.class})
    @Test
    @DisplayName("Creatures chosen for the trigger are sacrificed simultaneously")
    void sacrificesChosenCreaturesSimultaneously() {
        Permanent beetle = harness.addToBattlefieldAndReturn(player1, new MorticianBeetle());
        harness.addToBattlefield(player1, new TeekasDragon());
        harness.addToBattlefield(player1, new JungleWurm());
        harness.addToBattlefield(player1, new ViashinoWarrior());
        castDreadnought();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(
                beetle.getId(),
                harness.getPermanentId(player1, "Teeka's Dragon"),
                harness.getPermanentId(player1, "Jungle Wurm"),
                harness.getPermanentId(player1, "Viashino Warrior")));

        for (int i = 0; i < 4; i++) {
            harness.passBothPriorities();
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            harness.handleMayAbilityChosen(player1, false);
        }
    }
}
