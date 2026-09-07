package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ClementTheWorrywort.class, GrizzlyBears.class, AirElemental.class})
class ClementTheWorrywortTest extends BaseCardTest {

    @Test
    @DisplayName("Self-entry targets only an own creature with lesser mana value")
    void selfEntryTargetsOnlyLowerManaValueCreatureYouControl() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent airElemental = addCreatureReady(player1, new AirElemental());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ClementTheWorrywort()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(bears.getId(), player1.getId())
                .doesNotContain(airElemental.getId(), opponentBears.getId(),
                        findPermanent(player1, "Clement, the Worrywort").getId());

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Clement, the Worrywort");
    }

    @Test
    @DisplayName("Another creature entering triggers Clement's bounce ability")
    void anotherCreatureEntryTriggersBounce() {
        addCreatureReady(player1, new ClementTheWorrywort());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(bears.getId(), player1.getId())
                .doesNotContain(findPermanent(player1, "Air Elemental").getId());

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Air Elemental");
    }

    @Test
    @DisplayName("Clement can enter without a bounce target")
    void canEnterWithoutTarget() {
        harness.setHand(player1, List.of(new ClementTheWorrywort()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Clement, the Worrywort");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A source leaving before resolution does not invalidate the chosen target")
    void sourceLeavingBeforeResolutionKeepsTargetLegal() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ClementTheWorrywort()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, bears.getId());

        Permanent clement = findPermanent(player1, "Clement, the Worrywort");
        gd.playerBattlefields.get(player1.getId()).remove(clement);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Frog mana abilities add fixed-color creature-spell-only mana")
    void frogManaAbilitiesAddRestrictedGreenAndBlueMana() {
        Permanent clement = addCreatureReady(player1, new ClementTheWorrywort());

        harness.activateAbility(player1, 0, 0, null, null);
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.GREEN)).isZero();
        assertThat(pool.getCreatureSpellOnlyMana(ManaColor.GREEN)).isEqualTo(1);

        clement.untap();
        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(pool.get(ManaColor.BLUE)).isZero();
        assertThat(pool.getCreatureSpellOnlyMana(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Only Frogs receive Clement's mana abilities")
    void onlyFrogsReceiveManaAbilities() {
        addCreatureReady(player1, new ClementTheWorrywort());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bears.isTapped()).isFalse();
    }

}
