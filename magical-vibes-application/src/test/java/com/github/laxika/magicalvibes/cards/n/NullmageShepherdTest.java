package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NullmageShepherd.class, GrizzlyBears.class, LeoninScimitar.class, Pacifism.class})
class NullmageShepherdTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping four creatures destroys a target artifact")
    void destroysArtifact() {
        Permanent shepherd = addReadyShepherd();
        List<Permanent> creatures = addThreeReadyCreatures();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Leonin Scimitar");
        assertThat(shepherd.isTapped()).isTrue();
        assertThat(creatures).allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Tapping four creatures destroys a target enchantment")
    void destroysEnchantment() {
        addReadyShepherd();
        addThreeReadyCreatures();
        Permanent host = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent pacifism = harness.addToBattlefieldAndReturn(player2, new Pacifism());
        pacifism.setAttachedTo(host.getId());

        harness.activateAbility(player1, 0, null, pacifism.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Pacifism");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        addReadyShepherd();
        addThreeReadyCreatures();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without four untapped creatures")
    void cannotActivateWithoutFourCreatures() {
        addReadyShepherd();
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyShepherd() {
        Permanent shepherd = harness.addToBattlefieldAndReturn(player1, new NullmageShepherd());
        shepherd.setSummoningSick(false);
        return shepherd;
    }

    private List<Permanent> addThreeReadyCreatures() {
        return List.of(
                addCreatureReady(player1, new GrizzlyBears()),
                addCreatureReady(player1, new GrizzlyBears()),
                addCreatureReady(player1, new GrizzlyBears())
        );
    }
}
