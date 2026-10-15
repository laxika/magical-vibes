package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BorosSignet;
import com.github.laxika.magicalvibes.cards.e.ElvishSkysweeper;
import com.github.laxika.magicalvibes.cards.f.FaithsFetters;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NullmageShepherd.class, ElvishSkysweeper.class, BorosSignet.class, FaithsFetters.class})
class NullmageShepherdTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping four creatures destroys a target artifact")
    void destroysArtifact() {
        Permanent shepherd = addReadyShepherd();
        List<Permanent> creatures = addThreeReadyCreatures();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new BorosSignet());

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Boros Signet");
        assertThat(shepherd.isTapped()).isTrue();
        assertThat(creatures).allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Tapping four creatures destroys a target enchantment")
    void destroysEnchantment() {
        addReadyShepherd();
        addThreeReadyCreatures();
        Permanent host = addCreatureReady(player2, new ElvishSkysweeper());
        Permanent faithsFetters = harness.addToBattlefieldAndReturn(player2, new FaithsFetters());
        faithsFetters.setAttachedTo(host.getId());

        harness.activateAbility(player1, 0, null, faithsFetters.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Faith's Fetters");
        harness.assertOnBattlefield(player2, "Elvish Skysweeper");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        addReadyShepherd();
        addThreeReadyCreatures();
        Permanent creature = addCreatureReady(player2, new ElvishSkysweeper());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without four untapped creatures")
    void cannotActivateWithoutFourCreatures() {
        addReadyShepherd();
        addCreatureReady(player1, new ElvishSkysweeper());
        addCreatureReady(player1, new ElvishSkysweeper());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new BorosSignet());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot pay with tapped or opponent creatures")
    void cannotPayWithTappedOrOpponentCreatures() {
        Permanent shepherd = addReadyShepherd();
        addCreatureReady(player1, new ElvishSkysweeper());
        addCreatureReady(player1, new ElvishSkysweeper());
        Permanent tappedCreature = addCreatureReady(player1, new ElvishSkysweeper());
        tappedCreature.tap();
        addCreatureReady(player2, new ElvishSkysweeper());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new BorosSignet());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(shepherd.isTapped()).isFalse();
        assertThat(tappedCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Summoning-sick Shepherd and creatures can pay the tap cost")
    void canTapSummoningSickCreatures() {
        Permanent shepherd = harness.addToBattlefieldAndReturn(player1, new NullmageShepherd());
        List<Permanent> creatures = List.of(
                harness.addToBattlefieldAndReturn(player1, new ElvishSkysweeper()),
                harness.addToBattlefieldAndReturn(player1, new ElvishSkysweeper()),
                harness.addToBattlefieldAndReturn(player1, new ElvishSkysweeper())
        );
        shepherd.setSummoningSick(true);
        creatures.forEach(creature -> creature.setSummoningSick(true));
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new BorosSignet());

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Boros Signet");
        assertThat(shepherd.isTapped()).isTrue();
        assertThat(creatures).allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("A tapped Shepherd can activate by tapping four other creatures")
    void canActivateWhileShepherdIsTapped() {
        Permanent shepherd = addReadyShepherd();
        shepherd.tap();
        List<Permanent> creatures = addThreeReadyCreatures();
        Permanent fourthCreature = addCreatureReady(player1, new ElvishSkysweeper());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new BorosSignet());

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Boros Signet");
        assertThat(creatures).allMatch(Permanent::isTapped);
        assertThat(fourthCreature.isTapped()).isTrue();
        assertThat(shepherd.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can destroy an artifact controlled by the activating player")
    void canDestroyOwnArtifact() {
        addReadyShepherd();
        addThreeReadyCreatures();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new BorosSignet());

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Boros Signet");
        harness.assertNotOnBattlefield(player1, "Boros Signet");
    }

    @Test
    @DisplayName("Can choose four other creatures and leave Shepherd untapped")
    void canChooseCreaturesWithoutTappingShepherd() {
        Permanent shepherd = addReadyShepherd();
        List<Permanent> creatures = addThreeReadyCreatures();
        Permanent fourthCreature = addCreatureReady(player1, new ElvishSkysweeper());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new BorosSignet());

        harness.activateAbility(player1, 0, null, artifact.getId());
        for (Permanent creature : creatures) {
            harness.handlePermanentChosen(player1, creature.getId());
        }
        harness.handlePermanentChosen(player1, fourthCreature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Boros Signet");
        assertThat(creatures).allMatch(Permanent::isTapped);
        assertThat(fourthCreature.isTapped()).isTrue();
        assertThat(shepherd.isTapped()).isFalse();
    }

    private Permanent addReadyShepherd() {
        return addCreatureReady(player1, new NullmageShepherd());
    }

    private List<Permanent> addThreeReadyCreatures() {
        return List.of(
                addCreatureReady(player1, new ElvishSkysweeper()),
                addCreatureReady(player1, new ElvishSkysweeper()),
                addCreatureReady(player1, new ElvishSkysweeper())
        );
    }
}
